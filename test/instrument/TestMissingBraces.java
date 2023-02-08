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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 5, 61, 96));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 3, 3, 84, 91));
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 6, 61, 111));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 3, 3, 84, 91));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 4, 4, 93, 106));
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 7, 61, 142));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 3, 3, 84, 91));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 4, 4, 114, 122));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 5, 5, 124, 137));
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
    expectedBlocks.add(getMethodBlock("Main", "main", 2, 16, 61, 268));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 4, 5, 93, 103));
    expectedBlocks.add(getBlock("Main", "main", 6, 8, 126, 138));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 9, 9, 140, 188));
    expectedBlocks.add(getBlock("Main", "main", 11, 13, 203, 240));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 12, 12, 218, 226));
    expectedBlocks.add(getSingleStatementBlock("Main", "main", 12, 12, 228, 238));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
