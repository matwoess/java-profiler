package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.TestInstrumentUtils.baseTemplate;
import static instrument.TestInstrumentUtils.parseJavaFile;
import static instrument.TestProgramBuilder.*;
import static model.BlockType.BLOCK;
import static model.BlockType.SS_BLOCK;
import static model.ClassType.ANONYMOUS;
import static model.ClassType.LOCAL;

public class LocalClassesTest {

  @Test
  public void testEnumInMainMethod() {
    String fileContent = baseTemplate.formatted("""
        enum StatusCode {
          OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND
        }
        StatusCode statusCode = StatusCode.FORBIDDEN;
        if (statusCode != StatusCode.FORBIDDEN) {
          throw new RuntimeException("strange");
        }
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "StatusCode"),
            jMethod("main", 2, 11, 62, 262,
                jBlock(BLOCK, 7, 9, 214, 257)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testInterfaceInIfBlockAndAnonymousInstantiation() {
    String fileContent = baseTemplate.formatted("""
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
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jClass(LOCAL, "IGreeter",
                jMethod("greet", 5, 7, 140, 188),
                jMethod("greetPerson")
            ),
            jClass(ANONYMOUS, null,
                jMethod("greetPerson", 12, 14, 326, 394)
            ),
            jMethod("main", 2, 20, 62, 463,
                jBlock(BLOCK, 3, 17, 90, 440),
                jBlock(SS_BLOCK, 18, 18, 450, 458)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testLocalClassInAnonymousClassMethod() {
    String fileContent = baseTemplate.formatted("""
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
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jClass(ANONYMOUS, null,
                jClass(LOCAL, "CompHelper",
                    jMethod("comp", 7, 9, 250, 300)
                ),
                jMethod("compare", 5, 14, 188, 409)
            ),
            jMethod("main", 2, 18, 62, 460)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
