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
  boolean verbose = false;

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
    if (jumpStatement.abortPropagation(curBlock.blockType)) {
      return;
    }
    for (int i = blockStack.size() - 1; i >= 0; i--) {
      Block block = blockStack.get(i);
      block.registerInnerJumpBlock(curBlock);
      if (jumpStatement.stopPropagationAt(block.blockType)) {
        break;
      }
    }
  }


  void enterClass(boolean anonymous, boolean local) {
    if (curClass != null) {
      classStack.push(curClass);
    }
    if (curMeth != null) {
      methodStack.push(curMeth);
      curMeth = null;
    }
    String className = (anonymous) ? null : parser.la.val;
    curClass = new JClass(className);
    if (packageName != null) {
      curClass.packageName = packageName;
    }
    if (!classStack.isEmpty()) {
      curClass.setParentClass(classStack.peek());
    }
    if (anonymous) {
      curClass.classType = ClassType.ANONYMOUS;
    } else if (local) {
      curClass.classType = ClassType.LOCAL;
    }
    if (classStack.isEmpty()) {
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

  void enterBlock(BlockType blockType, boolean isSingleStatement) {
    assert curClass != null;
    if (curBlock != null) {
      CodePosition regionEndPos = curBlock.isSingleStatement ? tokenEndPosition(parser.t) : tokenStartPosition(parser.la);
      curBlock.endCodeRegion(regionEndPos);
      blockStack.push(curBlock);
    }
    curBlock = new Block(blockType);
    curBlock.id = curBlockId++;
    curBlock.setParentMethod(curMeth);
    curBlock.setParentClass(curClass);
    curBlock.isSingleStatement = isSingleStatement;
    curBlock.beg = Util.getBlockBegPos(parser, blockType, isSingleStatement);
    curBlock.incInsertPosition = Util.getIncInsertPos(parser, blockType, isSingleStatement);
    CodePosition regionStartPosition = Util.getRegionStartPos(parser, blockType, isSingleStatement);
    curBlock.startCodeRegion(regionStartPosition);
    allBlocks.add(curBlock);
    logger.enter(curBlock);
  }

  void leaveBlock(BlockType blockType) {
    leaveBlock(blockType, curBlock.isSingleStatement);
  }

  void leaveSwitchColonCase(BlockType blockType) {
    leaveBlock(blockType, true);
  }

  void leaveBlock(BlockType blockType, boolean missingBraces) {
    curBlock.end = tokenEndPosition(missingBraces ? parser.t : parser.la);
    curBlock.endCodeRegion(tokenEndPosition(parser.t));
    logger.leave(curBlock);
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
      curBlock.reenterBlock(tokenStartPosition(missingBraces ? parser.la : parser.scanner.Peek()));
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

  BlockType enterSSArrowBlock(boolean isSwitch, boolean isAssignment) {
    BlockType blockType = BlockType.LAMBDA;
    if (isSwitch) {
      blockType = (isAssignment) ? BlockType.SWITCH_EXPR_CASE : BlockType.SWITCH_CASE;
    }
    enterBlock(blockType, true);
    return blockType;
  }

  void leaveSingleStatement(BlockType blockType) {
    if (curBlock.isSingleStatement) {
      leaveBlock(blockType);
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

  public class Logger {
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String BRIGHT = "\u001B[97m";
    public static final String RESET = "\u001B[0m";

    Parser parser;
    public static int indent = 1;

    public Logger(Parser p) {
      parser = p;
    }

    public void log(String logMessage) {
      if (!verbose) return;
      System.out.printf("%s%3d:%s%-" + indent + "s%s%n", BRIGHT, parser.t.line, RESET, "", logMessage);
    }

    public void log(String formatString, Object... values) {
      log(String.format(formatString, values));
    }

    void enter(Component comp) {
      log(describe(comp, false) + GREEN + " -->" + RESET);
      indent += 2;
    }

    void leave(Component comp) {
      indent -= 2;
      log(describe(comp, true) + RED + " <--" + RESET);
    }

    private String describe(Component comp, boolean leave) {
      if (comp instanceof JClass clazz) return "class <" + clazz.getFullName() + ">";
      if (comp instanceof Method meth) return meth + "()";
      if (comp instanceof Block block)
        return String.format(
            "%s%s [%d]%s",
            block.blockType,
            block.isSingleStatement ? ", SS" : "",
            leave ? block.end.pos() : block.beg.pos(),
            block.jumpStatement != null ? " (" + block.jumpStatement.name() + ")" : ""
        );
      throw new RuntimeException("unknown component type: " + comp.getClass());
    }
  }
}
