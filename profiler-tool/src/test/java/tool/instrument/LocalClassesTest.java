package tool.instrument;

import org.junit.jupiter.api.Test;
import tool.model.JavaFile;

import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.BLOCK;
import static tool.model.ClassType.ANONYMOUS;
import static tool.model.ClassType.LOCAL;
import static tool.model.JumpStatement.Kind.RETURN;
import static tool.model.JumpStatement.Kind.THROW;

public class LocalClassesTest {

  @Test
  public void testEnumInMainMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            enum StatusCode {
              OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND
            }
            StatusCode statusCode = StatusCode.FORBIDDEN;
            if (statusCode != StatusCode.FORBIDDEN) {
              throw new RuntimeException("strange");
            }
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "StatusCode"),
            jMethod("main", 2, 11, 61, 262,
                jBlock(BLOCK, 7, 9, 213, 257).withJump(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testInterfaceInIfBlockAndAnonymousInstantiation() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            if ((2 + 6) % 2 == 0) {
              interface IGreeter {
                default void greet() {
                  System.out.println("Hello there.");
                }
                void greetPerson(Object person);
              }
              IGreeter greeter = new IGreeter() {
                @Override
                public void greetPerson(Object person) {
                  System.out.println("Hello " + person.toString() + ".");
                }
              };
              greeter.greetPerson(IGreeter.class);
            }
            if (true) return;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "IGreeter",
                jMethod("greet", 5, 7, 139, 188),
                jMethod("greetPerson")
            ),
            jClass(ANONYMOUS, null,
                jMethod("greetPerson", 12, 14, 325, 394)
            ),
            jMethod("main", 2, 20, 61, 463,
                jBlock(BLOCK, 3, 17, 89, 440),
                jSsBlock(BLOCK, 18, 18, 450, 458).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLocalClassInAnonymousClassMethod() {
    String fileContent = """
        public class Main {
          public static void main(String[] args) {
            final Comparator<String> lengthComparator = new Comparator<>() {
              @Override
              public int compare(String s1, String s2) {
                class CompHelper {
                  int comp(String s1, String s2) {
                    return s1.length() - s2.length();
                  }
                }
                CompHelper helper = new CompHelper();
                helper.comp(s1, s2);
                return helper.comp(s1, s2);
              }
            };
            lengthComparator.compare("word", "12345");
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Main",
            jClass(ANONYMOUS, null,
                jClass(LOCAL, "CompHelper",
                    jMethod("comp", 7, 9, 249, 300).withJump(RETURN)
                ),
                jMethod("compare", 5, 14, 187, 409).withJump(RETURN)
            ),
            jMethod("main", 2, 18, 61, 460)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
