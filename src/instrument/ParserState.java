package instrument;

import model.*;
import model.Class;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static instrument.Parser.*;

public class ParserState {
  Parser parser;

  int beginOfImports = 0;

  List<Class> allClasses = new ArrayList<>();
  Stack<Class> classStack = new Stack<>();
  List<Block> allBlocks = new ArrayList<>();
  Stack<Block> blockStack = new Stack<>();
  Stack<Method> methodStack = new Stack<>();
  Class curClass = null;
  Method curMeth = null;
  Block curBlock = null;

  public ParserState(Parser p) {
    parser = p;
  }

  void markBeginOfImports() {
    beginOfImports = parser.t.charPos + parser.t.val.length();
  }

  void markEndOfSuperCall() {
    assert curClass != null && curMeth != null && curBlock.blockType == BlockType.CONSTRUCTOR;
    curBlock.endOfSuperCall = parser.t.charPos + parser.t.val.length();
  }

  void enterClass() {
    enterClass(false);
  }

  void enterClass(boolean anonymous) {
    if (curClass != null) {
      classStack.push(curClass);
    }
    if (curMeth != null) {
      methodStack.push(curMeth);
      curMeth = null;
    }
    String className = (anonymous) ? "Anonymous" : parser.la.val;
    String fullClassName = (!classStack.isEmpty()) ? classStack.peek().name + "." + className : className;
    curClass = new Class(fullClassName);
    if (anonymous) {
      curClass.classType = ClassType.ANONYMOUS;
    } else {
      curClass.classType = switch (parser.t.kind) {
        case _class -> ClassType.CLASS;
        case _interface -> ClassType.INTERFACE;
        case _enum -> ClassType.ENUM;
        default -> throw new RuntimeException(String.format("unknown class type '%s' discovered.\n", parser.t));
      };
    }
    allClasses.add(curClass);
    System.out.printf("entering class <%s>\n", curClass.name);
  }

  void leaveClass() {
    System.out.printf("left class <%s>\n", curClass.name);
    if (curClass.classType == ClassType.ANONYMOUS && !methodStack.empty()) {
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
    curClass.methods.add(curMeth);
    System.out.println("found method declaration of: " + curMeth.name);
  }

  void enterMainMethod() {
    enterMethod();
    curMeth.isMain = true;
    curClass.isMain = true;
    System.out.println("method is main entry point.");
  }

  void enterBlock(boolean isMethod) {  // no missing braces
    enterBlock(getBlockTypeByContext(isMethod));
  }

  void enterBlock(BlockType blockType) {
    assert curClass != null;
    if (curBlock != null) {
      blockStack.push(curBlock);
    }
    curBlock = new Block();
    curBlock.clazz = curClass;
    curBlock.method = curMeth;
    Token blockStartToken = blockType.hasNoBraces() ? parser.t : parser.la; // la == '{'
    curBlock.beg = blockStartToken.line;
    curBlock.begPos = blockStartToken.charPos + blockStartToken.val.length();
    curBlock.blockType = blockType;
    allBlocks.add(curBlock);
    if (curMeth != null) {
      curMeth.blocks.add(curBlock);
    } else {
      curClass.blocks.add(curBlock);
    }
    System.out.printf("entering %s\n", curBlock);
  }

  void leaveBlock(boolean isMethod) {
    curBlock.end = parser.t.line;
    curBlock.endPos = parser.t.charPos + parser.t.val.length();
    if (blockStack.empty()) {
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
    }
    if (isMethod) {
      System.out.println("left method: " + curMeth.name);
      curMeth = null;
    } else {
      System.out.printf("left %s\n", curBlock);
    }
  }

  void checkSingleStatement(boolean isAssignment, boolean isSwitch, boolean isArrowExpr) {
    if (parser.t.kind == _else && parser.la.kind == _if) {
      System.out.println("else if found. no block.");
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
    if (curClass.classType == ClassType.INTERFACE) return false;  // interface cannot have static block
    return parser.la.kind == _static && parser.scanner.Peek().kind == _lbrace;
  }

  boolean isLabel() {
    return parser.la.kind == _ident && parser.scanner.Peek().kind == _colon;
  }

  boolean isEntryPoint() {
    if (curClass.classType == ClassType.INTERFACE) {
      // the "public" can be omitted in interfaces (implied)
      return parser.la.kind == _static
          && parser.scanner.Peek().kind == _void
          && parser.scanner.Peek().kind == _main;
    } else {
      return parser.la.kind == _public
          && parser.scanner.Peek().kind == _static
          && parser.scanner.Peek().kind == _void
          && parser.scanner.Peek().kind == _main;
    }
  }

  boolean isAssignment() {
    return parser.t.kind == _equals;
  }
}
