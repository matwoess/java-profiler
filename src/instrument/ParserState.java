package instrument;

import common.*;
import common.Class;

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
  Class curClass = null;
  Method curMeth = null;
  Block curBlock = null;
  boolean inAssignment = false;
  boolean inArrowExpression = false;
  boolean inSwitch = false;

  public ParserState(Parser p) {
    parser = p;
  }

  void markBeginOfImports() {
    beginOfImports = parser.t.charPos + parser.t.val.length();
  }

  void enterClass() {
    if (curClass != null) {
      classStack.push(curClass);
    }
    String className = (!classStack.isEmpty()) ? classStack.peek().name + "." + parser.la.val : parser.la.val;
    curClass = new Class(className);
    curClass.classType = switch (parser.t.kind) {
      case _class -> ClassType.CLASS;
      case _interface -> ClassType.INTERFACE;
      case _enum -> ClassType.ENUM;
      default -> throw new RuntimeException(String.format("unknown class type '%s' discovered.\n", parser.t));
    };
    allClasses.add(curClass);
    System.out.printf("entering class <%s>\n", curClass.name);
  }

  void leaveClass() {
    System.out.printf("left class <%s>\n", curClass.name);
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

  void leaveMethod() {
    curBlock.blockType = BlockType.METHOD;
    System.out.println("left method: " + curMeth.name);
    curMeth = null;
  }

  void enterBlock() {  // no missing braces
    enterBlock(getBlockTypeByContext(false));
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
    if (blockType.hasNoBraces() && inAssignment) {
      blockType = BlockType.SS_SWITCH_ARROW_CASE;
    }
    curBlock.blockType = blockType;
    allBlocks.add(curBlock);
    if (curMeth != null) {
      curMeth.blocks.add(curBlock);
    } else {
      curClass.blocks.add(curBlock);
    }
    System.out.printf("entering %s\n", curBlock);
  }

  void leaveBlock() {
    curBlock.end = parser.t.line;
    curBlock.endPos = parser.t.charPos + parser.t.val.length();
    System.out.printf("left %s\n", curBlock);
    if (blockStack.empty()) {
      if (curMeth != null) {
        leaveMethod();
      }
      curBlock = null;
    } else {
      curBlock = blockStack.pop();
    }
  }

  void checkSingleStatement() {
    if (parser.t.kind == _else && parser.la.kind == _if) {
      System.out.println("else if found. no block.");
      return;
    }
    if (parser.la.kind != _lbrace) {
      enterBlock(getBlockTypeByContext(true));
    }
  }

  void leaveSingleStatement() {
    if (curBlock.blockType.hasNoBraces()) {
      leaveBlock();
    }
  }

  BlockType getBlockTypeByContext(boolean missingBraces) {
    BlockType blockType;
    if (missingBraces) {
      if (inArrowExpression) {
        if (inSwitch) {
          blockType = BlockType.SS_SWITCH_ARROW_CASE;
        } else {
          blockType = BlockType.SS_LAMBDA;
        }
      } else if (inSwitch) {
          blockType = BlockType.SWITCH_CASE;
      } else {
        blockType = BlockType.SS_BLOCK;
      }
    }
    else {
      if (curMeth == null && parser.t.kind == _static && parser.la.kind == _lbrace) {
        blockType = BlockType.STATIC;
      } else if (inSwitch) {
        if (inArrowExpression) {
          blockType = BlockType.SWITCH_ARROW_CASE;
        } else {
          blockType = BlockType.SWITCH_CASE;
        }
      } else if (inArrowExpression) {
        blockType = BlockType.LAMBDA;
      } else {
        blockType = BlockType.BLOCK;
      }
    }
    return blockType;
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

  void checkAssignment() {
    inAssignment = parser.t.kind == _equals;
    if (inAssignment) {
      System.out.println("entered assignment expression.");
    }
  }

  void leaveAssignment() {
    if (inAssignment) {
      inAssignment = false;
      System.out.println("left assignment expression.");
    }
  }

  void enterSwitch() {
    inSwitch = true;
  }

  void leaveSwitch() {
    inSwitch = false;
  }

  void enterArrowExpression() {
    inArrowExpression = true;
  }

  void leaveArrowExpression() {
    inArrowExpression = false;
  }
}
