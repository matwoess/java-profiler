package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestClasses {
  @Test
  public void TestAbstractClass() {
    String abstractClass = """
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
    List<Parser.Block> blocks = getFoundBlocks(abstractClass);
    assertEquals(1, blocks.size());
    Parser.Block expectedBlock = getMethodBlock("Pet", "toString", 6, 11, 139, 234);
    assertEquals(expectedBlock, blocks.get(0));
  }

  @Test
  public void TestInnerClassAndStaticBlocks() {
    String innerClassAndStaticBlocks = """
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
    List<Parser.Block> blocks = getFoundBlocks(innerClassAndStaticBlocks);
    assertEquals(6, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getBlock("Classes", "static", 4, 6, 50, 65));
    expectedBlocks.add(getMethodBlock("Classes", "main", 8, 21, 109, 471));
    expectedBlocks.add(getBlock("Classes", "main", 12, 20, 234, 467));
    expectedBlocks.add(getBlock("Classes", "main", 14, 16, 285, 352));
    expectedBlocks.add(getBlock("Classes", "main", 16, 18, 359, 427));
    expectedBlocks.add(getBlock("Classes.PetFarm", "static", 26, 28, 561, 590));
    assertIterableEquals(expectedBlocks, blocks);
  }


  @Test
  public void TestDeepInnerClasses() {
    String deepInnerClasses = """
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
    List<Parser.Block> blocks = getFoundBlocks(deepInnerClasses);
    assertEquals(4, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getMethodBlock("InnerClasses", "main", 2, 9, 70, 275));
    expectedBlocks.add(getMethodBlock("InnerClasses.Inner", "level1", 11, 13, 318, 362));
    expectedBlocks.add(getMethodBlock("InnerClasses.Inner.Sub", "level2", 15, 17, 407, 455));
    expectedBlocks.add(getMethodBlock("InnerClasses.Inner.Sub.SubSub", "level3", 19, 21, 507, 559));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestClassWithInheritanceAndConstructors() {
    String classWithInheritance = """
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
    List<Parser.Block> blocks = getFoundBlocks(classWithInheritance);
    assertEquals(5, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    expectedBlocks.add(getMethodBlock("Pet", "toString", 6, 11, 139, 234));
    expectedBlocks.add(getMethodBlock("Dog", "Dog", 15, 18, 298, 344));
    expectedBlocks.add(getMethodBlock("Dog", "speak", 19, 19, 373, 391));
    expectedBlocks.add(getMethodBlock("Cat", "Cat", 23, 26, 455, 501));
    expectedBlocks.add(getMethodBlock("Cat", "speak", 27, 27, 530, 548));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
