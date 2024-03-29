enum Adjective {
  LOW, MEDIUM, HIGH,
  WEAK, STRONG, GREAT
}

enum Empty {
}

enum Skill {
  SINGING, PLAY_INSTRUMENT, ORGANIZING;

  enum InnerEnum {
    D,E
  }

  public String lowercase() {
    return this.name().toLowerCase();
  }
}


public enum Enums {
  PRIMARY(Skill.SINGING, Adjective.GREAT, 1.5f),
  SECONDARY(Skill.PLAY_INSTRUMENT, Adjective.MEDIUM, 5e+4f),
  TERTIARY(Skill.ORGANIZING, Adjective.WEAK, .3f);

  private final Skill skill;
  private final Adjective adjective;
  public final float number;

  Enums(Skill skill, Adjective adj, float number) {
    this.skill = skill;
    this.adjective = adj;
    this.number = number;
  }

  public static final long ID;

  static {
    ID = 5L;
  }

  public Skill getSkill() {
    return skill;
  }

  private Adjective getAdjective() {
    return adjective;
  }

  private synchronized void printSummary() {
    if (adjective != null) {
      if (skill != null) {
        System.out.println(getAdjective().name() + " at " + getSkill().lowercase() + ", number: " + number);
      }
    }
  }

  static class ClassInEnum implements InterfaceInEnum {
    public static void printSkillLowerCase(Enums val) {
      System.out.println(val.skill.lowercase());
    }
  }

  private interface InterfaceInEnum {
    default String uppercase(Enums val) {
      return val.name().toUpperCase();
    }
  }

  public static void callMethods() {
    ClassInEnum.printSkillLowerCase(Enums.PRIMARY);
    new ClassInEnum().uppercase(Enums.TERTIARY);
  }

  public static void main(String[] args) {
    Enums e = PRIMARY;
    switch (e.getSkill()) {
      case SINGING:
        assert (e.getAdjective() == Adjective.GREAT);
      default:
        e.printSummary();
    }
    e = SECONDARY;
    e.printSummary();
    ClassInEnum.printSkillLowerCase(e);
    callMethods();
    System.out.println(WithAbstractMethods.Value1.description());
    WithAbstractMethods.Value2.printDescription();
  }
}

enum WithTrailingComma {
  A,B,
}

enum WithTrailingCommaAndSemicolon {
  A,B,;
}
enum WithTrailingCommaSemicolonAndFunction {
  A,B,;
  void print1() {
    System.out.println("1");
  }
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