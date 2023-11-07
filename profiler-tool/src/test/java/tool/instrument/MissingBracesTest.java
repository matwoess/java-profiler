package tool.instrument;

import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestProgramBuilder.*;
import static tool.instrument.TestInstrumentUtils.baseTemplate;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.model.BlockType.*;
import static tool.model.JumpStatement.Kind.*;

public class MissingBracesTest {
  @Test
  public void testIf() {
    String fileContent = String.format(baseTemplate, """
        if (true == false)return;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 5, 62, 97,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIfElse() {
    String fileContent = String.format(baseTemplate, """
        if (true == false) break;
        else continue;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 6, 62, 112,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(BREAK),
                jSsBlock(BLOCK, 4, 4, 97, 107).withJump(CONTINUE)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testIfElseifElse() {
    String fileContent = String.format(baseTemplate, """
        if (true == false) break;
        else if (true == true) return;
        else continue;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 7, 62, 143,
                jSsBlock(BLOCK, 3, 3, 85, 92).withJump(BREAK),
                jSsBlock(BLOCK, 4, 4, 115, 123).withJump(RETURN),
                jSsBlock(BLOCK, 5, 5, 128, 138).withJump(CONTINUE)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMixedIfs() {
    String fileContent = String.format(baseTemplate, """
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
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 16, 62, 269,
                jSsBlock(BLOCK, 4, 5, 94, 104),
                jBlock(BLOCK, 6, 8, 127, 139),
                jSsBlock(BLOCK, 9, 9, 144, 189).withJump(THROW),
                jBlock(BLOCK, 11, 13, 204, 241),
                jSsBlock(BLOCK, 12, 12, 219, 227).withJump(RETURN),
                jSsBlock(BLOCK, 12, 12, 232, 239)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testDoubleWhileAndIfElse() {
    String fileContent = String.format(baseTemplate, """
        int x = 0;
        while (false) while(true)
          if(1==2)
            return;
          else
            x=1;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 10, 62, 147,
                jSsBlock(LOOP, 4, 8, 91, 142),
                jSsBlock(LOOP, 4, 8, 103, 142),
                jSsBlock(BLOCK, 5, 6, 114, 126).withJump(RETURN),
                jSsBlock(BLOCK, 7, 8, 133, 142)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testDoWhile() {
    String fileContent = String.format(baseTemplate, """
        int x = 0;
        do x+=1; while (x<5);
        do
          x+=1;
        while (x<10);
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 9, 62, 129,
                jSsBlock(LOOP, 4, 4, 80, 86),
                jSsBlock(LOOP, 5, 6, 102, 110)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testForAndForEach() {
    String fileContent = String.format(baseTemplate, """
        int[] array = new int[5];
        for (int i = 0; i < 5; i++)
          array[i] = i;
        for (int val : array) System.out.println(val);
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 8, 62, 188,
                jSsBlock(LOOP, 4, 5, 120, 136),
                jSsBlock(LOOP, 6, 6, 158, 183)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitch() {
    String fileContent = String.format(baseTemplate, """
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
         """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 18, 62, 228,
                jBlock(SWITCH_STMT, 4, 16, 90, 223),
                jBlock(COLON_CASE, 5, 8, 100, 129).noIncOffset(),
                jBlock(BLOCK, 5, 8, 102, 129).withJump(BREAK),
                jBlock(COLON_CASE, 10, 12, 157, 182).noIncOffset(),
                jBlock(COLON_CASE, 13, 14, 192, 203).noIncOffset().withJump(BREAK),
                jBlock(COLON_CASE, 15, 15, 214, 221).noIncOffset().withJump(BREAK)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLabels() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        outer: while (true) while(true)
           if(x==1)
             return;
           else
             break outer;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 10, 62, 165,
                jSsBlock(LOOP, 4, 8, 97, 160),
                jSsBlock(LOOP, 4, 8, 109, 160),
                jSsBlock(BLOCK, 5, 6, 121, 134).withJump(RETURN),
                jSsBlock(BLOCK, 7, 8, 142, 160).withJump(BREAK, "outer")
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
