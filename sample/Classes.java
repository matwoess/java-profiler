package profiler;

import java.util.ArrayList;
import java.util.List;

public class Classes {

  public static void main(String[] args) {
    PetFarm farm = new PetFarm();
    farm.pets.add(new Dog("Barky", 2));
    farm.pets.add(new Cat("Molly", 7));
    for (Pet pet : farm.pets) {
      String name = pet.name;
      int age = pet.age;
      String output;
      if (age > 3) {
        output = String.format("%s of age %d says %s", name, age, pet.speak());
      } else {
        output = String.format("Young %s says %s", name, pet.speak());
      }
      System.out.println(output);
    }
  }
}


static class PetFarm {
  List<Pet> pets = new ArrayList<>();

  static {
    pets.add(new Cat("Bob", 5));
  }
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

  public Cat(String name, int age) {
    this.name = name;
    this.age = age;
  }

  @Override
  String speak() {
    return "meow.";
  }
}
