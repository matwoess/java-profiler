import java.util.List;

public class SwitchPatternMatching {

  public static void main(String[] args) {
    Shape[] shapes = {
        new Shape.Rectangle(2, 3),
        new Shape.Rectangle(3, 3),
        new Shape.Square(4),
        new Shape.Circle(5),
        new Shape.Triangle(2, 3)
    };
    for (Shape shape : shapes) {
      printArea(shape);
    }
    List<Point<Unit>> points = List.of(
        new Point<>(new IntCoord(0), new IntCoord(0)),
        new Point<>(new IntCoord(1), new IntCoord(2)),
        new Point<>(new FloatCoord(1.0f), new FloatCoord(27.02134325f)),
        new Point<>(new IntCoord(3), new FloatCoord(2.5f)),
        new Point<>(new FloatCoord(-3f), new IntCoord(25))
    );
    for (Point<Unit> point : points) {
      printGenericPoint(point);
    }
    printObject("some string");
    printObject(.55d);
    printObject(-60);
    printObject(-7);
    printObject(points.getFirst());
    printObject(null);
    printObject(new Point<>(new IntCoord(5), new IntCoord(5)));
  }

  static void printArea(Shape shape) {
    double area = calculateArea(shape);
    System.out.println("Area: " + area);
  }

  static double calculateArea(Shape shape) {
    return switch (shape) {
      case Shape.Rectangle r when r.width() == r.height() -> Math.pow(r.width(), 2);
      case Shape.Rectangle r -> r.width() * r.height();
      case Shape.Square s -> {
        yield s.side() * s.side();
      }
      case Shape.Circle c -> Math.PI * c.radius() * c.radius();
      case Shape.Triangle t when t.getHeight() < 0 -> throw new RuntimeException("invalid triangle height");
      case Shape.Triangle t -> 0.5 * t.getBase() * t.getHeight();
    };
  }

  static void printGenericPoint(Point<Unit> p) {
    switch (p) {
      case Point<Unit>(IntCoord x, IntCoord y) when x.val() == 0 && y.val() == 0 -> System.out.println("origin");
      case Point(IntCoord x, IntCoord y) -> System.out.printf("iiPoint = (%d, %d)%n", x.val(), y.val());
      case Point(FloatCoord x, FloatCoord y) -> System.out.printf("ffPoint = (%.2f, %.2f)%n", x.val(), y.val());
      case Point(Unit x, IntCoord y) -> System.out.printf("xiPoint = (%s, %d)%n", x.toString(), y.val());
      case Point(Unit x, FloatCoord y) -> System.out.printf("xfPoint = (%s, %.2f)%n", x.toString(), y.val());
    }
  }

  private static void printObject(Object obj) {
    switch (obj) {
      case String s:
        System.out.println("String: " + s);
        break;
      case Integer i when i < 0 && i != Integer.MIN_VALUE && i % 2 != 0:
        if (i == -7) {
          System.out.println("Special Negative Odd Integer: " + i);
          break;
        }
        System.out.println("Negative Odd Integer: " + i);
        System.out.println("Absolute Value: " + Math.abs(i));
        break;
      case Integer i:
        System.out.println("Integer: " + i);
        break;
      case Float f: {
        System.out.println("Float: " + f);
        break;
      }
      case Double d:
        System.out.println("Double: " + d);
        break;
      case int[] ia:
        System.out.println("Array of int values of length" + ia.length);
        break;
      case Point(IntCoord x, IntCoord y) when x.val() == 0 && y.val() == 0:
        System.out.println("Origin Point");
        break;
      case null, default:
        System.out.println("Unknown type");
        break;
    }
  }
}

sealed interface Shape {
  record Rectangle(double width, double height) implements Shape {}
  record Square(double side) implements Shape {}
  record Circle(double radius) implements Shape {}
  final class Triangle implements Shape {
    private final double base;
    private final double height;
    public Triangle(double base, double height) {
      this.base = base;
      this.height = height;
    }
    public double getBase() {
      return base;
    }
    public double getHeight() {
      return height;
    }
  }
}

record Point<T>(T x, T y) {}
sealed interface Unit permits IntCoord, FloatCoord {}
record IntCoord(Integer val) implements Unit {}
record FloatCoord(Float val) implements Unit {}
