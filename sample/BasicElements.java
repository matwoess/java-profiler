public class BasicElements {
  static class MyClosable implements AutoCloseable {
    public void open() {
      System.out.println("Opening resource...");
    }

    public void close() throws Exception {
      System.out.println("closing resource...");
    }
  }

  public static void main(String[] args) {
    try (MyClosable resource = new MyClosable()) {
      resource.open();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
