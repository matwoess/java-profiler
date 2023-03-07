package instrument;

import common.Block;
import common.Class;
import common.Method;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static instrument.Util.getSingleStatementBlock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestSwitches {
  public String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
      }
      """;

  @Test
  public void TestNewSwitch() {
    String fileContent = String.format(baseTemplate, """
        String value = "aBcDe";
        switch (value) {
          case "aBcDe" -> {
            if (value.toUpperCase().equals("ABCDE")) {
              System.out.println("as expected");
            }
          }
          case "other value" -> System.out.println("unexpected");
          case "" -> {
            break;
          }
          default -> throw new RuntimeException("should never happen");
        }
        """);
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(6, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 17, 62, 384));
    expectedBlocks.add(getBlock(clazz, meth, 5, 9, 127, 225));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 174, 221));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 10, 10, 249, 283));
    expectedBlocks.add(getBlock(clazz, meth, 11, 13, 298, 313));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 14, 14, 326, 377));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestSwitchExpression() {
    String fileContent = String.format(baseTemplate, """
        int dependingOn = 5;
        int result = switch (dependingOn) {
          case 5 -> {
            yield 2;
          }
          case 1 -> 5;
          default -> {
            if (dependingOn < 10) {
              System.out.println("none of the above");
              yield 0;
            } else {
              yield -1;
            }
          }
        };
        System.out.println("result=" + result);
        """);
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(6, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 20, 62, 361));
    expectedBlocks.add(getBlock(clazz, meth, 5, 7, 137, 154));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 8, 8, 166,169));
    expectedBlocks.add(getBlock(clazz, meth, 9, 16, 184, 313));
    expectedBlocks.add(getBlock(clazz, meth, 10, 13, 212, 280));
    expectedBlocks.add(getBlock(clazz, meth, 13, 15, 287, 309));
    assertIterableEquals(expectedBlocks, blocks);
  }


}
