package tool.instrument;

import tool.model.BlockType;
import tool.model.CodePosition;

public class Util {
  static int startOfToken(Token token) {
    return token.charPos;
  }

  static int endOfToken(Token token) {
    return token.charPos + token.val.length();
  }

  static CodePosition tokenStartPosition(Token token) {
    return new CodePosition(token.line, startOfToken(token));
  }

  static CodePosition tokenEndPosition(Token token) {
    return new CodePosition(token.line, endOfToken(token));
  }

  public static CodePosition getBlockBegPos(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (parser.t.val.equals(":") && (blockType == BlockType.SWITCH_CASE || blockType == BlockType.SWITCH_EXPR_CASE)) {
      return tokenEndPosition(parser.t);
    }
    if (isSingleStatement) {
      return tokenEndPosition(parser.t);
    } else { // la == '{'
      return tokenStartPosition(parser.la);
    }
  }

  public static int getIncInsertPos(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (parser.t.val.equals(":") && (blockType == BlockType.SWITCH_CASE || blockType == BlockType.SWITCH_EXPR_CASE)) {
      return 0;
    }
    if (!isSingleStatement) { // la == '{'
      return endOfToken(parser.la);
    }
    return 0;
  }

  public static CodePosition getRegionStartPos(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (parser.t.val.equals(":") && (blockType == BlockType.SWITCH_CASE || blockType == BlockType.SWITCH_EXPR_CASE)) {
      return tokenStartPosition(parser.la);
    }
    if (isSingleStatement) {
      return tokenStartPosition(parser.la);
    } else { // la == '{'
      return tokenStartPosition(parser.scanner.Peek());
    }
  }
}
