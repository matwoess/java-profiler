package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.parseJavaFile;
import static model.BlockType.*;
import static model.ClassType.ENUM;
import static model.ClassType.INTERFACE;

public class TestEnums {
  @Test
  public void TestEmptyEnum() {
    String fileContent = """
        enum Empty {
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Empty", false)
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestSimpleEnumWithoutMembers() {
    String fileContent = """
        enum Adjective {
          LOW, MEDIUM, HIGH,
          WEAK, STRONG, GREAT
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Adjective", false)
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestEnumWithTrailingSemicolon() {
    String fileContent = """
        enum AB {
          A, B;
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "AB", false)
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void TestStaticBlock() {
    String fileContent = """
        enum AB {
          A, B;
          public static final long ID;
          static {
            ID = 1237877L;
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "AB", false,
            jBlock(STATIC, 4, 6, 59, 82)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void TestMemberFunction() {
    String fileContent = """
        enum Enum {
          VALUE1, VAL_2 ;
          public static final long ID;
          public String lowercase() {
            return this.name().toLowerCase();
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "Enum", false,
            jMethod("lowercase",
                jBlock(METHOD, 4, 6, 90, 132)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestEnumWithConstructorAndArguments() {
    String fileContent = """
        public enum WithConstructor {
          PRIMARY(1, "OK", 1.5f),
          SECONDARY(-6634, "", 5e+4f),
          TERTIARY(5, "This is a test", .3f);
         
          private final int integerVal;
          private final String stringVal;
          public final float floatVal;
         
          WithConstructor(int val1, String str, float num) {
            this.integerVal = val1;
            this.stringVal = str;
            this.floatVal = num;
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithConstructor", false,
            jMethod("WithConstructor",
                jBlock(CONSTRUCTOR, 10, 14, 276, 359)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestEnumMainFunctionAndSwitch() {
    String fileContent = """
        public enum WithMain {
          A,
          B,
          C;
          public static void main(String[] args) {
            WithMain wm = A;
            switch (wm) {
              case A:
                assert (e == A);
              default:
                System.out.println(wm.name());
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithMain", true,
            jMethod("main", true,
                jBlock(METHOD, 5, 13, 80, 222),
                jBlock(SWITCH_CASE, 8, 9, 133, 158),
                jBlock(SWITCH_CASE, 10, 11, 173, 212)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestEnumWithSubClassAndSubInterface() {
    String fileContent = """
        public enum WithSubClassAndInterface {
          A,B,C;
          
          static class ClassInEnum implements InterfaceInEnum {
            public static void printName(WithSubClassAndInterface val) {
              System.out.println(val.name());
            }
          }
          
          private interface InterfaceInEnum {
            default String lowercase(WithSubClassAndInterface val) {
              return val.name().toLowerCase();
            }
          }
          
          public static void callMethods() {
            ClassInEnum.printName(WithSubClassAndInterface.B);
            new ClassInEnum().lowercase(C);
          }
        }""";
    JavaFile expected = jFile(
        jClass(ENUM, "WithSubClassAndInterface", false,
            jClass("ClassInEnum",
                jMethod("printName",
                    jBlock(METHOD, 5, 7, 169, 213)
                )
            ),
            jClass(INTERFACE, "InterfaceInEnum", false,
                jMethod("lowercase",
                    jBlock(METHOD, 11, 13, 317, 362)
                )
            ),
            jMethod("callMethods",
                jBlock(METHOD, 16, 19, 404, 499)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
