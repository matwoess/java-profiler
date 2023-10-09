package tool.instrument;

import tool.model.JavaFile;
import org.junit.jupiter.api.Test;

import static tool.instrument.TestInstrumentUtils.parseJavaFile;
import static tool.instrument.TestProgramBuilder.*;
import static tool.model.BlockType.*;
import static tool.model.JumpStatement.RETURN;
import static tool.model.JumpStatement.THROW;

public class RecordsTest {
  @Test
  void testRecordWith1FieldAndNoBody() {
    String fileContent = """
        record Record(int x) {}
        """;
    JavaFile expected = jFile(
        jClass("Record")
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCanonicalConstructor() {
    String fileContent = """
        record LenWidth(double length, double width) {
          LenWidth(double length, double width) {
            if (length < 0 || width < 0) {
              throw new IllegalArgumentException(String.format("Invalid dimensions: %f, %f", length, width));
            }
            this.length = length;
            this.width = width;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("LenWidth",
            jConstructor("LenWidth", 2, 8, 88, 285,
                jBlock(BLOCK, 3, 5, 123, 231).withJump(THROW)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCanonicalConstructorsAndCompactForm() {
    String fileContent = """
        record LenWidth(double length, double width) {
          LenWidth {
            if (length < 0 || width < 0) {
              throw new IllegalArgumentException(String.format("Invalid dimensions: %f, %f", length, width));
            }
          }
          LenWidth(String lenStr, String widthStr) {
            this(Double.parseDouble(lenStr), Double.parseDouble(widthStr));
          }
        }
        """;
    int thisCallOffset = "\n    this(Double.parseDouble(lenStr), Double.parseDouble(widthStr));".length();
    JavaFile expected = jFile(null, 0,
        jClass("LenWidth",
            jConstructor("LenWidth", 2, 6, 59, 206,
                jBlock(BLOCK, 3, 5, 94, 202).withJump(THROW)
            ),
            jMethod("LenWidth",
                jBlock(CONSTRUCTOR, 7, 9, 251, 323, thisCallOffset)
            )
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithCustomFieldAccess() {
    String fileContent = """
        record Coordinate(int x, int y) {
          @Override
          public int x() {
            System.out.println("x was accessed with a value of: " + x);
            return x;
          }
          @Override
          public int y() {
            System.out.println("y was accessed with a value of: " + y);
            return x;
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Coordinate",
            jMethod("x", 3, 6, 64, 146).withJump(RETURN),
            jMethod("y", 8, 11, 177, 259).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testRecordWithStaticBlockMainMethodAndFunction() {
    String fileContent = """
        package recordPackage;
        public record Coordinate(int x, int y) {
          static int z;
          static {
            z = 50;
          }
          public static void main(String[] args) {
            Coordinate coord = new Coordinate(1, 5);
            coord.x();
            System.out.println(coord.y + z);
            System.out.println(coord.manhattanDistanceTo(new Coordinate(4, -1)));
          }
          int manhattanDistanceTo(Coordinate other) {
            return Math.abs(x - other.x()) + Math.abs(y - other.y());
          }
        }
        """;
    JavaFile expected = jFile("recordPackage", 22,
        jClass("Coordinate",
            jBlock(STATIC, 4, 6, 90, 106),
            jMethod("main", 7, 12, 149, 324),
            jMethod("manhattanDistanceTo", 13, 15, 370, 436).withJump(RETURN)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  void testNonClassRecordDefinitions() {
    String fileContent = """
        record Record() {
          private static final List<String> record = new ArrayList<>();
          public void record(String record) {
            this.record.add(record);
          }
          public String getRecord() {
            return record.get(0);
          }
          public static void main(String[] args) {
            Record record = new Record();
            Record.record.add("record");
            record.record("record");
            System.out.println(record.getRecord());
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("Record",
            jMethod("record", 3, 5, 119, 152),
            jMethod("getRecord", 6, 8, 182, 212).withJump(RETURN),
            jMethod("main", 9, 14, 255, 399)
        )
    );
    TestInstrumentUtils.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
