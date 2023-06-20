public class Switches {
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
    int result = switch (dependingOn) {
      case 5 -> {
        yield 2;
      }
      case 1 -> 5;
      case 2 -> throw new RuntimeException();
      default -> {
        if (dependingOn < 10) {
          System.out.println("none of the above");
          yield 0;
        } else {
          yield -1;
        }
      }
    };
    System.out.println("result=" + result);
    printObject(result);
    System.out.println(formatObject(result));
  }

  public static void printObject(Object o) {
    switch (o) {
      case String s && s.length() < 10  && s.startsWith("//"):
        System.out.printf("A short comment: %s", s);
        break;
      case Integer i, null:
        System.out.println("It was some int or null");
      break;
      case Float f: {
        System.out.printf("a float with value: %f", f);
        break;
      }
      case default:
        System.out.println("Null or some other object");
    };
  }

  public static String formatObject(Object o) {
    return switch (o) {
      case String s && s.length() < 10  && s.startsWith("//")-> String.format("A short comment: %s", s);
      case Integer i -> "It was some int";
      case Float f -> {
        yield String.format("a float with value: %f", f);
      }
      case null, default -> "Null or some other object";
    };
  }
}
