package instrument;

import model.Block;
import model.Class;
import model.ClassType;
import model.Method;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static instrument.Util.*;
import static model.BlockType.*;
import static model.BlockType.BLOCK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestAnonymousClasses {
  @Test
  public void TestAsArgumentInClassLevelMethod() {
    String fileContent = String.format(baseTemplate, "", """
        static File firstJavaFile(Path directory) {
          File[] allJavaFiles = directory.toFile().listFiles(new FilenameFilter() {
            boolean isTrue = false;
             
            @Override
            public boolean accept(File file, String name) {
              isTrue = returnTrue();
              return name.endsWith(".java");
            }
             
            class X {
              static void methodInX() {
                System.out.println("Hello from inside a nested class in an anonymous class.");
              }
            }
             
            public boolean returnTrue() {
              X.methodInX();
              return true;
            }
          });
          if (allJavaFiles != null && allJavaFiles.length > 0) {
            return allJavaFiles[0];
          } else {
            return null;
          }
        }
         """);
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(7, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 4, 62, 71));
    meth = new Method("firstJavaFile");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 5, 31, 118, 715));
    Class innerClass = new Class(null, ClassType.ANONYMOUS, false);
    innerClass.setParentClass(clazz);
    meth = new Method("accept");
    expectedBlocks.add(getBlock(METHOD, innerClass, meth, 10, 13, 289, 361));
    Class subInnerClass = new Class("X");
    subInnerClass.setParentClass(innerClass);
    meth = new Method("methodInX");
    expectedBlocks.add(getBlock(METHOD, subInnerClass, meth, 16, 18, 408, 503));
    meth = new Method("returnTrue");
    expectedBlocks.add(getBlock(METHOD, innerClass, meth, 21, 24, 544, 590));
    meth = new Method("firstJavaFile");
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 26, 28, 653, 685));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 28, 30, 692, 713));
    assertIterableEquals(expectedBlocks, blocks);
    assertEquals("Main$1", innerClass.getName());
    assertEquals("Main$1$X", subInnerClass.getName());
  }

  @Test
  public void TestAs2ndArgumentInClassLevelMethodWithGenericType() {
    String fileContent = String.format(baseTemplate, "", """
        static List<Integer> getSortedIntegers(List<Integer> arrayList) {
          Collections.sort(arrayList, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
              if (i1.equals(i2)) {
                return 0;
              }
             
              return i1 < i2 ? -1 : 1;
            }
          });
          return arrayList;
        }
        """);
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(4, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 4, 62, 71));
    meth = new Method("getSortedIntegers");
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 5, 17, 140, 380));
    Class innerClass = new Class(null, ClassType.ANONYMOUS, false);
    innerClass.setParentClass(clazz);
    meth = new Method("compare");
    expectedBlocks.add(getBlock(METHOD, innerClass, meth, 8, 14, 261, 352));
    expectedBlocks.add(getBlock(BLOCK, innerClass, meth, 9, 11, 288, 314));
    assertIterableEquals(expectedBlocks, blocks);
    assertEquals("Main$1", innerClass.getName());
  }

  @Test
  public void TestAsStatementStartInMethod() {
    String fileContent = String.format(baseTemplate, """
        new Main() {
          @Override
          public int hashCode() {
            return super.hashCode();
          }
        };
        """, "");
    List<Block> blocks = getFoundBlocks(fileContent);
    assertEquals(2, blocks.size());
    List<Block> expectedBlocks = new ArrayList<>();
    Class clazz = new Class("Main", true);
    Method meth = new Method("main", true);
    expectedBlocks.add(getBlock(METHOD, clazz, meth, 2, 10, 62, 158));
    Class innerClass = new Class(null, ClassType.ANONYMOUS, false);
    innerClass.setParentClass(clazz);
    meth = new Method("hashCode");
    expectedBlocks.add(getBlock(METHOD, innerClass, meth, 5, 7, 117, 150));
    assertIterableEquals(expectedBlocks, blocks);
    assertEquals("Main$1", innerClass.getName());
  }

}
