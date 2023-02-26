import java.io.Serializable;

public interface Interfaces extends Serializable {
  int x = 0;

  int[] arr = new int[]{0};

  int get();

  default void printGreeting() {
    System.out.println("Hello!");
  }

  static void increment() {
    arr[0]++;
  }
  public interface SubInterface extends Interfaces {
    @Override
    default public int get() {
      printGreeting();
      increment();
      return SubClass.getXPlus1() + arr[0];
    }

    class SubClass {
      static int getXPlus1() {
        return x + 1;
      }
    }
  }

  class X implements SubInterface { }

  static void main(String[] args) {
    SubInterface x = new X();
    int result = x.get();
    System.out.println(result);
  }
}
