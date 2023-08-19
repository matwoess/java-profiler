package tool.instrument;

import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestInstrumentUtils.baseTemplate;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.*;

public class BasicElementsTest {
  @Test
  public void testEmptyClass() {
    String fileContent = """
        class Empty {
        }""";
    JavaFile expected = jFile(
        jClass("Empty")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testHelloWorld() {
    String fileContent = """
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello World!");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("HelloWorld",
            jMethod("main", 2, 4, 68, 112)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStaticBlock() {
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
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testForLoopWithIfs() {
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
        jClass("Main",
            jMethod("main", 2, 16, 62, 244,
                jBlock(BLOCK, 5, 13, 136, 211),
                jBlock(BLOCK, 7, 9, 165, 178),
                jBlock(BLOCK, 10, 12, 194, 209)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testWhileAndDoWhileLoop() {
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
        jClass("Main",
            jMethod("main", 2, 12, 62, 188,
                jBlock(BLOCK, 4, 9, 95, 153),
                jBlock(BLOCK, 6, 8, 113, 129)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTryCatchFinally() {
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
        jClass("Main",
            jMethod("main", 2, 15, 62, 336,
                jBlock(BLOCK, 4, 6, 84, 99),
                jBlock(BLOCK, 6, 8, 132, 185),
                jBlock(BLOCK, 8, 10, 215, 279),
                jBlock(BLOCK, 10, 12, 289, 301)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTryWithResourceStatement() {
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
        jClass("TryWithResource",
            jClass("MyClosable",
                jMethod("open", 3, 5, 108, 163),
                jMethod("close", 7, 9, 207, 262)
            ),
            jMethod("main", 12, 18, 310, 459,
                jBlock(BLOCK, 13, 15, 361, 390),
                jBlock(BLOCK, 15, 17, 412, 455)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testComments() {
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
        jClass("Main",
            jMethod("main", 2, 10, 62, 284),
            jMethod("getTempString", 18, 20, 625, 680)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStatementBeginningWithStringLiteral() {
    String fileContent = String.format(baseTemplate, """
        // ignoring result
        "Some string.".split(" ");
         """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 6, 62, 117)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEscapedCharLiterals() {
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
        jClass("Main",
            jMethod("main", 2, 13, 62, 300)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testStringLiteralWithEscapedCharacters() {
    String fileContent = String.format(baseTemplate, """
        String s = "''''\\"\\"\\"\\r\\n\\t\\"\\f\\b\\s_asdf";
        s = "\\u42FA_\\uuuADA1_\\1_\\155adsf\\6_\\43_Text";
         """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 6, 62, 161)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMethodWithDoubleBackslashInString() {
    String fileContent = String.format(baseTemplate, """
        boolean b = "Text\\\\".endsWith("\\\\");
        if (b) {
          System.out.println("does end with \\\\");
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 62, 161,
                jBlock(BLOCK, 4, 6, 112, 156)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocks() {
    String fileContent = """
        class TextBlocks {
          static String tBlock = ""\"
              Line1,
              Line2
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                ,
                Line3,
                Line4
                ""\";
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocks",
            jMethod("main", 6, 12, 126, 200),
            jMethod("getTextBlock", 13, 15, 240, 263)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksContainingStringsAndEscapeSequences() {
    String fileContent = """
        class TextBlocksAndSubStrings {
          static String tBlock = ""\"
              "Line1 containing character 'a'",
              code = "String s = "some text"";
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                "More text \t\034"
                ""\";
                if (tBlock.length() > 65) {
                  return;
                }
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    System.out.println(getBuilderCode(parseJavaFile(fileContent)));
    JavaFile expected = jFile(
        jClass("TextBlocksAndSubStrings",
            jMethod("main", 6, 13, 193, 315,
                jBlock(BLOCK, 10, 12, 283, 311)
            ),
            jMethod("getTextBlock", 14, 16, 355, 378)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTextBlocksContainingTextBlocks() {
    String fileContent = """
        class TextBlocksAndSubTextBlocks {
          static String tBlock = ""\"
              ""\\"
              "TextBlock",
              ""\\"
              ""\";
          public static void main(String[] args) {
            tBlock += ""\"
                ""\\"
                ""\\\\"
                "TextBlock",
                ""\\\\"
                ""\\"
                ""\";
          }
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;

    System.out.println(getBuilderCode(parseJavaFile(fileContent)));
    JavaFile expected = jFile(
        jClass("TextBlocksAndSubTextBlocks",
            jMethod("main", 7, 15, 158, 268),
            jMethod("getTextBlock", 16, 18, 308, 331)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTernaryOperatorInReturn() {
    String fileContent = String.format(baseTemplate, """
        if (false) {
          return 0;
        }
        return false ? -1 : 0;
        """, """
        public void doNothing() {}
        """);
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 62, 121,
                jBlock(BLOCK, 3, 5, 79, 93)
            ),
            jMethod("doNothing", 9, 9, 150, 151)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTernaryOperator() {
    String fileContent = String.format(baseTemplate, """
        int number = 6;
        String msg = (number % 2 == 0)
            ? "Dividable by 2"
            : "Not dividable by 2";
        System.out.println(msg.contains("2") ? "2 appears" : "");
         """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 62, 227)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLabels() {
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
        jClass("Main",
            jMethod("main", 2, 16, 62, 220,
                jBlock(BLOCK, 5, 14, 99, 215),
                jBlock(BLOCK, 6, 13, 123, 213),
                jBlock(BLOCK, 7, 10, 141, 177),
                jBlock(BLOCK, 10, 12, 184, 209)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIncrementAndDecrement() {
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
        jClass("Main",
            jMethod("main", 2, 14, 62, 170,
                jBlock(BLOCK, 4, 12, 92, 165),
                jBlock(BLOCK, 5, 9, 108, 141),
                jBlock(BLOCK, 9, 11, 148, 163)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testGenericVariableInstantiation() {
    String fileContent = """
        class GenericInstantiation {
          private Map<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();
          void fill() {
            map = new HashMap<String, Set<Integer>>()};
            map.put("Hello", Set.of(1, 2, 3));
            map.get("Hello");
          }
          public static void main(String[] args) {
            new GenericInstantiation().fill();
          }
        }""";
    JavaFile expected = jFile(
        jClass("GenericInstantiation",
            jMethod("fill", 3, 7, 123, 236),
            jMethod("main", 8, 10, 279, 322)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testAssertStatement() {
    String fileContent = baseTemplate.formatted("""
        int x = 1;
        assert x == 1;
        int sum = "Hello".chars().map(ch -> ch + 2).sum();
        assert sum > 0 : "sum is: " + sum;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 62, 183,
                jBlock(SS_LAMBDA, 5, 5, 128, 135)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testThrowWithClassCastAndInstanceOf() {
    String fileContent = baseTemplate.formatted("""
        try {
          int i = 5 / 0;
        } catch (Exception ex) {
          if (ex instanceof ArithmeticException) {
            throw (ArithmeticException) ex;
          } else if (ex instanceof ClassCastException classEx) {
            throw classEx;
          }
          throw new RuntimeException("Other exception: " + ex.getMessage());
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 14, 62, 349,
                jBlock(BLOCK, 3, 5, 72, 91),
                jBlock(BLOCK, 5, 12, 114, 344),
                jBlock(BLOCK, 6, 8, 157, 197),
                jBlock(BLOCK, 8, 10, 250, 273)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testGenericArrayInstantiations() {
    String fileContent = """
        class GenArrays<T> {
          private static GenArrays<?>[] genArray = new GenArrays<?>[0];
          private static Class<?>[] classArray = new Class<?>[] {
              GenArrays.class, String.class
          };
            
          public static void main(String[] args) {
            genArray = new GenArrays<?>[6];
            genArray[0] = new GenArrays<String>();
            genArray[1] = new GenArrays<Map<Integer, Map<String, Void>>>();
            System.out.println(getFirstEntry());
            classArray[0] = GenArrays.class;
          }
            
          public static GenArrays<?> getFirstEntry() {
            return genArray[0];
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("GenArrays",
            jMethod("main", 7, 13, 227, 456),
            jMethod("getFirstEntry", 15, 17, 504, 532)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
