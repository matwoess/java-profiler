package tool.instrument;

import tool.model.JClass;
import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestProgramBuilder.*;
import static tool.instrument.TestInstrumentUtils.baseTemplate;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.model.BlockType.BLOCK;
import static tool.model.ClassType.ANONYMOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tool.model.JumpStatement.RETURN;

public class AnonymousClassesTest {
  @Test
  public void testAsArgumentInClassLevelMethod() {
    String fileContent = String.format(baseTemplate, "", """
        static File firstJavaFile(Path directory) {
          File[] allJavaFiles = directory.toFile().listFiles(new FilenameFilter() {
            boolean isTrue = false;
             
            @Override
            public boolean accept(File file, String name) {
              isTrue = returnTrue();
              return name.endsWith(".java");
            }
             
            class X {
              static void methodInX() {
                System.out.println("Hello from inside a nested class in an anonymous class.");
              }
            }
             
            public boolean returnTrue() {
              X.methodInX();
              return true;
            }
          });
          if (allJavaFiles != null && allJavaFiles.length > 0) {
            return allJavaFiles[0];
          } else {
            return null;
          }
        }
         """);
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 4, 62, 71),
            jMethod("firstJavaFile", 5, 31, 118, 715,
                jBlock(BLOCK, 26, 28, 653, 685).withJump(RETURN),
                jBlock(BLOCK, 28, 30, 692, 713).withJump(RETURN)
            ),
            jClass(ANONYMOUS, null,
                jMethod("accept", 10, 13, 289, 361).withJump(RETURN),
                jClass("X",
                    jMethod("methodInX", 16, 18, 408, 503)
                ),
                jMethod("returnTrue", 21, 24, 544, 590).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    JClass innerClass = expected.topLevelClasses.get(0).innerClasses.get(0);
    assertEquals("Main$1", innerClass.getName());
    assertEquals("Main$1$X", innerClass.innerClasses.get(0).getName());
  }

  @Test
  public void testAs2ndArgumentInClassLevelMethodWithGenericType() {
    String fileContent = String.format(baseTemplate, "", """
        static List<Integer> getSortedIntegers(List<Integer> arrayList) {
          Collections.sort(arrayList, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
              if (i1.equals(i2)) {
                return 0;
              }
             
              return i1 < i2 ? -1 : 1;
            }
          });
          return arrayList;
        }
        """);
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 4, 62, 71),
            jMethod("getSortedIntegers", 5, 17, 140, 380).withJump(RETURN),
            jClass(ANONYMOUS, null,
                jMethod("compare", 8, 14, 261, 352,
                    jBlock(BLOCK, 9, 11, 288, 314).withJump(RETURN)
                ).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

  @Test
  public void testAsStatementStartInMethod() {
    String fileContent = String.format(baseTemplate, """
        new Main() {
          @Override
          public int hashCode() {
            return super.hashCode();
          }
        };
        """, "");
    JavaFile expected = jFile(
        jClass("Main",
            jMethod("main", 2, 10, 62, 158),
            jClass(ANONYMOUS, null,
                jMethod("hashCode", 5, 7, 117, 150).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    assertEquals("Main$1", expected.topLevelClasses.get(0).innerClasses.get(0).getName());
  }

}
