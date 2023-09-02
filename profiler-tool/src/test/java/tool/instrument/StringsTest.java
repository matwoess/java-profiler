package tool.instrument;

import org.junit.jupiter.api.Test;
import tool.model.JavaFile;

import static tool.instrument.TestInstrumentUtils.baseTemplate;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.instrument.TestProgramBuilder.jMethod;
import static tool.model.BlockType.BLOCK;

public class StringsTest {
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
    JavaFile expected = jFile(
        jClass("TextBlocksAndSubTextBlocks",
            jMethod("main", 7, 15, 158, 268),
            jMethod("getTextBlock", 16, 18, 308, 331)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testTextBlocksContainingXMLStructure() {
    String fileContent = baseTemplate.formatted("""
        System.out.println(""\"
               <body>
               <span>  static String startEndWithBSlash = "\\\\\\\\asdf\\\\tqwerty\\\\\\\\";</span>
               <span>  static String emptyTextBlock = ""\\"</span>
               <span>  ""\\";</span>
               <span>  static String withNestedBlock = ""\\"</span>
               <span>      Text with</span>
               <span>      multiple (!)</span>
               <span>      lines!\\\\n</span>
               <span>      can contain "strings", \\\\"escaped strings\\\\"</span>
               <span>      and \\\\t""\\\\"</span>
               <span>      Text blocks!</span>
               <span>      with ""\\\\\\\\\\\\\\\\"</span>
               <span>        "nested(!) text blocks",</span>
               <span>        ""\\\\\\\\\\\\\\\\"</span>
               <span>      \\\\""\\"</span>
               <span>      ""\\";</span>
               <span></span>
               </body>
           ""\");
        """, "");
    System.out.println(getBuilderCode(parseJavaFile(fileContent)));
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 24, 62, 841)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksWithDifferentEndings() {
    String fileContent = """
        class TextBlocksWithDifferentEndings {
          public static void main(String[] args) {
            System.out.println(printStrings());
          }
          
          static String printStrings() {
            {
              System.out.print(""\"
                  ""\");
            } {
              System.out.println(""\"
                  x""\");
            } {
              System.out.println(""\"
                  \\""\"");
            } {
              System.out.println(""\"
                  "str1" "str2"
                  ""\");
            } {
              System.out.println(""\"
                  ""str1"" \\"\\"str2\\"\\"
                  ""\");
            } {
              System.out.println(""\"
                  ""str"" ßß$"!)(@æſĸðf\\t\\n\\
                  "\\""\"");
            }
            return ""\"
                \\""\"
                nested "str"
                ""\\"
                ""\";
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocksWithDifferentEndings",
            jMethod("main", 2, 4, 81, 125),
            jMethod("printStrings", 6, 34, 159, 663,
                jBlock(BLOCK, 7, 10, 165, 214),
                jBlock(BLOCK, 10, 13, 216, 268),
                jBlock(BLOCK, 13, 16, 270, 323),
                jBlock(BLOCK, 16, 20, 325, 400),
                jBlock(BLOCK, 20, 24, 402, 485),
                jBlock(BLOCK, 24, 28, 487, 584)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testTextBlocksWithQuotesBeforeInlineEnd() {
    String fileContent = """
        class TextBlocksWithQuotesBeforeInlineEnd {
          static String tBlock = ""\"
              \\"\\"\\"
              "TextBlock",
              \\""\"\\"
              ""\\""\"";
              
          public static void main(String[] args) {
            tBlock += ""\"
                \\""\\""\"";
            if (args.length == 0) {
              tBlock += ""\"
                  \\""\"
                  TextBlock",
                  ""
                  \\""\\"
                  "\\""
                  \\"\\"\\"
                  ""\\""\"";
            }
          }
          
          public static String getTextBlock() {
            return tBlock;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("TextBlocksWithQuotesBeforeInlineEnd",
            jMethod("main", 8, 21, 176, 387,
                jBlock(BLOCK, 11, 20, 240, 383)
            ),
            jMethod("getTextBlock", 23, 25, 428, 451)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

}
