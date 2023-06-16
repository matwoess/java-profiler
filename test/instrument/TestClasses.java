package instrument;

import model.Class;
import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.parseJavaFile;
import static model.BlockType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestClasses {
  @Test
  public void TestAbstractClass() {
    String fileContent = """
        abstract class Pet {
          String name;
          int age;
          abstract String speak();  // method without block
          @Override
          public String toString() {
            return "Pet{" +
                "name='" + name + '\\'' +
                ", age=" + age +
                '}';
          }
        }""";
    JavaFile expected = jFile(
        jClass("Pet",
            jMethod("toString",
                jBlock(METHOD, 6, 11, 139, 234)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestInnerClassAndStaticBlocks() {
    String fileContent = """
        public class Classes {
          static int i;
          
          static {
            i = 0;
          }
          
          public static void main(String[] args) {
            PetFarm farm = new PetFarm();
            farm.pets.add("Barky");
            farm.pets.add("Molly");
            for (String pet : farm.pets) {
              String output;
              if (pet.length() > 3) {
                output = String.format(pet + " has a long name.");
              } else {
                output = String.format(pet + " has a short name.");
              }
              System.out.println(output);
            }
          }
          
          static class PetFarm {
            static List<String> pets = new ArrayList<>();
            
            static {
              pets.add("Bob");
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("Classes", true,
            jBlock(STATIC, 4, 6, 50, 65),
            jMethod("main", true,
                jBlock(METHOD, 8, 21, 109, 471),
                jBlock(BLOCK, 12, 20, 234, 467),
                jBlock(BLOCK, 14, 16, 285, 352),
                jBlock(BLOCK, 16, 18, 359, 427)
            ),
            jClass("PetFarm",
                jBlock(STATIC, 26, 28, 561, 590)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void TestDeepInnerClasses() {
    String fileContent = """
        public class InnerClasses {
          public static void main(String[] args) {
            Inner inner = new Inner();
            Inner.Sub innerSub = new Inner.Sub();
            Inner.Sub.SubSub innerSubSub = new Inner.Sub.SubSub();
            inner.level1();
            innerSub.level2();
            innerSubSub.level3();
          }
          static class Inner {
            void level1() {
              System.out.println("Level: 1");
            }
            static class Sub {
              void level2() {
                System.out.println("Level: 2");
              }
              static class SubSub {
                void level3() {
                  System.out.println("Level: 3");
                }
              }
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("InnerClasses", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 9, 70, 275)
            ),
            jClass("Inner",
                jMethod("level1",
                    jBlock(METHOD, 11, 13, 318, 362)
                ),
                jClass("Sub",
                    jMethod("level2",
                        jBlock(METHOD, 15, 17, 407, 455)
                    ),
                    jClass("SubSub",
                        jMethod("level3",
                            jBlock(METHOD, 19, 21, 507, 559)
                        )
                    )
                )
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestClassWithInheritanceAndConstructors() {
    String fileContent = """
        abstract class Pet {
          String name;
          int age;
          abstract String speak();  // method without block
          @Override
          public String toString() {
            return "Pet{" +
                "name='" + name + '\\'' +
                ", age=" + age +
                '}';
          }
        }
                
        class Dog extends Pet {
          public Dog(String name, int age) {
            this.name = name;
            this.age = age;
          }
          @Override String speak() { return "woof!"; }
        }
                
        class Cat extends Pet {
          public Cat(String name, int age) {
            this.name = name;
            this.age = age;
          }
          @Override String speak() { return "meow."; }
        }""";
    JavaFile expected = jFile(
        jClass("Pet",
            jMethod("toString", false, 6, 11, 139, 234)
        ),
        jClass("Dog",
            jMethod("Dog",
                jBlock(CONSTRUCTOR, 15, 18, 298, 344)
            ),
            jMethod("speak", false, 19, 19, 373, 391)
        ),
        jClass("Cat",
            jMethod("Cat",
                jBlock(CONSTRUCTOR, 23, 26, 455, 501)
            ),
            jMethod("speak", false, 27, 27, 530, 548)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMembersWithInitBlocks() {
    String fileContent = """
        class InitBlocks {
          int[] ints = new int[]{0, 1, 3};
          List<Float> floats = new ArrayList<>(Arrays.asList(0.5f, 3.4f));
          String[] strings = new String[]{
              String.format("%d", ints[1]),
              floats.get(1).toString(),
              "ASDF",
          };
          void doNothing() {}
        }""";
    JavaFile expected = jFile(
        jClass("InitBlocks",
            jMethod("doNothing", false, 9, 9, 263, 264)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMembersWithImplicitInitializers() {
    String fileContent = """
        class ImplicitInitBlocks {
          static String[] strings = {"String1", "String2"};
          private static final int[][] intArray = {
            {55, 44},
            {123, 456}
          };
          void doNothing() {}
        }""";
    JavaFile expected = jFile(
        jClass("ImplicitInitBlocks",
            jMethod("doNothing", false, 7, 7, 177, 178)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestSubClassWithFollowingMethod() {
    // testing that the classStack works
    String fileContent = """
        class A {
          class B {
            void classBMeth() {}
          }
          void classAMeth1() {}
          class C {
            class D {
              void classDMeth() {}
            }
            void classCMeth() {}
          }
          void classAMeth2() {}
        }""";
    JavaFile expected = jFile(
        jClass("A",
            jClass("B",
                jMethod("classBMeth", false, 3, 3, 45, 46)
            ),
            jMethod("classAMeth1", false, 5, 5, 73, 74),
            jClass("C",
                jClass("D",
                    jMethod("classDMeth", false, 8, 8, 126, 127)
                ),
                jMethod("classCMeth", false, 10, 10, 157, 158)
            ),
            jMethod("classAMeth2", false, 12, 12, 185, 186)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
    Class tlClass = expected.topLevelClasses.get(0);
    Class innerClass1 = tlClass.innerClasses.get(0);
    Class innerClass2 = tlClass.innerClasses.get(1);
    Class subInnerClass = innerClass2.innerClasses.get(0);
    assertEquals("A", tlClass.getName());
    assertEquals("A$B", innerClass1.getName());
    assertEquals("A$C", innerClass2.getName());
    assertEquals("A$C$D", subInnerClass.getName());
  }

  @Test
  public void TestMethodsWithThrowsDeclarations() {
    // testing that the classStack works
    String fileContent = """
        abstract class ThrowClass {
          public abstract void method1() throws IOException;
          
          protected static int errorCode(int n) throws ArithmeticException, RuntimeException {
            return n / (n-1);
          }
          
          public static void main() throws IOException, RuntimeException {
            int err = errorCode(1);
            throw new RuntimeException(String.valueOf(err));
          }
        }""";
    JavaFile expected = jFile(
        jClass("ThrowClass", true,
            jMethod("errorCode", false, 4, 6, 168, 194),
            jMethod("main", true, 8, 11, 262, 347)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestInheritedClassWithSuperCallAndThisCall() {
    String fileContent = """
        class Dog {
          String name;
          int age;
          public Dog(String name, int age) {
            this.name = name;
            this.age = age;
          }
          String speak() {
            return "woof";
          }
        }
        class SmallDog extends Dog {
          boolean amSmall;
          public SmallDog(String name, int age) {
            super(name, age);
            amSmall = true;
            super.speak();
          }
          public SmallDog(String name, int age, boolean small) {
            this(name, age);
            amSmall = small;
          }
          @Override
          String speak() {
            if (amSmall) {
              return "wuf!";
            } else {
              return super.speak();
            }
          }
        }""";
    JavaFile expected = jFile(
        jClass("Dog",
            jMethod("Dog",
                jBlock(CONSTRUCTOR, 4, 7, 74, 120)
            ),
            jMethod("speak", false, 8, 10, 139, 162)
        ),
        jClass("SmallDog",
            jMethod("SmallDog",
                jBlock(CONSTRUCTOR, 14, 18, 254, 319, "\n    super(name, age);".length())
            ),
            jMethod("SmallDog",
                jBlock(CONSTRUCTOR, 19, 22, 376, 422, "\n    this(name, age);".length())
            ),
            jMethod("speak", false, 24, 30, 453, 544,
                jBlock(BLOCK, 25, 27, 472, 499),
                jBlock(BLOCK, 27, 29, 506, 540)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
