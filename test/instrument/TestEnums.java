package instrument;

import common.Block;
import common.Class;
import common.Method;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static common.BlockType.*;
import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestEnums {
  @Test
  public void TestEmptyEnum() {
    String fileContent = """
        enum Empty {
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestSimpleEnumWithoutMembers() {
    String fileContent = """
        enum Adjective {
          LOW, MEDIUM, HIGH,
          WEAK, STRONG, GREAT
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestEnumWithTrailingSemicolon() {
    String fileContent = """
        enum AB {
          A, B;
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(0, blocks.size());
  }


  @Test
  public void TestStaticBlock() {
    String fileContent = """
        enum AB {
          A, B;
          public static final long ID;
          static {
            ID = 1237877L;
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("AB");
    Block block = getBlock(STATIC, clazz, null, 4, 6, 59, 82);
    assertEquals(block, blocks.get(0));
  }


  @Test
  public void TestMemberFunction() {
    String fileContent = """
        enum Enum {
          VALUE1, VAL_2 ;
          public static final long ID;
          public String lowercase() {
            return this.name().toLowerCase();
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("Enum");
    Method meth = new Method("lowercase");
    Block block = getBlock(METHOD, clazz, meth, 4, 6, 90, 132);
    assertEquals(block, blocks.get(0));
  }

  @Test
  public void TestEnumWithConstructorAndArguments() {
    String fileContent = """
        public enum WithConstructor {
          PRIMARY(1, "OK", 1.5f),
          SECONDARY(-6634, "", 5e+4f),
          TERTIARY(5, "This is a test", .3f);
         
          private final int integerVal;
          private final String stringVal;
          public final float floatVal;
         
          WithConstructor(int val1, String str, float num) {
            this.integerVal = val1;
            this.stringVal = str;
            this.floatVal = num;
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    Class clazz = new Class("WithConstructor");
    Method meth = new Method("WithConstructor");
    Block block = getBlock(METHOD, clazz, meth, 10, 14, 276, 359);
    assertEquals(block, blocks.get(0));
  }

  @Test
  public void TestEnumMainFunctionAndSwitch() {
    String fileContent = """
        public enum WithMain {
          A,
          B,
          C;
          public static void main(String[] args) {
            WithMain wm = A;
            switch (wm) {
              case A:
                assert (e == A);
              default:
                System.out.println(wm.name());
            }
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    Class clazz = new Class("WithMain", true);
    Method meth = new Method("main", true);
    List<Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 5, 13, 80, 222));
    expectedBlocks.add(getBlock(SWITCH_CASE, clazz, meth, 8, 9, 133, 158));
    expectedBlocks.add(getBlock(SWITCH_CASE, clazz, meth, 10, 11, 173, 212));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestEnumWithSubClassAndSubInterface() {
    String fileContent = """
        public enum WithSubClassAndInterface {
          A,B,C;
          
          static class ClassInEnum implements InterfaceInEnum {
            public static void printName(WithSubClassAndInterface val) {
              System.out.println(val.name());
            }
          }
          
          private interface InterfaceInEnum {
            default String lowercase(WithSubClassAndInterface val) {
              return val.name().toLowerCase();
            }
          }
          
          public static void callMethods() {
            ClassInEnum.printName(WithSubClassAndInterface.B);
            new ClassInEnum().lowercase(C);
          }
        }""";
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    Class clazz = new Class("WithSubClassAndInterface.ClassInEnum");
    Method meth = new Method("printName");
    List<Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 5, 7, 169, 213));
    clazz = new Class("WithSubClassAndInterface.InterfaceInEnum");
    meth = new Method("lowercase");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 11, 13, 317, 362));
    clazz = new Class("WithSubClassAndInterface");
    meth = new Method("callMethods");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 16, 19, 404, 499));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
