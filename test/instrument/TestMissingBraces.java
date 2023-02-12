package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestMissingBraces {
  public String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
      }
      """;

  @Test
  public void TestIf() {
    String fileContent = String.format(baseTemplate, """
        if (true == false)return;
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main");
    Parser.Method meth = new Parser.Method("main");
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 5, 62, 97));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 3, 3, 85, 92));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestIfElse() {
    String fileContent = String.format(baseTemplate, """
        if (true == false) break;
        else continue;
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main");
    Parser.Method meth = new Parser.Method("main");
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 6, 62, 112));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 3, 3, 85, 92));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 4, 97, 107));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestIfElseifElse() {
    String fileContent = String.format(baseTemplate, """
        if (true == false) break;
        else if (true == true) return;
        else continue;
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main");
    Parser.Method meth = new Parser.Method("main");
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 7, 62, 143));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 3, 3, 85, 92));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 4, 115, 123));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 5, 5, 128, 138));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestMixedIfs() {
    String fileContent = String.format(baseTemplate, """
        int x = 50;
        if (x % 2 == 0)
          x += 1;
        else if (x % 2 == 1) {
          x += 3;
        }
        else throw new RuntimeException("invalid state");
            
        if (x > 51) {
          if (x == 53) return; else x = 0;
        }
        System.out.println(x);
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(7, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main");
    Parser.Method meth = new Parser.Method("main");
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 16, 62, 269));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 5, 94, 104));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 127, 139));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 9, 9, 144, 189));
    expectedBlocks.add(getBlock(clazz, meth, 11, 13, 204, 241));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 12, 12, 219, 227));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 12, 12, 232, 239));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
