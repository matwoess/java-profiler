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
  String packageName = null;

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
    for (Block block = curBlock; block.parentBlock != null; block = block.parentBlock) {
      if (jumpStatement.stopPropagationAt(block)) {
        break;
      }
      block.registerInnerJumpBlock(curBlock);
    }
  }


  void enterClass(ClassType classType) {
    String className = null;
    if (classType == ClassType.ANONYMOUS) {
      endCodeRegion();
    } else {
      className = parser.la.val;
    }
    JClass newClass = new JClass(className, classType);
    newClass.packageName = packageName;
    if (curMeth != null) {
      methodStack.push(curMeth);
      curMeth = null;
    }
    if (curClass != null) {
      classStack.push(curClass);
      newClass.setParentClass(curClass);
    } else {
      topLevelClasses.add(newClass);
    }
    logger.enter(newClass);
    curClass = newClass;
  }

  void leaveClass() {
    logger.leave(curClass);
    if (!methodStack.empty() && (curClass.classType == ClassType.ANONYMOUS || curClass.classType == ClassType.LOCAL)) {
      curMeth = methodStack.pop();
      //reenterBlock(curBlock.blockType, curBlock.isSingleStatement || curBlock.blockType == BlockType.COLON_CASE);
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
    Block newBlock = new Block(blockType);
    newBlock.id = curBlockId++;
    newBlock.setParentMethod(curMeth);
    newBlock.setParentClass(curClass);
    newBlock.isSingleStatement = blockType != BlockType.COLON_CASE && missingBraces;
    newBlock.beg = Util.getBlockBegPos(parser, blockType, missingBraces);
    newBlock.incInsertPosition = Util.getIncInsertPos(parser, blockType, missingBraces);
    allBlocks.add(newBlock);
    if (curBlock != null) {
      endCodeRegion();
      blockStack.push(curBlock);
      newBlock.setParentBlock(curBlock);
    }
    logger.enter(newBlock);
    curBlock = newBlock;
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
    logger.leave(curBlock);
    endCodeRegion();
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
    }
    if (blockType == BlockType.METHOD) {
      leaveMethod();
    }
    reenterBlock(blockType, missingBraces);
  }

  void enterSingleStatementBlock(BlockType blockType) {
    if (parser.t.val.equals("else") && parser.la.val.equals("if")) {
      logger.log("else if found. no block.");
      return;
    }
    enterBlock(blockType, true);
  }

  void enterSSArrowBlock(BlockType blockType) {
    enterBlock(blockType, true);
  }

  void leaveSingleStatement(BlockType blockType) {
    if (curBlock.isSingleStatement) { // due to else if
      leaveBlock(blockType);
    }
  }

  void startCodeRegion(BlockType blockType, boolean missingBraces) {
    assert curCodeRegion == null;
    Token nextToken = getRegionStartToken(parser, blockType, missingBraces);
    if (validCodeRegionStartToken(nextToken)) {
      curCodeRegion = new CodeRegion();
      curCodeRegion.beg = tokenStartPosition(nextToken);
      curCodeRegion.block = curBlock;
      logger.enter(curCodeRegion);
    }
  }

  boolean validCodeRegionStartToken(Token nextToken) {
    //if (curMeth == null) return false;
    if (curBlock.blockType.hasNoCounter()) return false;
    return !nextToken.val.equals("else");
  }

  private void endCodeRegion() {
    if (curCodeRegion == null) {
      return;
    }
    curCodeRegion.end = tokenEndPosition(parser.t);
    logger.leave(curCodeRegion);
    assert curCodeRegion.end != curCodeRegion.beg;
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
