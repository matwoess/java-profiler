package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestBasic {

  public String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
         %s
      }
      """;

  @Test
  public void TestEmptyProgram() {
    String emptyClass = """
        class Empty {
        }""";
    List<Parser.Block> blocks = getFoundBlocks(emptyClass);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestHelloWorld() {
    String helloWorld = """
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello World!");
          }
        }
        """;
    List<Parser.Block> blocks = getFoundBlocks(helloWorld);
    assertEquals(1, blocks.size());
    Parser.Class clazz = new Parser.Class("HelloWorld", true);
    Parser.Method meth = new Parser.Method("main", true);
    Parser.Block expectedBlock = getMethodBlock(clazz, meth, 2, 4, 68, 112);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestStaticBlock() {
    String staticBlock = """
        public class Static {
          static int x;
          
          static {
            x = 0;
          }
        }
        """;
    List<Parser.Block> blocks = getFoundBlocks(staticBlock);
    assertEquals(1, blocks.size());
    Parser.Class clazz = new Parser.Class("Static");
    Parser.Method meth = new Parser.Method("static");
    Parser.Block expectedBlock = getBlock(clazz, meth, 4, 6, 49, 64);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestForLoopWithIfs() {
    String fileContent = String.format(baseTemplate, """
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
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 16, 62, 244));
    expectedBlocks.add(getBlock(clazz, meth, 5, 13, 136, 211));
    expectedBlocks.add(getBlock(clazz, meth, 7, 9, 165, 178));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 194, 209));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestWhileAndDoWhileLoop() {
    String fileContent = String.format(baseTemplate, """
        int x = 100;
        while (x > 0) {
          x -= 10;
          do {
            x += 3;
          } while ((x % 2) != 0);
        }
        System.out.println("x=" + x);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 12, 62, 188));
    expectedBlocks.add(getBlock(clazz, meth, 4, 9, 95, 153));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 113, 129));
    assertIterableEquals(expectedBlocks, blocks);
  }


  @Test
  public void TestTryCatchFinally() {
    String fileContent = String.format(baseTemplate, """
        int x = 50;
        try {
          x = x / 0;
        } catch (ArithmeticException ex) {
          System.out.println("Error: " + ex.getMessage());
        } catch (RuntimeException ex) {
          System.out.println("Unexpected error: " + ex.getMessage());
        } finally {
          x /= 2;
        }
        System.out.println("x=" + x);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 15, 62, 336));
    expectedBlocks.add(getBlock(clazz, meth, 4, 6, 84, 99));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 132, 185));
    expectedBlocks.add(getBlock(clazz, meth, 8, 10, 215, 279));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 289, 301));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
