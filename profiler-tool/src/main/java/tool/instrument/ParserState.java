package tool.instrument;

import tool.model.JClass;
import tool.model.*;
import tool.model.BlockType;
import tool.model.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

  public ParserState(Parser p) {
    parser = p;
    logger = new Logger(p);
  }

  void setPackageName(List<String> packageName) {
    this.packageName = String.join(".", packageName);
    this.beginOfImports = parser.t.charPos + parser.t.val.length();
  }

  void markEndOfSuperCall() {
    assert curClass != null && curMeth != null && curBlock.blockType == BlockType.CONSTRUCTOR;
    curBlock.incInsertPosition = parser.t.charPos + parser.t.val.length();
  }

  void registerThrow() {
    curBlock.startsWithThrow = true;
  }

  void registerJumpStatement() {
    curBlock.endsWithJumpStatement = true;
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

  void enterBlock(boolean isMethod) {  // no missing braces
    enterBlock(getBlockTypeByContext(isMethod));
  }

  void enterBlock(BlockType blockType) {
    assert curClass != null;
    if (curBlock != null) {
      blockStack.push(curBlock);
    }
    curBlock = new Block(blockType);
    curBlock.setParentMethod(curMeth);
    curBlock.setParentClass(curClass);
    if (blockType.hasNoBraces()) {
      curBlock.beg = parser.t.line;
      curBlock.begPos = parser.t.charPos + parser.t.val.length();
    } else { // la == '{'
      curBlock.beg = parser.la.line;
      curBlock.begPos = parser.la.charPos;
      curBlock.incInsertPosition = parser.la.charPos + parser.la.val.length();
    }
    allBlocks.add(curBlock);
    logger.enter(curBlock);
  }

  void leaveBlock(boolean isMethod) {
    curBlock.end = parser.t.line;
    curBlock.endPos = parser.t.charPos + parser.t.val.length();
    logger.leave(curBlock);
    boolean splitParentBlock = curBlock.endsWithJumpStatement;
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
      if (splitParentBlock && !parser.la.val.equals("}")) { // no statement after block end
        leaveBlock(false); // do not exit methods, ever
        // SS_BLOCK for now ... just for inserting counter right away, not after next token (which should be '{') TODO
        enterBlock(BlockType.SS_BLOCK);
      }
    }
    if (isMethod) {
      leaveMethod();
    }
  }

  void checkSingleStatement(boolean isAssignment, boolean isSwitch, boolean isArrowExpr) {
    if (parser.t.val.equals("else") && parser.la.val.equals("if")) {
      logger.log("else if found. no block.");
      return;
    }
    if (!parser.la.val.equals("{")) {
      enterBlock(getBlockTypeByContext(true, isAssignment, isSwitch, isArrowExpr));
    }
  }

  void leaveSingleStatement() {
    if (curBlock.blockType.hasNoBraces()) {
      leaveBlock(false);
    }
  }

  BlockType getBlockTypeByContext(boolean isMethod) {
    if (isMethod) {
      if (curMeth.name.equals(curClass.name)) { // TODO: not entirely correct, also must not have return type
        return BlockType.CONSTRUCTOR;
      } else {
        return BlockType.METHOD;
      }
    }
    return getBlockTypeByContext(false, false, false, false);
  }

  BlockType getBlockTypeByContext(boolean missingBraces, boolean inAssignment, boolean inSwitch, boolean inArrowExpr) {
    if (curMeth == null && parser.t.val.equals("static") && parser.la.val.equals("{")) {
      return BlockType.STATIC;
    }
    if (!missingBraces) {
      return BlockType.BLOCK;
    }
    if (inArrowExpr && inSwitch && inAssignment) {
      return BlockType.SS_SWITCH_EXPR_ARROW_CASE;
    } else if (inArrowExpr && !inSwitch) {
      return BlockType.SS_LAMBDA;
    } else if (!inArrowExpr && inSwitch) {
      return BlockType.SWITCH_CASE;
    }
    return BlockType.SS_BLOCK;
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
        return String.format("%s [%d]", block.blockType, leave ? block.endPos : block.begPos);
      throw new RuntimeException("unknown component type: " + comp.getClass());
    }
  }
}
