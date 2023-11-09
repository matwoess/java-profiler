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
    if (blockType == BlockType.COLON_CASE || isSingleStatement) {
      return tokenEndPosition(parser.t);
    } else { // la == '{'
      return tokenStartPosition(parser.la);
    }
  }

  public static int getIncInsertOffset(Parser parser, BlockType blockType, boolean isSingleStatement) {
    if (!isSingleStatement && blockType != BlockType.COLON_CASE) { // la == '{'
      return endOfToken(parser.la) - getBlockBegPos(parser, blockType, isSingleStatement).pos();
    } else {
      return 0;
    }
  }

  static Token getRegionStartToken(Parser parser, BlockType blockType, boolean isSingleStatement) {
    parser.scanner.ResetPeek();
    if (blockType == BlockType.COLON_CASE || isSingleStatement) {
      return parser.la;
    } else { // la == '{'
      return parser.scanner.Peek();
    }
  }

}
