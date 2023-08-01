package instrument;

import model.JClass;
import model.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static instrument.TestInstrumentUtils.parseJavaFile;
import static instrument.TestProgramBuilder.*;
import static model.BlockType.*;
import static model.ClassType.ANONYMOUS;
import static model.ClassType.LOCAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpecialCasesTest {
  @Test
  public void testBeginOfImportsAndPackageName_NoPackage() {
    String fileContent = """
        import static java.lang.System.exit;
        import java.util.ArrayList;
        class Empty {
        }""";
    JavaFile expected = jFile(null, 0, jClass("Empty"));
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testBeginOfImportsAndPackageName() {
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
            jMethod("meth", 5, 6, 122, 126),
            jClass("InEmpty",
                jMethod("innerMeth", 8, 9, 167, 173)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    JClass tlClass = expected.topLevelClasses.get(0);
    JClass innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("name.Of._the_.pkg.Empty", tlClass.getFullName());
    assertEquals("Empty", tlClass.getName());
    assertEquals("name.Of._the_.pkg.Empty$InEmpty", innerClass.getFullName());
    assertEquals("Empty$InEmpty", innerClass.getName());
  }

  @Test
  public void testNestedBlockTypes() {
    String fileContent = """
        class BlockTypeClass {
          static int a;
           
          static {
            if (5 % 2 == 1) switch ("this") {
              case "thus" -> a = 1;
              case "this" -> {
                a = 2;
              }
            }
            else {
              a = "this".chars().map(i -> i + 1).sum();
            }
          }
           
          public BlockTypeClass() {
            super();
            if (a == 2) {
              System.out.println("2");
            } else System.out.println("other");
          }
           
          public void method() {
            String x = "12345";
            if (x != null) {
              for (int charVal : x.chars().toArray())
                switch (charVal) {
                  case 1:
                    System.out.println(1);
                  case 2:
                    x.chars().map(ch -> {
                      return switch (ch) {
                        case 0 -> {
                          yield 0;
                        }
                        case 1 -> 1 + 5;
                        case 2 -> x.chars().map(c -> c + 1).filter(c -> {
                          return c < 10;
                        }).sum();
                        case 3 -> {
                          if (ch == 3) yield 3;
                          else yield -1;
                        }
                        case 4 -> {
                          yield switch (ch) {
                            default -> ch;
                          };
                        }
                        default -> -1;
                      };
                    }).forEach(i -> System.out.println(i));
                  case 3:
                    List<Integer> numbers = List.of(1, 2, 3, 4);
                    numbers.sort(new Comparator<Integer>() {
                      @Override
                      public int compare(Integer i1, Integer i2) {
                        int x = switch (i1) {
                          case 0 -> 0;
                          case -1 -> {
                            yield -1;
                          }
                          default -> i1 - i2;
                        };
                        return x;
                      }
                    });
                    if (numbers.size() > 5) System.out.println(numbers.get(0));
                  default:
                    System.out.println("other value");
                }
            }
            if (x.equals("abc")) {
              ;
            }
          }
        }
        """;
    JavaFile expected = jFile(null, 0,
        jClass("BlockTypeClass",
            jBlock(STATIC, 4, 14, 50, 237),
            jBlock(SS_BLOCK, 5, 10, 70, 168),
            jBlock(SS_BLOCK, 6, 6, 109, 116),
            jBlock(BLOCK, 7, 9, 139, 162),
            jBlock(BLOCK, 11, 13, 179, 233),
            jBlock(SS_LAMBDA, 12, 12, 213, 219),
            jMethod("BlockTypeClass",
                jBlock(CONSTRUCTOR, 16, 21, 266, 372, 13),
                jBlock(BLOCK, 18, 20, 297, 334),
                jBlock(SS_BLOCK, 20, 20, 339, 368)
            ),
            jClass(ANONYMOUS, null,
                jMethod("compare", 56, 65, 1457, 1706,
                    jBlock(SS_SWITCH_EXPR_ARROW_CASE, 58, 58, 1523, 1526),
                    jBlock(BLOCK, 59, 61, 1557, 1607),
                    jBlock(SS_SWITCH_EXPR_ARROW_CASE, 62, 62, 1636, 1645)
                )
            ),
            jMethod("method", 23, 75, 398, 1921,
                jBlock(BLOCK, 25, 71, 443, 1876),
                jBlock(SS_BLOCK, 26, 70, 489, 1870),
                jBlock(SWITCH_CASE, 28, 29, 534, 569),
                jBlock(SWITCH_CASE, 30, 51, 587, 1246),
                jBlock(BLOCK, 31, 51, 621, 1208),
                jBlock(BLOCK, 33, 35, 684, 729),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 36, 36, 755, 762),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 37, 39, 788, 887),
                // TODO: missing lambda blocks!
                jBlock(BLOCK, 40, 43, 915, 1006),
                jBlock(SS_BLOCK, 41, 41, 946, 955),
                jBlock(SS_BLOCK, 42, 42, 978, 988),
                jBlock(BLOCK, 44, 48, 1034, 1146),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 46, 46, 1103, 1107),
                jBlock(SS_SWITCH_EXPR_ARROW_CASE, 49, 49, 1173, 1177),
                jBlock(SS_LAMBDA, 51, 51, 1222, 1244),
                jBlock(SWITCH_CASE, 52, 67, 1264, 1794),
                jBlock(SS_BLOCK, 67, 67, 1758, 1794),
                jBlock(SWITCH_CASE, 68, 69, 1813, 1860),
                jBlock(BLOCK, 72, 74, 1903, 1917)
            )
        )
    );
    System.out.println(getBuilderCode(parseJavaFile(fileContent)));
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testNamingWithInnerAndLocalClasses() {
    String fileContent = """
        package mixed.pkg;
        
        class MixedClasses {
          public class Inner {
          }
            
          public static void main(String[] args) {
            Runnable r1 = new Runnable() {
              @Override
              public void run() {
              }
            };
            class Local1 {
            }
            Runnable r2 = new Runnable() {
              @Override
              public void run() {
                class Local2 {
                }
              }
            };
            interface Local3 {
            }
          }
            
          private Runnable r3 = new Runnable() {
            @Override
            public void run() {
            }
          };
        }""";
    JavaFile expected = jFile("mixed.pkg", 18,
        jClass("MixedClasses",
            jClass("Inner"),
            jClass(ANONYMOUS, null,
                jMethod("run", 10, 11, 188, 196)
            ),
            jClass(LOCAL, "Local1"),
            jClass(ANONYMOUS, null,
                jClass(LOCAL, "Local2"),
                jMethod("run", 17, 20, 305, 346)
            ),
            jClass(LOCAL, "Local3"),
            jClass(ANONYMOUS, null,
                jMethod("run", 28, 29, 466, 472)
            ),
            jMethod("main", 7, 24, 111, 386)
        )
    );
    JavaFile actual = parseJavaFile(fileContent);
    TestInstrumentUtils.assertResultEquals(expected, actual);
    List<JClass> innerClasses = expected.topLevelClasses.get(0).innerClasses;
    assertEquals("MixedClasses$Inner", innerClasses.get(0).getName());
    assertEquals("mixed.pkg.MixedClasses$Inner", innerClasses.get(0).getFullName());
    assertEquals("1", innerClasses.get(1).name);
    assertEquals("MixedClasses$1", innerClasses.get(1).getName());
    assertEquals("mixed.pkg.MixedClasses$1", innerClasses.get(1).getFullName());
    assertEquals("Local1", innerClasses.get(2).name);
    assertEquals("MixedClasses$Local1", innerClasses.get(2).getName());
    assertEquals("mixed.pkg.MixedClasses$Local1", innerClasses.get(2).getFullName());
    assertEquals("2", innerClasses.get(3).name);
    assertEquals("MixedClasses$2", innerClasses.get(3).getName());
    assertEquals("mixed.pkg.MixedClasses$2", innerClasses.get(3).getFullName());
    assertEquals("Local2", innerClasses.get(3).innerClasses.get(0).name);
    assertEquals("MixedClasses$2$Local2", innerClasses.get(3).innerClasses.get(0).getName());
    assertEquals("mixed.pkg.MixedClasses$2$Local2", innerClasses.get(3).innerClasses.get(0).getFullName());
    assertEquals("Local3", innerClasses.get(4).name);
    assertEquals("MixedClasses$Local3", innerClasses.get(4).getName());
    assertEquals("mixed.pkg.MixedClasses$Local3", innerClasses.get(4).getFullName());
    assertEquals("3", innerClasses.get(5).name);
    assertEquals("MixedClasses$3", innerClasses.get(5).getName());
    assertEquals("mixed.pkg.MixedClasses$3", innerClasses.get(5).getFullName());
  }

}
