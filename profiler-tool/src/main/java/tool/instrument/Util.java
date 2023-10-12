package tool.instrument;

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
}
