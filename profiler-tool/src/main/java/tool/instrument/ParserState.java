package tool.instrument;

import tool.model.JClass;
import tool.model.*;
import tool.model.BlockType;
import tool.model.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static tool.instrument.Util.*;

public class ParserState {
  Parser parser;
  Logger logger;

  int beginOfImports = 0;
  String packageName;

  List<JClass> topLevelClasses = new ArrayList<>();
  Stack<JClass> classStack = new Stack<>();
  List<Block> allBlocks = new ArrayList<>();
  Stack<Block> blockStack = new Stack<>();
  Stack<Method> methodStack = new Stack<>();
  JClass curClass = null;
  Method curMeth = null;
  Block curBlock = null;
  int curBlockId = 0;
  CodeRegion curCodeRegion;

  public ParserState(Parser p) {
    parser = p;
    logger = new Logger(p);
  }

  void setPackageName(List<String> packageName) {
    this.packageName = String.join(".", packageName);
    this.beginOfImports = endOfToken(parser.t);
  }

  void markEndOfSuperCall() {
    assert curClass != null && curMeth != null && curBlock.blockType == BlockType.CONSTRUCTOR;
    curBlock.incInsertPosition = endOfToken(parser.t);
  }

  void registerJumpStatement() {
    JumpStatement jumpStatement = JumpStatement.fromToken(parser.t.val);
    curBlock.jumpStatement = jumpStatement;
    logger.log("> found jump statement: %s", jumpStatement.name());
    registerJumpInOuterBlocks(jumpStatement);
  }

  private void registerJumpInOuterBlocks(JumpStatement jumpStatement) {
    if (jumpStatement.stopPropagationAt(curBlock)) {
      return;
    }
    for (int i = blockStack.size() - 1; i >= 0; i--) {
      Block block = blockStack.get(i);
      block.registerInnerJumpBlock(curBlock);
      if (jumpStatement.stopPropagationAt(block)) {
        break;
      }
    }
  }


  void enterClass(ClassType classType) {
    if (curClass != null) {
      classStack.push(curClass);
    }
    if (curMeth != null) {
      methodStack.push(curMeth);
      curMeth = null;
    }
    String className = null;
    if (classType == ClassType.ANONYMOUS) {
      endCodeRegion();
    } else {
      className = parser.la.val;
    }
    curClass = new JClass(className, classType);
    if (packageName != null) {
      curClass.packageName = packageName;
    }
    if (!classStack.isEmpty()) {
      curClass.setParentClass(classStack.peek());
    } else {
      topLevelClasses.add(curClass);
    }
    logger.enter(curClass);
  }

  void leaveClass() {
    logger.leave(curClass);
    if (!methodStack.empty() && (curClass.classType == ClassType.ANONYMOUS || curClass.classType == ClassType.LOCAL)) {
      curMeth = methodStack.pop();
    }
    if (classStack.empty()) {
      curClass = null;
    } else {
      curClass = classStack.pop();
    }
  }

  void enterMethod() {
    assert curClass != null;
    curMeth = new Method(parser.t.val);
    curMeth.setParentClass(curClass);
    logger.enter(curMeth);
  }

  void leaveMethod() {
    assert curMeth != null;
    logger.leave(curMeth);
    curMeth = null;
  }

  void enterBlock(BlockType blockType) {
    if (blockType == BlockType.METHOD) {
      if (curMeth.name.equals(curClass.name)) { // TODO: not entirely correct, also must not have return type
        blockType = BlockType.CONSTRUCTOR;
      }
    }
    enterBlock(blockType, false);
  }

  void enterSwitchColonCase() {
    assert curBlock != null && curBlock.blockType.isSwitch();
    enterBlock(BlockType.COLON_CASE, true);
  }

  void enterBlock(BlockType blockType, boolean missingBraces) {
    assert curClass != null;
    Block parentBlock = curBlock;
    if (curBlock != null) {
      endCodeRegion();
      blockStack.push(curBlock);
    }
    curBlock = new Block(blockType);
    curBlock.id = curBlockId++;
    curBlock.setParentMethod(curMeth);
    curBlock.setParentClass(curClass);
    curBlock.setParentBlock(parentBlock);
    curBlock.isSingleStatement = blockType != BlockType.COLON_CASE && missingBraces;
    curBlock.beg = Util.getBlockBegPos(parser, blockType, missingBraces);
    curBlock.incInsertPosition = Util.getIncInsertPos(parser, blockType, missingBraces);
    allBlocks.add(curBlock);
    logger.enter(curBlock);
    startCodeRegion(blockType, missingBraces);
  }

