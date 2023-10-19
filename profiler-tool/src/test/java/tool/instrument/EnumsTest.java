package tool.instrument;

import org.junit.jupiter.api.Test;
import tool.model.CodePosition;
import tool.model.JavaFile;

import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.*;
import static tool.model.JumpStatement.RETURN;

public class EnumsTest {
  @Test
  public void testEmptyEnum() {
    String fileContent = """
        enum Empty {
        }""";
    JavaFile expected = jFile(
        jClass("Empty")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSimpleEnumWithoutMembers() {
    String fileContent = """
        enum Adjective {
          LOW, MEDIUM, HIGH,
          WEAK, STRONG, GREAT
        }""";
    JavaFile expected = jFile(
        jClass("Adjective")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithTrailingSemicolon() {
    String fileContent = """
        enum AB {
          A, B;
        }""";
    JavaFile expected = jFile(
        jClass("AB")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithTrailingComma() {
    String fileContent = """
        enum AB {
          A, B,
        }""";
    JavaFile expected = jFile(jClass("AB"));
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithTrailingCommaAndSemicolon() {
    String fileContent = """
        enum AB {
          A, B,;
        }""";
    JavaFile expected = jFile(jClass("AB"));
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testStaticBlock() {
    String fileContent = """
        enum AB {
          A, B;
          public static final long ID;
          static {
            ID = 1237877L;
          }
        }""";
    JavaFile expected = jFile(
        jClass("AB",
            jBlock(STATIC, 4, 6, 59, 82)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testMemberFunction() {
    String fileContent = """
        enum Enum {
          VALUE1, VAL_2, ;
          public static final long ID;
          public String lowercase() {
            return this.name().toLowerCase();
          }
        }""";
    JavaFile expected = jFile(
        jClass("Enum",
            jMethod("lowercase", 4, 6, 91, 133).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithConstructorAndArguments() {
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
        jClass("WithConstructor",
            jConstructor("WithConstructor", 10, 14, 276, 359)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumMainFunctionAndSwitch() {
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
        jClass("WithMain",
            jMethod("main", 5, 13, 80, 222,
                jBlock(SWITCH_STMT, 7, 12, 119,218),
                jBlock(COLON_CASE, 8, 9, 133, 158).noIncOffset(),
                jBlock(COLON_CASE, 10, 11, 173, 212).noIncOffset()
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithSubClassAndSubInterface() {
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
        jClass("WithSubClassAndInterface",
            jClass("ClassInEnum",
                jMethod("printName", 5, 7, 169, 213)
            ),
            jClass("InterfaceInEnum",
                jMethod("lowercase", 11, 13, 317, 362).withJump(RETURN)
            ),
            jMethod("callMethods", 16, 19, 404, 499)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithInnerEnum_WithAndWithoutSemicolon() {
    String fileContent = """
        public enum WithInnerEnum {
          A,B,C;
           
          enum InnerEnum {
            D,E;
          }
           
          public static void printValues() {
            for (InnerEnum val : InnerEnum.values()) {
              System.out.println(val);
            }
            for (WithInnerEnum val : WithInnerEnum.values()) {
              System.out.println(val);
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("WithInnerEnum",
            jClass("InnerEnum"),
            jMethod("printValues", 8, 15, 107, 287,
                jBlock(LOOP, 9, 11, 154, 191),
                jBlock(LOOP, 12, 14, 246, 283)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    fileContent = fileContent.replace("D,E;", "D,E");
    expected.foundBlocks.forEach(block -> {
      block.beg = new CodePosition(block.beg.line(), block.beg.pos() - 1);
      block.incInsertPosition--;
      block.end = new CodePosition(block.end.line(), block.end.pos() - 1);
    });
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testEnumWithAbstractMethodsAndImplementationsPerValue() {
    String fileContent = """
        public enum EnumWithInnerAbstractEnum {
          A,B,C;
          
          public static void main(String[] args) {
            System.out.println(WithAbstractMethods.Value1.description());
            WithAbstractMethods.Value2.printDescription();
          }
            
          enum WithAbstractMethods {
            Value1() {
              @Override
              String description() {
                return "The value 1";
              }
              static int x;
              static {
                x = 5;
              }
          
              @Override
              void printDescription() {
                System.out.println(x + ", " + this.description());
              }
            },
            Value2 {
              @Override
              String description() {
                return null;
              }
             
              @Override
              void printDescription() {
                System.out.println("the description is:" + this.description());
              }
            };
            abstract String description();
            abstract void printDescription();
          }
        }""";
    JavaFile expected = jFile(
        jClass("EnumWithInnerAbstractEnum",
            jClass("WithAbstractMethods",
                jMethod("description", 12, 14, 303, 341).withJump(RETURN),
                jBlock(STATIC, 16, 18, 376, 399),
                jMethod("printDescription", 21, 23, 448, 515),
                jMethod("description", 27, 29, 580, 609).withJump(RETURN),
                jMethod("printDescription", 32, 34, 658, 738),
                jMethod("description"),
                jMethod("printDescription")
            ),
            jMethod("main", 4, 7, 92, 213)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testEnumWithValueAnnotations() {
    String fileContent = """
        enum AnnotatedEnum {
          @Deprecated A_VAL,
          @API(status = STABLE, since = "5.4")
          B_VAL,
          C_VAL,
          @API(status = STABLE, since = "6.0") D_VAL;
          
          public static final long ID = 1;
          
          AnnotatedEnum getDVal() {
            return D_VAL;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("AnnotatedEnum",
            jMethod("getDVal", 10, 12, 209, 231).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
