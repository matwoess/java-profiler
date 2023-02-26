import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Interfaces extends Serializable {
  int x = 0;

  int[] ints = new int[]{0, 1, 3};
  List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
  String[] strings = new String[]{
      String.format("%d", ints[1]),
      floats.get(1).toString(),
      "ASDF",
  };

  int get();

  default void printGreeting() {
    System.out.println("Hello!");
  }

  static void increment() {
    ints[0]++;
  }

  public interface SubInterface extends Interfaces {
    @Override
    default public int get() {
      printGreeting();
      increment();
      return SubClass.getXPlus1() + ints[0];
    }

    class SubClass {
      static int getXPlus1() {
        return x + 1;
      }
    }
  }

  class X implements SubInterface {
    void callGet() {
      get();
    }
  }

  static void main(String[] args) {
    SubInterface x = new X();
    int result = x.get();
    System.out.println(result);
  }
}
