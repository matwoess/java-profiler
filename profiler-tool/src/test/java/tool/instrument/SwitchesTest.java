package tool.instrument;

import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestProgramBuilder.*;
import static tool.instrument.TestInstrumentUtils.baseTemplate;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.model.BlockType.*;

public class SwitchesTest {

  @Test
  public void testSwitch() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        switch (x) {
          case 1: {
            x += 3;
            break;
          }
          case 2: {}
          case 3: {
           x *= 2;
           x = x - 1;
          }
          case 4: {
            break;
          }
          default: { break; }
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 20, 62, 239,
                jBlock(BLOCK, 5, 8, 102, 129),
                jBlock(BLOCK, 9, 9, 141, 142),
                jBlock(BLOCK, 10, 13, 154, 183),
                jBlock(BLOCK, 14, 16, 195, 210),
                jBlock(BLOCK, 17, 17, 223, 232)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testCommaSeparatedCases() {
    String fileContent = String.format(baseTemplate, """
        int x = 1;
        switch (x) {
          case 1, 2, 3: {
            x += 3;
            break;
          }
          case 4, 5: {
            x *= 2;
            x = x - 1;
          }
          default: { break; }
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 16, 62, 210,
                jBlock(BLOCK, 5, 8, 108, 135),
                jBlock(BLOCK, 9, 12, 150, 181),
                jBlock(BLOCK, 13, 13, 194, 203)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testNewSwitch() {
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
        jClass("Main",
            jMethod("main", 2, 17, 62, 384,
                jBlock(BLOCK, 5, 9, 127, 225),
                jBlock(BLOCK, 6, 8, 174, 221),
                jBlock(SS_BLOCK, 10, 10, 249, 283),
                jBlock(BLOCK, 11, 13, 298, 313),
                jBlock(SS_BLOCK, 14, 14, 326, 377)
            )
        )
    );
    expected.foundBlocks.get(expected.foundBlocks.size() - 1).startsWithThrow = true;
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitchExpression() {
    String fileContent = String.format(baseTemplate, """
        for (int i = 0; i < 11; i++) {
          int result = switch (i) {
            case 5: {
              yield 2;
            }
            case 1: case 2: yield 5;
            case 11: case 12: throw new RuntimeException();
            default: {
              if (i < 10) {
                System.out.println("none of the above");
                yield 0;
              } else {
                yield -1;
              }
            }
          };
          System.out.println("result=" + result);
        }""", "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 21, 62, 442,
                jBlock(BLOCK, 3, 20, 97, 438),
                jBlock(BLOCK, 5, 7, 139, 160),
                jBlock(SWITCH_CASE, 8, 8, 180, 189),
                jBlock(SWITCH_CASE, 9, 9, 211, 241),
                jBlock(BLOCK, 10, 17, 256, 389),
                jBlock(BLOCK, 11, 14, 276, 350),
                jBlock(BLOCK, 14, 16, 357, 383)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSwitchExpression_ArrowCases() {
    String fileContent = String.format(baseTemplate, """
        int dependingOn = 7;
        int result = switch (dependingOn) {
          case 5 -> {
            yield 2;
          }
          case 1, 2 -> 5;
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
        jClass("Main",
            jMethod("main", 2, 20, 62, 364,
                jBlock(BLOCK, 5, 7, 137, 154),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 8, 8, 169, 172),
                jBlock(BLOCK, 9, 16, 187, 316),
                jBlock(BLOCK, 10, 13, 215, 283),
                jBlock(BLOCK, 13, 15, 290, 312)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testClassLevelSwitchExpression() {
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
        jClass("ClassLevelSwitch",
            jBlock(SS_SWITCH_EXPR_ARROW_CASE, 3, 3, 105, 108),
            jBlock(SS_SWITCH_EXPR_ARROW_CASE, 4, 4, 125, 128),
            jBlock(BLOCK, 5, 7, 145, 166),
            jMethod("main", 10, 12, 215, 254)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testSwitchExpressionAsReturn() {
    String fileContent = String.format(baseTemplate, """
        System.out.println(StatusCode.getStatusCodeDescription(StatusCode.FORBIDDEN));
        System.out.println(StatusCode.getStatusCodeDescription(StatusCode.UNAUTHORIZED));
        """, """
        static enum StatusCode {
          OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND;
          public static String getStatusCodeDescription(StatusCode code) {
            return switch(code) {
              case OK -> "everything went great";
              case NOTFOUND, FORBIDDEN -> "cannot access";
              case UNAUTHORIZED -> {
                yield "did you forget to enter your password?";
              }
            };
          }
        }
        """);
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 6, 62, 232),
            jClass("StatusCode",
                jMethod("getStatusCodeDescription", 9, 17, 368, 591,
                    jBlock(SS_SWITCH_EXPR_ARROW_CASE, 11, 11, 411, 436),
                    jBlock(SS_SWITCH_EXPR_ARROW_CASE, 12, 12, 470, 487),
                    jBlock(BLOCK, 13, 15, 516, 580)
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

}