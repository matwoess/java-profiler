import java.io.File;
import java.io.FilenameFilter;
 import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnonymousClasses {
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

  public static void main(String[] args) throws IOException {
    Path currentDir = Path.of(".");
    File firstJFile = firstJavaFile(currentDir);
    System.out.println("first java file in current directory is: " + firstJFile);
    var arrayList = new ArrayList<Integer>();
    arrayList.add(3);
    arrayList.add(1);
    arrayList.add(2);
    getSortedIntegers(arrayList);
    new AnonymousClasses() {
      @Override
      public int hashCode() {
        return super.hashCode();
      }
    };
    new Classes().main(null);
  }
}
