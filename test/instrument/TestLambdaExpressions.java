package instrument;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static instrument.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestLambdaExpressions {
  public String baseTemplate = """
      public class Main {
        public static void main(String[] args) {
          %s
        }
      }
      """;

  @Test
  public void TestSimpleForEach() {
    String fileContent = String.format(baseTemplate, """
        "xyz".chars().forEach(ch -> {
          System.out.println(ch);
        });
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 7, 62, 131));
    expectedBlocks.add(getBlock(clazz, meth, 3, 5, 96, 124));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestAnonymousFunction() {
    String fileContent = String.format(baseTemplate, """
        Function<Integer, Integer> doubleFn = (num) -> { return num*2; };
        int result = doubleFn.apply(5);
        System.out.println(result);
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 7, 62, 197));
    expectedBlocks.add(getBlock(clazz, meth, 3, 3, 115, 131));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestAnonymousSupplier() {
    String fileContent = String.format(baseTemplate, """
        Supplier<String> getHello = () -> {
          return "Hello";
        };
        System.out.println(getHello.get());
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 8, 62, 164));
    expectedBlocks.add(getBlock(clazz, meth, 3, 5, 102, 122));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestLambdaStatementWithMissingBraces() {
    String fileContent = String.format(baseTemplate, """
        Function<Integer, Double> divideBy3 = num -> num / 3.0;
        double result = divideBy3.apply(7);
        System.out.println(result);
        """);
    Function<Integer, Double> divideBy3 = num -> num / 3.0;
    double result = divideBy3.apply(7);
    System.out.println(result);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 7, 62, 191));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 3, 3, 111, 122));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestLambdaWithParameterListAndBracesAroundStatement() {
    String fileContent = String.format(baseTemplate, """
        BiFunction<Float, Float, Float> addTogether = (x, y) -> (x + y);
        double result = addTogether.apply(7f, 5.6f);
        System.out.println(result);
        """);
    BiFunction<Float, Float, Float> addTogether = (x, y) -> (x + y);
    double result = addTogether.apply(7f, 5.6f);
    System.out.println(result);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 7, 62, 209));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 3, 3, 122, 131));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestMoreComplexLambdasWithoutBlocks() {
    String fileContent = String.format(baseTemplate, """
        BiFunction<String, Predicate<String>, Integer> parseIntIf
            = (num, pred) -> pred.test(num) ? Integer.parseInt(num) : -1;
        Predicate<String> isNotBlank = (String str) -> !str.isBlank();
        int result = parseIntIf.apply("234", isNotBlank);
        System.out.println(result);
        """);
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 9, 62, 336));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 4, 145, 190));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 5, 5, 237, 253));
    assertIterableEquals(expectedBlocks, blocks);
  }

  @Test
  public void TestChainedLambdasOnArray() {
    String fileContent = String.format(baseTemplate, """
        int[] array = new int[]{1, 2, 3, 4, 5, 6};
        Arrays.stream(array)
            .map(x -> x*2)
            .peek(x -> System.out.printf("%d ", x))
            .filter(x -> x > 5)
            .ifPresent(possibleResult -> System.out.println("\\nRes: " + possibleResult));
        """);
    int[] array = new int[]{1, 2, 3, 4, 5, 6};
    Arrays.stream(array)
        .map(x -> x*2)
        .peek(x -> System.out.printf("%d ", x))
        .filter(x -> x > 5)
        .reduce((acc, x) -> acc + x)
        .ifPresent(possibleResult -> System.out.println("\nRes: " + possibleResult));
    List<Parser.Block> blocks = getFoundBlocks(fileContent);
    assertEquals(3, blocks.size());
    List<Parser.Block> expectedBlocks = new ArrayList<>();
    Parser.Class clazz = new Parser.Class("Main", true);
    Parser.Method meth = new Parser.Method("main", true);
    expectedBlocks.add(getMethodBlock(clazz, meth, 2, 9, 62, 336));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 4, 4, 145, 190));
    expectedBlocks.add(getSingleStatementBlock(clazz, meth, 5, 5, 237, 253));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
