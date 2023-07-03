package instrument;

import model.Class;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static instrument.Parser.*;

public class ParserState {
  Parser parser;
  Logger logger;

  int beginOfImports = 0;
  String packageName;

  List<Class> topLevelClasses = new ArrayList<>();
  Stack<Class> classStack = new Stack<>();
  List<Block> allBlocks = new ArrayList<>();
  Stack<Block> blockStack = new Stack<>();
  Stack<Method> methodStack = new Stack<>();
  Class curClass = null;
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


  void enterClass(boolean anonymous, boolean local) {
    if (curClass != null) {
      classStack.push(curClass);
    }
    if (curMeth != null) {
      methodStack.push(curMeth);
      curMeth = null;
    }
    String className = (anonymous) ? null : parser.la.val;
    curClass = new Class(className);
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
    curBlock.endPos = parser.t.charPos;
    if (curBlock.blockType != BlockType.SS_LAMBDA) { // exclude ")" or ";" in lambda blocks
      curBlock.endPos += parser.t.val.length();
    }
    logger.leave(curBlock);
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
    }
    if (isMethod) {
      leaveMethod();
    }
  }

  void checkSingleStatement(boolean isAssignment, boolean isSwitch, boolean isArrowExpr) {
    if (parser.t.kind == _else && parser.la.kind == _if) {
      logger.log("else if found. no block.");
      return;
    }
    if (parser.la.kind != _lbrace) {
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
    if (curMeth == null && parser.t.kind == _static && parser.la.kind == _lbrace) {
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
    return parser.la.kind == _ident && parser.scanner.Peek().kind == _lpar;
  }

  boolean staticAndLBrace() {
    return parser.la.kind == _static && parser.scanner.Peek().kind == _lbrace;
  }

  boolean isLabel() {
    return parser.la.kind == _ident && parser.scanner.Peek().kind == _colon;
  }

  boolean thisAndLPar() {
    return parser.la.kind == _this && parser.scanner.Peek().kind == _lpar;
  }

  boolean isAssignment() {
    return parser.t.kind == _equals || parser.t.kind == _return || parser.t.kind == _yield;
  }

  static class Logger {
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String BRIGHT = "\u001B[97m";
    public static final String RESET = "\u001B[0m";

    Parser parser;
    static boolean verbose = true;
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
      if (comp instanceof Class clazz) return "class <" + clazz.getFullName() + ">";
      if (comp instanceof Method meth) return meth + "()";
      if (comp instanceof Block block)
        return String.format("%s [%d]", block.blockType, leave ? block.endPos : block.begPos);
      throw new RuntimeException("unknown component type: " + comp.getClass());
    }
  }
}
