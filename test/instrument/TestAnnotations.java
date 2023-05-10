package instrument;

import common.Block;
import common.Class;
import common.Method;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static common.BlockType.METHOD;
import static instrument.Util.getBlock;
import static instrument.Util.getFoundBlocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestAnnotations {
  @Test
  public void TestClassAnnotations() {
    String fileContent = """
        @SuppressWarnings("unchecked")
        @Deprecated
        public class Annotations {
          public static void main(String[] args) {
          }
        }
        """;
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Annotations", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 4, 5, 114, 118));
    assertIterableEquals(expectedBlocks, blocks);
   }

  @Test
  public void TestMethodAnnotations() {
    String fileContent = """
        public class Annotations {
          @Deprecated
          @SuppressWarnings({"unused", "unchecked"})
          public static void main(String[] args) {
          }
        }
        """;
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Annotations", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 4, 5, 128, 132));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestArgumentAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(@SuppressWarnings("null") String[] args) {
          }
        }
        """;
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Annotations", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 3, 95, 99));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestStatementAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(String[] args) {
            @SuppressWarnings("ParseError")
            Integer i1 = Integer.parseInt("asdf");
            @SuppressWarnings("unused")
            String s = "a";
          }
        }
        """;
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Annotations", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 7, 69, 204));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
