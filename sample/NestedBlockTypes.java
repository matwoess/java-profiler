import java.util.Comparator;
import java.util.List;

class NestedBlockTypes {
  static int a;

  static {
    if (5 % 2 == 1) switch ("this") {
      case "thus" -> a = 1;
      case "this" -> {
        a = 2;
      }
    }
    else {
      a = "this".chars().map(i -> i + 1).sum();
    }
  }

  public NestedBlockTypes() {
    super();
    if (a == 2) {
      System.out.println("2");
    } else System.out.println("other");
  }

  public void method() {
    String x = "12345";
    if (x != null) {
      for (int charVal : x.chars().toArray())
        switch (charVal) {
          case 1:
            System.out.println(1);
          case 2:
            x.chars().map(ch -> {
              return switch (ch) {
                case 0 -> {
                  yield 0;
                }
                case 1 -> 1 + 5;
                case 2 -> x.chars().map(c -> c + 1).filter(c -> {
                  return c < 10;
                }).sum();
                case 3 -> {
                  if (ch == 3) yield 3;
                  else yield -1;
                }
                case 4 -> {
                  yield switch (ch) {
                    default -> ch;
                  };
                }
                default -> -1;
              };
            }).forEach(i -> System.out.println(i));
          case 3:
            List<Integer> numbers = List.of(1, 2, 3, 4);
            numbers.sort(new Comparator<Integer>() {
              @Override
              public int compare(Integer i1, Integer i2) {
                int x = switch (i1) {
                  case 0 -> 0;
                  case -1 -> {
                    yield -1;
                  }
                  default -> i1 - i2;
                };
                return x;
              }
            });
            if (numbers.size() > 5) System.out.println(numbers.get(0));
          default:
            System.out.println("other value");
        }
    }
    if (x.equals("abc")) {
      ;
    }
  }

  public static void main(String[] args) {
    new NestedBlockTypes().method();
  }
}