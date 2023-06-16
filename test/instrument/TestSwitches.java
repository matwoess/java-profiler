package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.baseTemplate;
import static instrument.Util.parseJavaFile;
import static model.BlockType.*;

public class TestSwitches {

  @Test
  public void TestSwitch() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        switch (x) {
          case 1: {
            i += 3;
            break;
          }
          case 2: {}
          case 3: {
           i *= 2;
           i = i - 1;
          }
          case 4: {
            break;
          }
          default: { break; }
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 20, 62, 239),
                jBlock(BLOCK, 5, 8, 102, 129),
                jBlock(BLOCK, 9, 9, 141, 142),
                jBlock(BLOCK, 10, 13, 154, 183),
                jBlock(BLOCK, 14, 16, 195, 210),
                jBlock(BLOCK, 17, 17, 223, 232)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestNewSwitch() {
    String fileContent = String.format(baseTemplate, """
        String value = "aBcDe";
        switch (value) {
          case "aBcDe" -> {
            if (value.toUpperCase().equals("ABCDE")) {
              System.out.println("as expected");
            }
          }
          case "other value" -> System.out.println("unexpected");
          case "" -> {
            break;
          }
          default -> throw new RuntimeException("should never happen");
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 17, 62, 384),
                jBlock(BLOCK, 5, 9, 127, 225),
                jBlock(BLOCK, 6, 8, 174, 221),
                jBlock(SS_BLOCK, 10, 10, 249, 283),
                jBlock(BLOCK, 11, 13, 298, 313),
                jBlock(SS_BLOCK, 14, 14, 326, 377)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestSwitchExpression() {
    String fileContent = String.format(baseTemplate, """
        int dependingOn = 5;
        int result = switch (dependingOn) {
          case 5 -> {
            yield 2;
          }
          case 1 -> 5;
          default -> {
            if (dependingOn < 10) {
              System.out.println("none of the above");
              yield 0;
            } else {
              yield -1;
            }
          }
        };
        System.out.println("result=" + result);
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 20, 62, 361),
                jBlock(BLOCK, 5, 7, 137, 154),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 8, 8, 166, 169),
                jBlock(BLOCK, 9, 16, 184, 313),
                jBlock(BLOCK, 10, 13, 212, 280),
                jBlock(BLOCK, 13, 15, 287, 309)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestClassLevelSwitchExpression() {
    String fileContent = """
        public class ClassLevelSwitch {
          static int globalInt = switch ("switch".hashCode()) {
            case 12345 -> 5;
            case 6789 -> 6;
            default -> {
              yield 8;
            }
          };
                
          public static void main(String[] args) {
            System.out.println(globalInt);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("ClassLevelSwitch", true,
            jBlock(SS_SWITCH_EXPR_ARROW_CASE, 3, 3, 105, 108),
            jBlock(SS_SWITCH_EXPR_ARROW_CASE, 4, 4, 125, 128),
            jBlock(BLOCK, 5, 7, 145, 166),
            jMethod("main", true, 10, 12, 215, 254)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

}
