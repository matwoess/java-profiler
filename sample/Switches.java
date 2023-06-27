public class Switches {
  enum StatusCode {
    OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND
  }
  
  static int globalInt = switch ("switch".hashCode()) {
    case 12345 -> 5;
    case 6789, 6660 -> 6;
    case Integer.MAX_VALUE -> 8;
    default -> {
      yield 8;
    }
  };

  public static void main(String[] args) {
    System.out.println(globalInt);
    String value = "aBcDe";
    switch (value.charAt(1)) {
      case 'a':
        System.out.println("is 'a'");
        break;
      case 'c', 'D',  'e':
        System.out.println("is 'c', 'D' or 'e'");
        break;
      case 'B': {
        System.out.println("is 'B'");
        break;
      }
      default:
        System.out.println("is 'e' or other char.");
        break;
    }

    switch (value) {
      case "aBcDe" -> {
        if (value.toUpperCase().equals("ABCDE")) {
          System.out.println("as expected");
        }
      }
      case "x", "other value" -> System.out.println("unexpected");
      case "" -> {
        break;
      }
      default -> throw new RuntimeException("should never happen");
    }

    int dependingOn = 5;
    int result1 = switch (dependingOn) {
      case 5: {
        yield 2;
      }
      case 1: yield 5;
      case 2: throw new RuntimeException();
      default: {
        if (dependingOn < 10) {
          System.out.println("none of the above");
          yield 0;
        } else {
          yield -1;
        }
      }
    };

    dependingOn = 7;
    int result2 = switch (dependingOn) {
      case 5 -> {
        yield 2;
      }
      case 1 -> 5;
      case 2, 3 -> throw new RuntimeException();
      default -> {
        if (dependingOn < 10) {
          System.out.println("none of the above");
          yield 0;
        } else {
          yield -1;
        }
      }
    };
    System.out.println("result1=" + result1);
    System.out.println("result2=" + result2);
    System.out.println(getStatusCodeDescription(StatusCode.FORBIDDEN));
  }

  public static String getStatusCodeDescription(StatusCode code) {
    return switch(code) {
      case OK -> "everything went great";
      case NOTFOUND, FORBIDDEN -> "cannot access";
      case UNAUTHORIZED -> {
        yield "did you forget to enter your password?";
      }
    };
  }
}
