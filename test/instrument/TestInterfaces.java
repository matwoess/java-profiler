package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.getFoundBlocks;
import static instrument.Util.getMethodBlock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestInterfaces {
  @Test
  public void TestAbstractMethodsNoBlock() {
    String abstractClass = """
        interface AbstractMethods {
          String getName();
          public int getAge(int userId);
          abstract void printInfo(String userName, int age);
        }""";
    List<Parser.Block> blocks = getFoundBlocks(abstractClass);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestMembersWithInitBlocks() {
    String fileContent = """
        interface InitBlocks {
          int[] ints = new int[]{0, 1, 3};
          List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
          String[] strings = new String[]{
              String.format("%d", ints[1]),
              floats.get(1).toString(),
              "ASDF",
          };
          default void doNothing() {}
        }""";
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Parser.Class clazz = new Parser.Class("InitBlocks");
    Parser.Method meth = new Parser.Method("doNothing");
    Parser.Block expectedBlock = getMethodBlock(clazz, meth, 9, 9, 275, 276);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestDefaultAndStaticMethodsWithBlock() {
    String fileContent = """
        interface DefaultStatic {
          int x = 0;
          default String getName() {
            return "empty";
          }
          default public int getAge(int userId) {
            return userId / 2;
          }
          static void printInfo(String userName, int age) {
            System.out.printf("%s: %d\\n", userName, age);
          }
        }""";
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("DefaultStatic");
    Parser.Method meth = new Parser.Method("getName");
    expectedBlocks.add(getMethodBlock(clazz, meth, 3, 5, 67, 91));
    meth = new Parser.Method("getAge");
    expectedBlocks.add(getMethodBlock(clazz, meth, 6, 8, 133, 160));
    meth = new Parser.Method("printInfo");
    expectedBlocks.add(getMethodBlock(clazz, meth, 9, 11, 212, 266));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestSubInterfacesAndClasses() {
    String fileContent = """
        public interface Interfaces extends Serializable {
          int x = 0;
          int get();
          public interface SubInterface extends Interfaces {
            @Override
            default public int get() {
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
        }""";
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Interfaces.SubInterface");
    Parser.Method meth = new Parser.Method("get");
    expectedBlocks.add(getMethodBlock(clazz, meth, 6, 8, 174, 225));
    clazz = new Parser.Class("Interfaces.SubInterface.SubClass");
    meth = new Parser.Method("getXPlus1");
    expectedBlocks.add(getMethodBlock(clazz, meth, 11, 13, 278, 308));
    clazz = new Parser.Class("Interfaces.X");
    meth = new Parser.Method("callGet");
    expectedBlocks.add(getMethodBlock(clazz, meth, 18, 20, 376, 395));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestMainEntryPoint() {
    String fileContent = """
        public interface WithMain {
          int get();
          
          class X implements WithMain {
            @Override
            public int get() {
              return 0;
            }
          }
          
          public static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("WithMain.X", false);
    Parser.Method meth = new Parser.Method("get");
    expectedBlocks.add(getMethodBlock(clazz, meth, 6, 8, 110, 132));
    clazz = new Parser.Class("WithMain.X", true);
    meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 11, 15, 180, 268));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
