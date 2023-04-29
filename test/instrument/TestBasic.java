package instrument;

import common.Block;
import common.Method;
import common.Class;
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
    List<Block> blocks = getFoundBlocks(emptyClass);
    assertEquals(0, blocks.size());
  }

  @Test
  public void TestBeginOfImports() {
    String withoutPackage = """
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    int beginOfImports = getBeginOfImports(withoutPackage);
    assertEquals(0, beginOfImports);
    String withPackage = """
        package name.of._the_.package ;
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    beginOfImports = getBeginOfImports(withPackage);
    int lengthOfPackageDeclaration = "package name.of._the_.package ;".length();
    assertEquals(lengthOfPackageDeclaration, beginOfImports);
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
    List<Block> blocks = getFoundBlocks(helloWorld);
    assertEquals(1, blocks.size());
    Class clazz = new Class("HelloWorld", true);
    Method meth = new Method("main", true);
    Block expectedBlock = getMethodBlock(clazz, meth, 2, 4, 68, 112);
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
    List<Block> blocks = getFoundBlocks(staticBlock);
    assertEquals(1, blocks.size());
    Class clazz = new Class("Static");
    Block expectedBlock = getStaticBlock(clazz, 4, 6, 49, 64);
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
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
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 15, 62, 336));
    expectedBlocks.add(getBlock(clazz, meth, 4, 6, 84, 99));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 132, 185));
    expectedBlocks.add(getBlock(clazz, meth, 8, 10, 215, 279));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 289, 301));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestComments() {
    String fileContent = String.format(baseTemplate, """
        // Testing comments
        /* begin comment /* no nested multi-line comments
        // but single line ones possible
        // Block comments start with "/*", end with "*" followed by "/".
        end comments */
        String s = getTempString(1);
         """, """
        /**
        This is a docstring:<br/>
        Method returns a string containing a given number.<br>
        Is called by the {@link #main(String[]) main} method in class {@link Main Main}.
        @param number the number which should be contained in the returned string.
        @returns a new string containing the number.
        */
       static String getTempString(int number) {
         return String.format("The number was %d", number);
       }
        """);
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 10, 62, 284));
    meth = new Method("getTempString", false);
    expectedBlocks.add(getMethodBlock(clazz, meth, 18, 20, 625, 680));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestStatementBeginningWithStringLiteral() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        "Some string.".split(" ");
         """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 6, 62, 117));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestCharLiterals() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        char c1 = '\\"';
        char c2 = '\\'';
        char c3 = '\\n';
        char c4 = '\\r';
        char c5 = '\\t';
         """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 10, 62, 170));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestStringLiteralWithEscapedCharacters() {
    String fileContent = String.format(baseTemplate, """
        String s = "''''\\"\\"\\"\\r\\n\\t\\"asdf";
         """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 5, 62, 108));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestTernaryOperator() {
    String fileContent = String.format(baseTemplate, """
        int number = 6;
        String msg = (number % 2 == 0)
            ? "Dividable by 2"
            : "Not dividable by 2";
        System.out.println(msg.contains("2") ? "2 appears" : "");
         """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(1, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 9, 62, 227));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestLabels() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        outer:
        while (true) {
          inner: while (true) {
            if (x == 1) {
              x++;
              break inner;
            } else {
              break outer;
            }
          }
        }
        """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 16, 62, 220));
    expectedBlocks.add(getBlock(clazz, meth, 5, 14, 99, 215));
    expectedBlocks.add(getBlock(clazz, meth, 6, 13, 123, 213));
    expectedBlocks.add(getBlock(clazz, meth, 7, 10, 141, 177));
    expectedBlocks.add(getBlock(clazz, meth, 10, 12, 184, 209));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
