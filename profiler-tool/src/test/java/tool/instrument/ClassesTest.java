package tool.instrument;

import org.junit.jupiter.api.Test;
import tool.model.JClass;
import tool.model.JavaFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.*;
import static tool.model.JumpStatement.RETURN;
import static tool.model.JumpStatement.THROW;

public class ClassesTest {
  @Test
  public void testAbstractClass() {
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
            jMethod("toString", 6, 11, 139, 234).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testInnerClassAndStaticBlocks() {
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
        jClass("Classes",
            jBlock(STATIC, 4, 6, 50, 65),
            jMethod("main", 8, 21, 109, 471,
                jBlock(LOOP, 12, 20, 234, 467),
                jBlock(BLOCK, 14, 16, 285, 352),
                jBlock(BLOCK, 16, 18, 359, 427)
            ),
            jClass("PetFarm",
                jBlock(STATIC, 26, 28, 561, 590)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }


  @Test
  public void testDeepInnerClasses() {
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
        jClass("InnerClasses",
            jMethod("main", 2, 9, 70, 275),
            jClass("Inner",
                jMethod("level1", 11, 13, 318, 362),
                jClass("Sub",
                    jMethod("level2", 15, 17, 407, 455),
                    jClass("SubSub",
                        jMethod("level3", 19, 21, 507, 559)
                    )
                )
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testClassWithInheritanceAndConstructors() {
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
            jMethod("toString", 6, 11, 139, 234).withJump(RETURN)
        ),
        jClass("Dog",
            jConstructor("Dog", 15, 18, 298, 344),
            jMethod("speak", 19, 19, 373, 391).withJump(RETURN)
        ),
        jClass("Cat",
            jConstructor("Cat", 23, 26, 455, 501),
            jMethod("speak", 27, 27, 530, 548).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMembersWithInitBlocks() {
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
            jMethod("doNothing", 9, 9, 263, 264)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testMembersWithImplicitInitializers() {
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
            jMethod("doNothing", 7, 7, 177, 178)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testSubClassWithFollowingMethod() {
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
                jMethod("classBMeth", 3, 3, 45, 46)
            ),
            jMethod("classAMeth1", 5, 5, 73, 74),
            jClass("C",
                jClass("D",
                    jMethod("classDMeth", 8, 8, 126, 127)
                ),
                jMethod("classCMeth", 10, 10, 157, 158)
            ),
            jMethod("classAMeth2", 12, 12, 185, 186)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
    JClass tlClass = expected.topLevelClasses.get(0);
    JClass innerClass1 = tlClass.innerClasses.get(0);
    JClass innerClass2 = tlClass.innerClasses.get(1);
    JClass subInnerClass = innerClass2.innerClasses.get(0);
    assertEquals("A", tlClass.getName());
    assertEquals("A$B", innerClass1.getName());
    assertEquals("A$C", innerClass2.getName());
    assertEquals("A$C$D", subInnerClass.getName());
  }

  @Test
  public void testMethodsWithThrowsDeclarations() {
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
        jClass("ThrowClass",
            jMethod("errorCode", 4, 6, 168, 194).withJump(RETURN),
            jMethod("main", 8, 11, 262, 347).withJump(THROW)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testInheritedClassWithSuperCallAndThisCall() {
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
            jMethod("speak", 8, 10, 139, 162).withJump(RETURN)
        ),
        jClass("SmallDog",
            jMethod("SmallDog",
                jBlock(CONSTRUCTOR, 14, 18, 254, 319, "\n    super(name, age);".length())
            ),
            jMethod("SmallDog",
                jBlock(CONSTRUCTOR, 19, 22, 376, 422, "\n    this(name, age);".length())
            ),
            jMethod("speak", 24, 30, 453, 544,
                jBlock(BLOCK, 25, 27, 472, 499).withJump(RETURN),
                jBlock(BLOCK, 27, 29, 506, 540).withJump(RETURN)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void testClassAndConstructorRefs() {
    String fileContent = """
        class WithRefs {
          static Class<?> classRef = WithRefs.class;
          Supplier<WithRefs> constructorRef = WithRefs::new;
            
          public WithRefs() {
            updateRefs();
          }
          public void updateRefs() {
            classRef = WithRefs.class;
            this.constructorRef = WithRefs::new;
            this.constructorRef = getConstructorRef();
            classRef.toString().chars().average().orElseThrow(RuntimeException::new);
            Consumer<String> anotherRef = Integer::parseInt;
            anotherRef.accept("5");
            anotherRef = System.out::println;
            anotherRef.accept("updated ref variables");
            System.out.println(List.of(1,2,3).toArray(Integer[]::new));
          }
          Supplier<WithRefs> getConstructorRef() {
            return WithRefs::new;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("WithRefs",
            jConstructor("WithRefs", 5, 7, 137, 159),
            jMethod("updateRefs", 8, 18, 188, 620),
            jMethod("getConstructorRef", 19, 21, 663, 693).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
