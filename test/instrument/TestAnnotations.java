package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.parseJavaFile;

public class TestAnnotations {
  @Test
  public void TestClassAnnotations() {
    String fileContent = """
        @SuppressWarnings("unchecked")
        @Deprecated
        public class Annotations {
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations", true,
            jMethod("main", true, 4, 5, 112, 116)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMethodAnnotations() {
    String fileContent = """
        public class Annotations {
          @Deprecated
          @SuppressWarnings({"unused", "unchecked"})
          public static void main(String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations", true,
            jMethod("main", true, 4, 5, 128, 132)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestArgumentAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(@SuppressWarnings("null") String[] args) {
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations", true,
            jMethod("main", true, 2, 3, 95, 99)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestStatementAnnotations() {
    String fileContent = """
        public class Annotations {
          public static void main(String[] args) {
            @SuppressWarnings("ParseError")
            Integer i1 = Integer.parseInt("000555");
            @SuppressWarnings("unused")
            String s = "a";
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Annotations", true,
            jMethod("main", true, 2, 7, 69, 206)
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
