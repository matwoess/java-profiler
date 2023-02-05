package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.getExpectedBlock;
import static instrument.Util.getFoundBlocks;
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
    Parser.Block expectedBlock = getExpectedBlock("HelloWorld", "main", 2, 4, true);
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
    Parser.Block expectedBlock = getExpectedBlock("Static", "static", 4, 6, false);
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
    expectedBlocks.add(getExpectedBlock("Main", "main", 2, 16, true));
    expectedBlocks.add(getExpectedBlock("Main", "main", 5, 13, false));
    expectedBlocks.add(getExpectedBlock("Main", "main", 7, 9, false));
    expectedBlocks.add(getExpectedBlock("Main", "main", 10, 12, false));
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
    expectedBlocks.add(getExpectedBlock("Main", "main", 2, 12, true));
    expectedBlocks.add(getExpectedBlock("Main", "main", 4, 9, false));
    expectedBlocks.add(getExpectedBlock("Main", "main", 6, 8, false));
    assertIterableEquals(expectedBlocks, blocks);
  }


  @Test
  public void TestTryCatchFinally() {
    String fileContent = String.format(baseTemplate, """
        int x = 50;
        try {
          x = x / 0;
        } catch (ArithmeticException ex) {
          System.out.println("ERROR: " + ex.getMessage());
        } finally {
          x /= 2;
        }
        System.out.println("x=" + x);
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getExpectedBlock("Main", "main", 2, 13, true));
    expectedBlocks.add(getExpectedBlock("Main", "main", 4, 6, false));
    expectedBlocks.add(getExpectedBlock("Main", "main", 6, 8, false));
    expectedBlocks.add(getExpectedBlock("Main", "main", 8, 10, false));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
