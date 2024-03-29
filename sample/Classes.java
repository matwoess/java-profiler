import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Classes {
  static int i;

  static {
    i = 0;
  }

  public static void main(String[] args) throws IOException, RuntimeException {
    PetFarm farm = new PetFarm();
    farm.pets.add(new Dog("Barky", 2));
    farm.pets.add(new Cat("Molly", 7));
    for (Pet pet : farm.pets) {
      String name = pet.name;
      int age = pet.age;
      String output;
      if (age > 3) {
        output = String.format("%s of age %d says: %s", name, age, pet.speak());
      } else {
        output = String.format("Young %s says: %s", name, pet.speak());
      }
      System.out.println(output);
    }
  }

  static class PetFarm {
    static List<Pet> pets = new ArrayList<>();

    static {
      pets.add(new Cat("Bob", 5));
      System.out.println(strings[0] + intArray[0][1]);
    }
  }

  private static final String[] strings = {"String1", "String2"};
  private static final int[][] intArray = {{1, 2}, {123, 456}};
}

abstract class Pet {
  String name;
  int age;

  abstract String speak();  // method without block

  @Override
  public String toString() {
    return "Pet{" +
        "name='" + name + '\'' +
        ", age=" + age +
        '}';
  }
}

class Dog extends Pet {

  public Dog(String name, int age) {
    this.name = name;
    this.age = age;
  }

  @Override
  String speak() {
    return "woof!";
  }
}

class Cat extends Pet {
  static Class<?> classRef = Pet.class;
  BiFunction<String, Integer, Pet> constructorRef = Cat::new;

  public Cat(String name, int age) {
    this.name = name;
    this.age = age;
    updateRefs();
  }

  public void updateRefs() {
    classRef = Dog.class;
    this.constructorRef = Cat::new;
    classRef.toString().chars().average().orElseThrow(RuntimeException::new);
    Consumer<String> anotherRef = Integer::parseInt;
    anotherRef.accept("5");
    anotherRef = System.out::println;
    anotherRef.accept("updated ref variables");
  }

  @Override
  String speak() {
    return "meow.";
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
}