  void leaveBlock(BlockType blockType) {
    leaveBlock(blockType, curBlock.isSingleStatement);
  }

  void leaveSwitchColonCase() {
    leaveBlock(BlockType.COLON_CASE, true);
  }

  void leaveBlock(BlockType blockType, boolean missingBraces) {
    curBlock.end = tokenEndPosition(missingBraces ? parser.t : parser.la);
    endCodeRegion();
    logger.leave(curBlock);
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
      reenterBlock(blockType, missingBraces);
    }
    if (blockType == BlockType.METHOD) {
      leaveMethod();
    }
  }

  void enterSingleStatementBlock(BlockType blockType) {
    if (parser.t.val.equals("else") && parser.la.val.equals("if")) {
      logger.log("else if found. no block.");
      return;
    }
    if (!parser.la.val.equals("{")) {
      enterBlock(blockType, true);
    }
  }

  void enterSSArrowBlock(BlockType blockType) {
    enterBlock(blockType, true);
  }

  void leaveSingleStatement(BlockType blockType) {
    if (curBlock.isSingleStatement) {
      leaveBlock(blockType);
    }
  }

  void startCodeRegion(BlockType blockType, boolean missingBraces) {
    Token nextToken = getRegionStartToken(parser, blockType, missingBraces);
    if (validCodeRegionStartToken(nextToken)) {
      curCodeRegion = new CodeRegion();
      curCodeRegion.beg = tokenStartPosition(nextToken);
      curCodeRegion.block = curBlock;
      logger.enter(curCodeRegion);
    }
  }

  static boolean validCodeRegionStartToken(Token nextToken) {
    return switch (nextToken.val) {
      case "case", "catch", "default", "else", "class", "enum", "interface" /*, "record"*/ -> false;
      default -> true;
    };
  }

  private void endCodeRegion() {
    if (curCodeRegion == null) {
      return;
    }
    curCodeRegion.end = tokenEndPosition(parser.t);
    logger.leave(curCodeRegion);
    if (curCodeRegion.end == curCodeRegion.beg) {
      logger.log("empty code region. ignore.");
      return;
    }
    curBlock.addCodeRegion(curCodeRegion);
    curCodeRegion = null;
  }

  private void reenterBlock(BlockType blockType, boolean missingBraces) {
    if (curBlock == null) return;
    Token nextToken = missingBraces ? parser.la : parser.scanner.Peek();
    if (validCodeRegionStartToken(nextToken)) {
      startCodeRegion(blockType, missingBraces);
      curCodeRegion.minusBlocks.addAll(curBlock.innerJumpBlocks);
    }
  }

  boolean identAndLPar() {
    parser.scanner.ResetPeek();
    return parser.la.kind == Parser._ident && parser.scanner.Peek().val.equals("(");
  }

  boolean classNameAndLBrace() {
    parser.scanner.ResetPeek();
    return parser.la.val.equals(curClass.name) && parser.scanner.Peek().val.equals("{");
  }

  boolean staticAndLBrace() {
    parser.scanner.ResetPeek();
    return parser.la.val.equals("static") && parser.scanner.Peek().val.equals("{");
  }

  boolean isLabel() {
    return parser.la.kind == Parser._ident && parser.scanner.Peek().val.equals(":");
  }

  boolean thisAndLPar() {
    return parser.la.val.equals("this") && parser.scanner.Peek().val.equals("(");
  }

  boolean isAssignment() {
    return parser.t.val.equals("=") || parser.t.val.equals("return") || parser.t.val.equals("yield");
  }

  public boolean classDefWithNoLeadingDot() {
    parser.scanner.ResetPeek();
    return !parser.t.val.equals(".")
        && (parser.la.val.equals("class")
        || parser.la.val.equals("interface")
        || parser.la.val.equals("record") && parser.scanner.Peek().kind == Parser._ident);
  }
}
