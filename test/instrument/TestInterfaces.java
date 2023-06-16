package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.parseJavaFile;
import static model.BlockType.METHOD;
import static model.ClassType.CLASS;
import static model.ClassType.INTERFACE;

public class TestInterfaces {
  @Test
  public void TestAbstractMethodsNoBlock() {
    String fileContent = """
        interface AbstractMethods {
          String getName();
          public int getAge(int userId);
          abstract void printInfo(String userName, int age);
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "AbstractMethods", false)
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMembersWithInitBlocks() {
    String fileContent = """
        interface InitBlocks {
          int[] ints = new int[]{0, 1, 3};
          List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
          String[] strings = new String[]{
              String.format("%d", ints[1]),
              floats.get(1).toString(),
              "ASDF",
          };
          default void doNothing() {}
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "InitBlocks", false,
            jMethod("doNothing",
                jBlock(METHOD, 9, 9, 275, 276)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestDefaultAndStaticMethodsWithBlock() {
    String fileContent = """
        interface DefaultStatic {
          int x = 0;
          default String getName() {
            return "empty";
          }
          default public int getAge(int userId) {
            return userId / 2;
          }
          static void printInfo(String userName, int age) {
            System.out.printf("%s: %d\\n", userName, age);
          }
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "DefaultStatic", false,
            jMethod("getName",
                jBlock(METHOD, 3, 5, 67, 91)
            ),
            jMethod("getAge",
                jBlock(METHOD, 6, 8, 133, 160)
            ),
            jMethod("printInfo",
                jBlock(METHOD, 9, 11, 212, 266)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestSubInterfacesAndClasses() {
    String fileContent = """
        public interface Interfaces extends Serializable {
          int x = 0;
          int get();
          public interface SubInterface extends Interfaces {
            @Override
            default public int get() {
              return SubClass.getXPlus1() + ints[0];
            }
            
            class SubClass {
              static int getXPlus1() {
                return x + 1;
              }
            }
          }
          
          class X implements SubInterface {
            void callGet() {
              get();
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "Interfaces", false,
            jClass(INTERFACE, "SubInterface", false,
                jMethod("get",
                    jBlock(METHOD, 6, 8, 174, 225)
                ),
                jClass(CLASS, "SubClass", false,
                    jMethod("getXPlus1",
                        jBlock(METHOD, 11, 13, 278, 308)
                    )
                )
            ),
            jClass("X",
                jMethod("callGet",
                    jBlock(METHOD, 18, 20, 376, 395)
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMainEntryPointPublic() {
    String fileContent = """
        public interface WithMain {
          int get();
          
          class X implements WithMain {
            @Override
            public int get() {
              return 0;
            }
          }
          
          public static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "WithMain", true,
            jClass("X",
                jMethod("get",
                    jBlock(METHOD, 6, 8, 110, 132)
                )
            ),
            jMethod("main", true,
                jBlock(METHOD, 11, 15, 180, 268)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMainEntryPointImplicitPublic() {
    String fileContent = """
        public interface InferredPublic {
          static void main(String[] args) {
            WithMain x = new X();
            int result = x.get();
            System.out.println(result);
          }
        }""";
    JavaFile expected = jFile(
        jClass(INTERFACE, "InferredPublic", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 6, 69, 157)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
