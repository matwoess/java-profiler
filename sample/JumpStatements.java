public class JumpStatements {
  public static void main(String[] args) {
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
}