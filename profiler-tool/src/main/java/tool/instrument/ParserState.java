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
  List<Block> allBlocks = new ArrayList<>();
  Stack<Block> blockBackupStack = new Stack<>();
  JClass curClass = null;
  Method curMeth = null;
  Block curBlock = null;
  int curBlockId = 0;
  CodeRegion curCodeRegion;

  List<String> curLabels = new ArrayList<>();

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
    curBlock.incInsertOffset = endOfToken(parser.t) - curBlock.beg.pos();
  }

  void registerLabel() {
    curLabels.add(parser.la.val);
  }

  void registerJumpStatement() {
    JumpStatement jumpStatement;
    if ((parser.t.val.equals("break") || parser.t.val.equals("continue")) && parser.la.kind == Parser._ident) {
      jumpStatement = JumpStatement.fromTokenWithLabel(parser.t.val, parser.la.val);
    } else {
      jumpStatement = JumpStatement.fromToken(parser.t.val);
    }
    curBlock.jumpStatement = jumpStatement;
    logger.log("> found jump statement: %s", jumpStatement);
    registerJumpInOuterBlocks(jumpStatement);
  }

  private void registerJumpInOuterBlocks(JumpStatement jumpStatement) {
    if (jumpStatement.stopPropagationAt(curBlock)) {
      return; // do not propagate at all if the `curBlock` is catching the jump immediately (e.g. switch case with break)
    }
    for (Block block = curBlock.parentBlock; block != null; block = block.parentBlock) {
      block.registerInnerJumpBlock(curBlock);
      if (jumpStatement.stopPropagationAt(block)) {
        break;
      }
    }
  }


  void enterClass(ClassType classType, String className) {
    endCodeRegion();
    JClass newClass = new JClass(className, classType);
    newClass.packageName = packageName;
    newClass.setParentClass(curClass);
    if (curClass == null) {
      topLevelClasses.add(newClass);
    }
    if (curBlock != null) {
      blockBackupStack.push(curBlock);
      curBlock = null;
      curMeth = null;
    }
    logger.enter(newClass);
    curClass = newClass;
  }

  void leaveClass() {
    logger.leave(curClass);
    if (curClass.classType == ClassType.ANONYMOUS || curClass.classType == ClassType.LOCAL) {
      if (!blockBackupStack.isEmpty()) {
        curBlock = blockBackupStack.pop();
        curMeth = curBlock.method;
        reenterBlock(curBlock.blockType, true); // always true, because leaveClass is called after '}'
      }
    }
    curClass = curClass.parentClass;
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
    endCodeRegion();
    Block newBlock = new Block(blockType);
    newBlock.id = curBlockId++;
    newBlock.setParentMethod(curMeth);
    newBlock.setParentClass(curClass);
    newBlock.isSingleStatement = blockType != BlockType.COLON_CASE && missingBraces;
    newBlock.beg = Util.getBlockBegPos(parser, blockType, missingBraces);
    newBlock.incInsertOffset = Util.getIncInsertOffset(parser, blockType, missingBraces);
    newBlock.setParentBlock(curBlock);
    allBlocks.add(newBlock);
    if (!curLabels.isEmpty()) {
      newBlock.labels.addAll(curLabels);
      curLabels.clear();
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
    curBlock = curBlock.parentBlock;
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
    if (curBlock.blockType.hasNoCounter()) return false;
    return !nextToken.val.equals("else") && !nextToken.val.equals("catch") && !nextToken.val.equals("finally");
  }

  private void endCodeRegion() {
    if (curCodeRegion == null) return;
    curCodeRegion.end = tokenEndPosition(parser.t);
    logger.leave(curCodeRegion);
    // assert !curCodeRegion.end.equals(curCodeRegion.beg); // TODO: remove, true for empty block "{}"
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
