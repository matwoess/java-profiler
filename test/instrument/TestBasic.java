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
    Parser.Block expectedBlock = getMethodBlock("HelloWorld", "main", 2, 4, 67, 111);
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
    Parser.Block expectedBlock = getBlock("Static", "static", 4, 6, 48, 63);
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 16, 61, 243));
    expectedBlocks.add(getBlock("Main", "main", 5, 13, 135, 210));
    expectedBlocks.add(getBlock("Main", "main", 7, 9, 164, 177));
    expectedBlocks.add(getBlock("Main", "main", 10, 12, 193, 208));
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 12, 61, 187));
    expectedBlocks.add(getBlock("Main", "main", 4, 9, 94, 152));
    expectedBlocks.add(getBlock("Main", "main", 6, 8, 112, 128));
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 15, 61, 335));
    expectedBlocks.add(getBlock("Main", "main", 4, 6, 83, 98));
    expectedBlocks.add(getBlock("Main", "main", 6, 8, 131, 184));
    expectedBlocks.add(getBlock("Main", "main", 8, 10, 214, 278));
    expectedBlocks.add(getBlock("Main", "main", 10, 12, 288, 300));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
