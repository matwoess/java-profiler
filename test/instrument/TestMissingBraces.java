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
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
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
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
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
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
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
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 16, 62, 269));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 5, 94, 104));
    expectedBlocks.add(getBlock(clazz, meth, 6, 8, 127, 139));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 9, 9, 144, 189));
    expectedBlocks.add(getBlock(clazz, meth, 11, 13, 204, 241));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 12, 12, 219, 227));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 12, 12, 232, 239));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestDoubleWhileAndIfElse() {
    String fileContent = String.format(baseTemplate, """
        int x = 0;
        while (false) while(true)
          if(1==2)
            return;
          else
            x=1;
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(5, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 10, 62, 147));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 8, 91, 142));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 8, 103, 142));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 5, 6, 114, 126));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 7, 8, 133, 142));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestDoWhile() {
    String fileContent = String.format(baseTemplate, """
        int x = 0;
        do x+=1; while (x<5);
        do
          x+=1;
        while (x<10);
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 9, 62, 129));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 4, 80, 86));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 5, 6, 102, 110));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestForAndForEach() {
    String fileContent = String.format(baseTemplate, """
        int[] array = new int[5];
        for (int i = 0; i < 5; i++)
          array[i] = i;
        for (int val : array) System.out.println(val);
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 8, 62, 188));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 5, 120, 136));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 6, 6, 158, 183));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestSwitch() {
    String fileContent = String.format(baseTemplate, """
       int x = 1;
       switch (x) {
         case 1: {
           x += 3;
           break;
         }
         case 2: case 3:
         case 4:
          x *= 2;
          x = x - 1;
         case 5:
           break;
         default: break;
       }
        """, "");
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(7, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 18, 62, 228));
    expectedBlocks.add(getBlock(clazz, meth, 5, 8, 102, 129));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 9, 9, 139, 139));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 9, 9, 147, 147));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 10, 12, 157, 182));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 13, 14, 192, 203));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 15, 15, 214, 221));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
