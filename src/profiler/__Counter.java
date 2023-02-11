package profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Class {
  String name;
  int count;
}
class Method {
  String name;
  int count;
}
class Block {
  Class clazz;
  Method method;
  int beg, end;
  boolean isMethodBlock;
  int count;
}

public class __Counter {
  private static Block[] blocks;
  public static synchronized void inc(int n) {
    blocks[n].count++;
    blocks[n].method.count++;
    blocks[n].clazz.count++;
  }
  public static synchronized void init(String fileName) {
    try {
      String fileContent = Files.readString(Path.of(fileName));
      Class curClass = null;
      Method curMeth = null;
      String[] metaData = fileContent.split(" ");
      int curBlock = 0;
      for (int i = 0; i < metaData.length; i++) {
        String data = metaData[i];
        if (blocks == null) {
          int nBlocks = Integer.parseInt(data);
          blocks = new Block[nBlocks];
          continue;
        }
        if ("#".equals(data)) {
          curClass = null;
          curMeth = null;
          continue;
        }
        if (curClass == null) {
          curClass = new Class();
          curClass.name = data;
          continue;
        }
        char ch0 = data.charAt(0);
        if (curMeth == null || ch0 < '0' || ch0 > '9') {
          curMeth = new Method();
          curMeth.name = data;
          continue;
        }
        Block block = new Block();
        block.beg = Integer.parseInt(data);
        block.end = Integer.parseInt(metaData[i + 1]);
        block.isMethodBlock = "1".equals(metaData[i + 2]);
        i += 2;
        block.clazz = curClass;
        block.method = curMeth;
        blocks[curBlock++] = block;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static synchronized void save(String fileName) {
    Path countsFile = Path.of(fileName);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < blocks.length; i++) {
      Block block = blocks[i];
      builder.append(block.count).append(" ");
    }
    try {
      Files.writeString(countsFile, builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
