package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.TestProgramBuilder.*;
import static instrument.TestInstrumentUtils.baseTemplate;
import static instrument.TestInstrumentUtils.parseJavaFile;
import static model.BlockType.*;

public class MissingBracesTest {
  @Test
  public void testIf() {
    String fileContent = String.format(baseTemplate, """
        if (true == false)return;
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 5, 62, 97,
                jBlock(SS_BLOCK, 3, 3, 85, 92)
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
                jBlock(SS_BLOCK, 3, 3, 85, 92),
                jBlock(SS_BLOCK, 4, 4, 97, 107)
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
                jBlock(SS_BLOCK, 3, 3, 85, 92),
                jBlock(SS_BLOCK, 4, 4, 115, 123),
                jBlock(SS_BLOCK, 5, 5, 128, 138)
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
                jBlock(SS_BLOCK, 4, 5, 94, 104),
                jBlock(BLOCK, 6, 8, 127, 139),
                jBlock(SS_BLOCK, 9, 9, 144, 189),
                jBlock(BLOCK, 11, 13, 204, 241),
                jBlock(SS_BLOCK, 12, 12, 219, 227),
                jBlock(SS_BLOCK, 12, 12, 232, 239)
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
                jBlock(SS_BLOCK, 4, 8, 91, 142),
                jBlock(SS_BLOCK, 4, 8, 103, 142),
                jBlock(SS_BLOCK, 5, 6, 114, 126),
                jBlock(SS_BLOCK, 7, 8, 133, 142)
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
                jBlock(SS_BLOCK, 4, 4, 80, 86),
                jBlock(SS_BLOCK, 5, 6, 102, 110)
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
                jBlock(SS_BLOCK, 4, 5, 120, 136),
                jBlock(SS_BLOCK, 6, 6, 158, 183)
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
                jBlock(BLOCK, 5, 8, 102, 129),
                jBlock(SWITCH_CASE, 9, 9, 139, 139),
                jBlock(SWITCH_CASE, 9, 9, 147, 147),
                jBlock(SWITCH_CASE, 10, 12, 157, 182),
                jBlock(SWITCH_CASE, 13, 14, 192, 203),
                jBlock(SWITCH_CASE, 15, 15, 214, 221)
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
                jBlock(SS_BLOCK, 4, 8, 97, 160),
                jBlock(SS_BLOCK, 4, 8, 109, 160),
                jBlock(SS_BLOCK, 5, 6, 121, 134),
                jBlock(SS_BLOCK, 7, 8, 142, 160)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
