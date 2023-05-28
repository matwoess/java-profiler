package instrument;

import model.Block;
import model.Class;
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
    clazz = new Class("Main.Anonymous");
    meth = new Method("accept");
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 10, 13, 289, 361));
    clazz = new Class("Main.Anonymous.X");
    meth = new Method("methodInX");
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 16, 18, 408, 503));
    clazz = new Class("Main.Anonymous");
    meth = new Method("returnTrue");
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 21, 24, 544, 590));
    clazz = new Class("Main", true);
    meth = new Method("firstJavaFile");
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 26, 28, 653, 685));
    expectedBlocks.add(getBlock(BLOCK, clazz, meth, 28, 30, 692, 713));
    assertIterableEquals(expectedBlocks, blocks);
  }
}
