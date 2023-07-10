public record Records(int x, int y) {
  static int z;
  
  public static void main(String[] args) {
    Rectange r1 = new Rectange(5, 7);
    Rectange r2 = new Rectange("2.8", "4.33e42");
    Rectange square = Rectange.getSquare(25);
    System.out.println(square);
    System.out.println(square.getArea() + r2.getPerimeter() + r1.width() * r2.length());
    Records records = new Records((int)square.width(), (int)square.length());
    records.x();
    System.out.println(records.y + z);
    System.out.println("Manhattan distance: " + records.manhattanDistanceTo(new Records(5, 7)));
  }

  @Override
  public int x() {
    System.out.println("x was accessed with a value of: " + x);
    return x;
  }

  static {
    z = 5000;
  }
  
  int manhattanDistanceTo(Records other) {
    return Math.abs(x - other.x()) + Math.abs(y - other.y());
  }
}

record Rectange(double length, double width) {

  Rectange {
    if (length < 0 || width < 0) {
      throw new IllegalArgumentException(String.format("Invalid dimensions: %f, %f", length, width));
    }
  }
  Rectange(String length, String width) {
    this(Double.parseDouble(length), Double.parseDouble(width));
  }
  double getPerimeter() {
    return 2 * length + 2 * width;
  }
  
  double getArea() {
    return this.length() * this.width();
  }
  
  public static Rectange getSquare(double width) {
    return new Rectange(width, width);
  }
}


