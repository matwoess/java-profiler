public class Simple {
  public static void main(String[] args) {
    String output = "finished";
    int x = 0;
    for (int i = 0; i < 10; i++) {
      x = 0;
      if (i % 2 == 0) {
        x++;
      }
      if (x > 10) {
        break;
      }
    }

    System.out.println(output);
  }
}