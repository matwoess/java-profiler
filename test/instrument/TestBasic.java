package instrument;

import model.Block;
import model.JavaFile;
import model.Method;
import model.Class;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.ProgramBuilder.*;
import static model.BlockType.*;
import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestBasic {
  @Test
  public void TestEmptyProgram() {
    String fileContent = """
        class Empty {
        }""";
    JavaFile expected = jFile(
        jClass("Empty")
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestBeginOfImportsAndPackageName_NoPackage() {
    String fileContent = """
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    JavaFile expected = jFile("<default>", 0, jClass("Empty"));
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestBeginOfImportsAndPackageName() {
    String fileContent = """
        package name.Of._the_.pkg ;
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
          void meth() {
          }
          class InEmpty {
            void innerMeth() {
            }
          }
        }""";
    int lengthOfPackageDeclaration = "package name.Of._the_.pkg ;".length();
    JavaFile expected = jFile("name.Of._the_.pkg", lengthOfPackageDeclaration,
        jClass("Empty",
            jMethod("meth", false, 5, 6, 122, 126),
            jClass("InEmpty",
                jMethod("innerMeth",
                    jBlock(METHOD, 8, 9, 167, 173)
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
    Class tlClass = expected.topLevelClasses.get(0);
    Class innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("name.Of._the_.pkg.Empty", tlClass.getFullName());
    assertEquals("Empty", tlClass.getName());
    assertEquals("name.Of._the_.pkg.Empty$InEmpty", innerClass.getFullName());
    assertEquals("Empty$InEmpty", innerClass.getName());
  }

  @Test
  public void TestHelloWorld() {
    String fileContent = """
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello World!");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("HelloWorld", true,
            jMethod("main", true, 2, 4, 68, 112)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestStaticBlock() {
    String fileContent = """
        public class Static {
          static int x;
          
          static {
            x = 0;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Static",
            jBlock(STATIC, 4, 6, 49, 64)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 16, 62, 244,
                jBlock(BLOCK, 5, 13, 136, 211),
                jBlock(BLOCK, 7, 9, 165, 178),
                jBlock(BLOCK, 10, 12, 194, 209)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 12, 62, 188,
                jBlock(BLOCK, 4, 9, 95, 153),
                jBlock(BLOCK, 6, 8, 113, 129)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 15, 62, 336,
                jBlock(BLOCK, 4, 6, 84, 99),
                jBlock(BLOCK, 6, 8, 132, 185),
                jBlock(BLOCK, 8, 10, 215, 279),
                jBlock(BLOCK, 10, 12, 289, 301)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestTryWithResourceStatement() {
    String fileContent = """
        public class TryWithResource {
          static class MyClosable implements AutoCloseable {
            public void open() {
              System.out.println("Opening resource...");
            }
          
            public void close() throws Exception {
              System.out.println("closing resource...");
            }
          }
          
          public static void main(String[] args) {
            try (MyClosable resource = new MyClosable()) {
              resource.open();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("TryWithResource", true,
            jClass("MyClosable",
                jMethod("open",
                    jBlock(METHOD, 3, 5, 108, 163)
                ),
                jMethod("close",
                    jBlock(METHOD, 7, 9, 207, 262)
                )
            ),
            jMethod("main", true, 12, 18, 310, 459,
                jBlock(BLOCK, 13, 15, 361, 390),
                jBlock(BLOCK, 15, 17, 412, 455)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 10, 62, 284),
            jMethod("getTempString", false, 18, 20, 625, 680)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestStatementBeginningWithStringLiteral() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        "Some string.".split(" ");
         """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 6, 62, 117)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestEscapedCharLiterals() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        char c = '\\"'; c = '\\'';
        c = '\\n'; c = '\\r'; c = '\\t';
        c = '\\\\';
        c = '\\b'; c = '\\s'; c = '\\f';
        c = '\\0'; c = '\\1'; c = '\\2'; c = '\\3';
        c = '\\6'; c = '\\67';
        c = '\\uFF1A'; c = '\\uuu231A';
        c = '\\064'; c = '\\377';
         """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 13, 62, 300)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestStringLiteralWithEscapedCharacters() {
    String fileContent = String.format(baseTemplate, """
        String s = "''''\\"\\"\\"\\r\\n\\t\\"\\f\\b\\s_asdf";
        s = "\\u42FA_\\uuuADA1_\\1_\\155adsf\\6_\\43_Text";
         """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 6, 62, 161)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMethodWithDoubleBackslashInString() {
    String fileContent = String.format(baseTemplate, """
        boolean b = "Text\\\\".endsWith("\\\\");
        if (b) {
          System.out.println("does end with \\\\");
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 8, 62, 161,
                jBlock(BLOCK, 4, 6, 112, 156)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestTernaryOperatorInReturn() {
    String fileContent = String.format(baseTemplate, """
        if (false) {
          return 0;
        }
        return false ? -1 : 0;
        """, """
        public void doNothing() {}
        """);
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 8, 62, 121,
                jBlock(BLOCK, 3, 5, 79, 93)
            ),
            jMethod("doNothing", false, 9, 9, 150, 151)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 9, 62, 227)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
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
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 16, 62, 220,
                jBlock(BLOCK, 5, 14, 99, 215),
                jBlock(BLOCK, 6, 13, 123, 213),
                jBlock(BLOCK, 7, 10, 141, 177),
                jBlock(BLOCK, 10, 12, 184, 209)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestIncrementAndDecrement() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        while (true) {
          if (x == 1) {
            --x;
            ++x;
            ++(x);
          } else {
            break;
          }
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true, 2, 14, 62, 170,
                jBlock(BLOCK, 4, 12, 92, 165),
                jBlock(BLOCK, 5, 9, 108, 141),
                jBlock(BLOCK, 9, 11, 148, 163)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
