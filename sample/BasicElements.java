public class BasicElements {
  static class MyClosable implements AutoCloseable {
    public void open() {
      System.out.println("Opening resource...");
    }

    public void close() throws Exception {
      System.out.println("closing resource...");
    }
  }

  static {
    System.out.println("in static block");
  }

  /**
   * Main function
   * @param args the arguments
   */
  public static void main(String[] args) {
    try (MyClosable resource = new MyClosable()) {
      resource.open();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    int x = 1;
    while (true) {
      if (x == 1) {
        ++x;
        ++(x);
      } else {
        break;
      }
    }
    outer: for (;;) {
      do {
        x--;
        --x;
        if (x == 1) {
          break outer;
        }
      } while (x > 0);
    }
    int y = (x == 1) ? 0 : 5;
    // with trailing double-slash
    String s = String.format("y has the value: %s\\", y);
    /* using a try catch with finally block */
    try {
      System.out.println(s);
    } catch (RuntimeException ex) {
      System.out.println(ex.toString());
      throw ex;
    } finally {
      System.out.println("leaving try-catch-finally");
    }
    "Hello World".chars().sum();
    assert x == 1;
    assert "Hello".chars().asDoubleStream().map(d -> d + 2).sum() > 0 : "is 0 or less";
  }

  String multiLineString = """
      Text
      With
      Multiple
      lines!
      """;

  public String getMultiLineString() {
    return multiLineString;
  }
}
