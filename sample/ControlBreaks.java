import java.util.function.Function;
import java.util.stream.Stream;

public class ControlBreaks {
  public static void main(String[] args) {
    loopWithLabels();
    if (singleStmtIfsInLoop()) return;
    lambdasWithReturn();
    switchesWithBreaksAndReturns("param");
    tryBlocksWithThrow();
    breakOutOfNonLoopBlocks();
    nFullRows();
  }

  private static void loopWithLabels() {
    int counter = 0;
    loop1:
    outerLoop:
    while (true) {
      loop2:
      while (true) {
        loop3:
        innerLoop:
        while (true) {
          counter++;
          if (counter <= 5) {
            continue;
          }
          counter++;
          if (counter == 5_000_000) {
            if (true) {
              break innerLoop;
            }
          }
          counter--;
          if (counter > 10 && counter <= 30) {
            counter += 5;
            break loop2;
          }
          counter += 0;
          if (counter > 30) {
            break loop1;
          }
          System.out.println("counting...");
        }
        System.out.println("loop 2");
      }
      System.out.println("looop 1");
    }
    System.out.println("finished with x=" + counter);
    if (counter == 35) {
      System.out.println("exit early...");
      return;
    }
    System.out.println("skipped due to return");
  }

  private static boolean singleStmtIfsInLoop() {
    for (int i = 0; i < 10; i++) {
      if (i < 5) continue;
      if (i > 20) if (i < 15) {
        return true;
      }
      i = i + 0;
    }
    return false;
  }

  private static void lambdasWithReturn() {
    Function<Integer, Boolean> isEven = (Integer x) -> {
      return x % 2 == 0;
    };
    if (isEven.apply(3)) {
      return;
    }
    int sum = Stream.of(1, 2, 5, 7).map(x -> {
      return x + 3;
    }).filter(x -> {
      while (true)
        if (x > 10) {
          break;
        } else x++;
      if (isEven.apply(x)) {
        return true;
      }
      return x - 1 == 5;
    }).mapToInt(x -> x * 2).sum();
    System.out.println("sum = " + sum);
  }

  private static void switchesWithBreaksAndReturns(String param) {
    if (param != null) {
      switch (param) {
        case "p":
          if (param.charAt(1) == 'e') {
            break;
          }
          System.out.println(param);
        case "par":
          if (param.length() == 3) {
            return;
          }
          System.out.println(param);

          param.toString();
        case "param": {
          if (param.hashCode() > 0) {
            break;
          }
        }
        System.out.println("another statement");
        default:
          break;
      }
      System.out.println("switch executed.");
    }
    System.out.println("returning now..");
  }

  private static void tryBlocksWithThrow() {
    Result result = null;
    for (int i = -1; i < 3; i++) {
      try {
        result = Result.fromInt(i);
        if (result == null) {
          return;
        }
      } catch (IllegalArgumentException e) {
        System.out.println("argument was invalid.");
      } catch (RuntimeException e) {
        System.out.println("other error: " + e.getMessage());
      } finally {
        System.out.println("finally");
      }
    }
    if (result != Result.OK) {
      try {
        result = Result.fromString("NOK");
        result = Result.fromString("0");
        if (result == Result.NOK) {
          throw new RuntimeException("NOK");
        }
        result = Result.fromString("asdf");
        if (result == null) throw new RuntimeException("error getting result.");
        result = Result.fromString("never reached");
      } catch (Exception e) {
        System.out.println("change result to NOK");
        result = Result.NOK;
      }
      if (result == null) {
        throw new RuntimeException("no result!");
      }
    }
    if (result != null) {
      // inner block should also depend on no result throw
      System.out.println(result);
    }
  }

  enum Result {
    OK, NOK;

    static Result fromInt(int code) {
      if (code >= 0) {
        try {
          if (code == 0) return OK;
          if (code == 1) return NOK;
          throw new IllegalArgumentException("undefined code " + code);
        } catch (Exception e) {
          System.out.println("the following error occurred: " + e.getMessage());
          throw e;
        }
      }
      throw new RuntimeException("invalid error code");
    }

    static Result fromString(String codeString) {
      int code = -1;
      try {
        code = Integer.parseInt(codeString);
      } catch (Exception e) {
        System.out.println("could not convert: " + e.getMessage());
      }
      System.out.println(code);
      Result result = null;
      if (code == -1) {
        try {
          result = Result.valueOf(codeString);
          if (code != -1) {
            throw new RuntimeException("something went wrong...");
          }
          code = 5;
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
      if (result != null) {
        return result;
      } else {
        System.out.println("invalid code");
        return null;
      }
    }
  }

  private static void breakOutOfNonLoopBlocks() {
    int i = 0;
    outer:
    {
      while (true) {
        inner:
        {
          i++;
          while (true) {
            if (i == 4) {
              System.out.println("i is " + i);
              break outer;
            } else if (i == 3) {
              System.out.println("i is " + i);
              break inner;
            }
            System.out.println("inc i");
            i++;
          }
        }
        System.out.println("i is still " + i);
      }
    }
    System.out.println("broke out of loops.");
    endif:
    if (i == 4) {
      if (i % 2 == 0) break endif;
      else System.out.println("strange..");
      System.out.println("didn't print this!");
    }
    System.out.println("done.");
  }

  private static void nFullRows() {
    char[][] board = {
        {'x', 'x', 'o', 'x'},
        {'x', 'x', 'x', 'x'},
        {' ', 'x', ' ', 'x'},
        {'o', 'o', 'x', 'o'}
    };
    int fullRows = 0, fullXRows = 0;
    loop1:
    outerLoop:
    for (int i = 0; i < board.length; i++) {
      boolean anyO = false;
      rowLoop:
      for (int j = 0; j < board[i].length; j++) {
        if (board[i][j] == ' ') {
          System.out.println("non-full row: " + i);
          continue outerLoop;
        }
        if (board[i][j] == 'x') {
          continue rowLoop;
        }
        anyO = true;
      }
      fullRows++;
      if (!anyO) {
        fullXRows++;
      }
    }
    System.out.println("number of full rows: " + fullRows);
    System.out.println("number of full 'x' rows: " + fullXRows);
  }
}