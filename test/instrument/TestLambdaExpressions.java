package instrument;

import model.JavaFile;
import org.junit.jupiter.api.Test;

import static instrument.ProgramBuilder.*;
import static instrument.Util.baseTemplate;
import static instrument.Util.parseJavaFile;
import static model.BlockType.*;

public class TestLambdaExpressions {
  @Test
  public void TestSimpleForEach() {
    String fileContent = String.format(baseTemplate, """
        "xyz".chars().forEach(ch -> {
          System.out.println(ch);
        });
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 7, 62, 131),
                jBlock(BLOCK, 3, 5, 96, 124)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestAnonymousFunction() {
    String fileContent = String.format(baseTemplate, """
        Function<Integer, Integer> doubleFn = (num) -> { return num*2; };
        int result = doubleFn.apply(5);
        System.out.println(result);
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 7, 62, 197),
                jBlock(BLOCK, 3, 3, 115, 131)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestAnonymousSupplier() {
    String fileContent = String.format(baseTemplate, """
        Supplier<String> getHello = () -> {
          return "Hello";
        };
        System.out.println(getHello.get());
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 8, 62, 164),
                jBlock(BLOCK, 3, 5, 102, 122)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestLambdaStatementWithMissingBraces() {
    String fileContent = String.format(baseTemplate, """
        Function<Integer, Double> divideBy3 = num -> num / 3.0;
        double result = divideBy3.apply(7);
        System.out.println(result);
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 7, 62, 191),
                jBlock(SS_LAMBDA, 3, 3, 111, 122)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestLambdaWithParameterListAndBracesAroundStatement() {
    String fileContent = String.format(baseTemplate, """
        BiFunction<Float, Float, Float> addTogether = (x, y) -> (x + y);
        double result = addTogether.apply(7f, 5.6f);
        System.out.println(result);
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 7, 62, 209),
                jBlock(SS_LAMBDA, 3, 3, 122, 131)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestMoreComplexLambdasWithoutBlocks() {
    String fileContent = String.format(baseTemplate, """
        BiFunction<String, Predicate<String>, Integer> parseIntIf
            = (num, pred) -> pred.test(num) ? Integer.parseInt(num) : -1;
        Predicate<String> isNotBlank = (String str) -> !str.isBlank();
        int result = parseIntIf.apply("234", isNotBlank);
        System.out.println(result);
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 9, 62, 336),
                jBlock(SS_LAMBDA, 4, 4, 145, 190),
                jBlock(SS_LAMBDA, 5, 5, 237, 253)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestChainedStreamsWithLambdasAsParameter() {
    String fileContent = String.format(baseTemplate, """
        int[] array = new int[]{1, 2, 3, 4, 5, 6};
        Arrays.stream(array)
            .map(x -> x*2)
            .peek(x -> System.out.printf("%d ", x))
            .filter(x -> (x > 5))
            .reduce((acc, x) -> ((acc) + (x)))
            .ifPresent(possibleResult -> System.out.println("\\nRes: " + possibleResult));
        """, "");
    JavaFile expected = jFile(
        jClass("Main", true,
            jMethod("main", true,
                jBlock(METHOD, 2, 11, 62, 345),
                jBlock(SS_LAMBDA, 5, 5, 144, 149),
                jBlock(SS_LAMBDA, 6, 6, 164, 193),
                jBlock(SS_LAMBDA, 7, 7, 210, 219),
                jBlock(SS_LAMBDA, 8, 8, 243, 258),
                jBlock(SS_LAMBDA, 9, 9, 291, 339)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestClassLevelLambdaMembers() {
    String fileContent = """
        private class LambdaMembers {
          static Runnable printHello = () -> {
            System.out.println("Hello!");
          };
          static Function<Integer, Double> divideByTwo = x -> {
            return x / 2.0;
          };
          static Consumer<Double> printDouble = (d) -> {
            System.out.println(d);
          };
          public static void main(String[] args) {
            printHello.run();
            Stream.of(3, 6, 9).map(divideByTwo).forEach(printDouble);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("LambdaMembers", true,
            jBlock(BLOCK, 2, 4, 68, 106),
            jBlock(BLOCK, 5, 7, 163, 187),
            jBlock(BLOCK, 8, 10, 237, 268),
            jMethod("main", true,
                jBlock(METHOD, 11, 14, 312, 400)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }

  @Test
  public void TestClassLevelSingleStatementLambdas() {
    String fileContent = """
        class LambdaMembers {
          static Runnable printHello = () -> System.out.println("Hello!");
          static Function<Integer, Double> divideByTwo = x -> x / 2.0;
          static Consumer<Double> printDouble = (d) ->
            System.out.println(d);
          static Function<Integer, Integer> addTwo = x -> x + 2;
          static int[] ints = Arrays.stream(new int[]{5, 4}).map(x -> x * 2).filter(x -> x < 10).toArray();
          
          public static void main(String[] args) {
            printHello.run();
            Stream.of(3, 6, 9).map.(addTwo).map(divideByTwo).forEach(printDouble);
          }
        }
        """;
    JavaFile expected = jFile(
        jClass("LambdaMembers", true,
            jBlock(SS_LAMBDA, 2, 2, 58, 88),
            jBlock(SS_LAMBDA, 3, 3, 142, 151),
            jBlock(SS_LAMBDA, 4, 5, 198, 225),
            jBlock(SS_LAMBDA, 6, 6, 275, 282),
            jBlock(SS_LAMBDA, 7, 7, 344, 351),
            jBlock(SS_LAMBDA, 7, 7, 363, 371),
            jMethod("main", true,
                jBlock(METHOD, 9, 12, 426, 527)
            )
        )
    );
    Util.assertResultEquals(expected, parseJavaFile(fileContent));
  }
}
