package instrument;

import org.junit.jupiter.api.Test;

import common.Block;
import common.Method;
import common.Class;

import java.util.ArrayList;
import java.util.List;

import static common.BlockType.METHOD;
import static instrument.Util.*;
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
    List<Block> blocks = getFoundBlocks(abstractClass);
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("InitBlocks");
    Method meth = new Method("doNothing");
    Block expectedBlock = getBlock(METHOD, clazz, meth, 9, 9, 275, 276);
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("DefaultStatic");
    Method meth = new Method("getName");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 3, 5, 67, 91));
    meth = new Method("getAge");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 6, 8, 133, 160));
    meth = new Method("printInfo");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 9, 11, 212, 266));
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Interfaces.SubInterface");
    Method meth = new Method("get");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 6, 8, 174, 225));
    clazz = new Class("Interfaces.SubInterface.SubClass");
    meth = new Method("getXPlus1");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 11, 13, 278, 308));
    clazz = new Class("Interfaces.X");
    meth = new Method("callGet");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 18, 20, 376, 395));
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("WithMain.X", false);
    Method meth = new Method("get");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 6, 8, 110, 132));
    clazz = new Class("WithMain", true);
    meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 11, 15, 180, 268));
    assertIterableEquals(expectedBlocks, blocks);
    // without public
    fileContent = """
        public interface InferredPublic {
          static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    clazz = new Class("InferredPublic", true);
    meth = new Method("main", true);
    Block expectedBlock = getBlock(METHOD, clazz, meth, 2, 6, 69, 157);
    assertEquals(expectedBlock, blocks.get(0));
  }
}
