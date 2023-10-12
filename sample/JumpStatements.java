import java.util.function.Function;
import java.util.stream.Stream;

public class JumpStatements {
  public static void main(String[] args) {
    loopWithLabels();
    if (singleStmtIfsInLoop()) return;
    lambdasWithReturn();
    switchesWithBreaksAndReturns("param");
  }

  private static void loopWithLabels() {
    int counter = 0;
    loop1: while (true) {
      loop2: while (true) {
        loop3: while (true) {
          counter++;
          if (counter <= 5) {
            continue;
          }
          counter++;
          if (counter == 5_000_000) {
            if (true) {
              break;
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
      }
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
}