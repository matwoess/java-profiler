package tool.instrument;

import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestProgramBuilder.*;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.model.BlockType.*;
import static tool.model.JumpStatement.Kind.*;

public class MissingBracesTest {
  @Test
  public void testIf() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if (true == false)return;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 4, 61, 96,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIfElse() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if (true == false) break;
            else continue;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 5, 61, 115,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(BREAK),
                jSsBlock(BLOCK, 4, 4, 101, 111).withJump(CONTINUE)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIfElseifElse() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if (true == false) break;
            else if (true == true) return;
            else continue;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 6, 61, 150,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(BREAK),
                jSsBlock(BLOCK, 4, 4, 119, 127).withJump(RETURN),
                jSsBlock(BLOCK, 5, 5, 136, 146).withJump(CONTINUE)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMixedIfs() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 50;
            if (x % 2 == 0)
              x += 1;
            else if (x % 2 == 1) {
              x += 3;
            }
            else throw new RuntimeException("invalid state");
                
            if (x > 51) {
              if (x == 53) return; else x = 0;
            }
            System.out.println(x);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 15, 61, 308,
                jSsBlock(BLOCK, 4, 5, 98, 112),
                jBlock(BLOCK, 6, 8, 138, 159),
                jSsBlock(BLOCK, 9, 9, 168, 213).withJump(THROW),
                jBlock(BLOCK, 11, 13, 231, 277),
                jSsBlock(BLOCK, 12, 12, 251, 259).withJump(RETURN),
                jSsBlock(BLOCK, 12, 12, 264, 271)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testDoubleWhileAndIfElse() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 0;
            while (false) while(true)
              if(1==2)
                return;
              else
                x=1;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 61, 166,
                jSsBlock(LOOP, 4, 8, 95, 162),
                jSsBlock(LOOP, 4, 8, 107, 162),
                jSsBlock(BLOCK, 5, 6, 122, 138).withJump(RETURN),
                jSsBlock(BLOCK, 7, 8, 149, 162)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testDoWhile() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 0;
            do x+=1; while (x<5);
            do
              x+=1;
            while (x<10);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 61, 144,
                jSsBlock(LOOP, 4, 4, 84, 90),
                jSsBlock(LOOP, 5, 6, 110, 122)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testForAndForEach() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int[] array = new int[5];
            for (int i = 0; i < 5; i++)
              array[i] = i;
            for (int val : array) System.out.println(val);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 7, 61, 199,
                jSsBlock(LOOP, 4, 5, 124, 144),
                jSsBlock(LOOP, 6, 6, 170, 195)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitch() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            switch (x) {
              case 1: {
                x += 3;
                break;
              }
              case 2: case 3:
              case 4:
               x *= 2;
               x = x - 1;
              case 5:
                break;
              default: break;
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 17, 61, 279,
                jBlock(SWITCH_STMT, 4, 16, 93, 275),
                jBlock(COLON_CASE, 5, 8, 108, 149).noIncOffset(),
                jBlock(BLOCK, 5, 8, 109, 149).withJump(BREAK),
                jBlock(COLON_CASE, 10, 12, 185, 218).noIncOffset(),
                jBlock(COLON_CASE, 13, 14, 232, 247).withJump(BREAK).noIncOffset(),
                jBlock(COLON_CASE, 15, 15, 262, 269).withJump(BREAK).noIncOffset()
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLabels() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            int x = 1;
            outer: while (true) while(true)
               if(x==1)
                 return;
               else
                 break outer;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 61, 184,
                jSsBlock(LOOP, 4, 8, 101, 180),
                jSsBlock(LOOP, 4, 8, 113, 180),
                jSsBlock(BLOCK, 5, 6, 129, 146).withJump(RETURN),
                jSsBlock(BLOCK, 7, 8, 158, 180).withJump(BREAK, "outer")
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
