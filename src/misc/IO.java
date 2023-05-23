package misc;

import model.Block;
import model.Class;
import model.JavaFile;
import model.Method;

import java.io.FileOutputStream;
import java.io.IOException;

import static misc.Constants.metadataFile;

public class IO {

  public static void exportMetadata(Metadata metadata) {
    StringBuilder builder = new StringBuilder();
    builder.append(metadata.blocksCount).append(" ");
    for (JavaFile jFile : metadata.javaFiles) {
      builder.append(jFile.sourceFile.toUri()).append(" ");
      for (Class clazz : jFile.foundClasses) {
        builder.append(clazz.name).append(" ");
        for (Method meth : clazz.methods) {
          builder.append(meth.name).append(" ");
          for (Block block : meth.blocks) {
            builder.append(block.beg).append(" ");
            builder.append(block.end).append(" ");
            builder.append(block.blockType.ordinal()).append(" ");
          }
        }
        builder.append("#").append(" ");
      }
    }
    try (FileOutputStream fos = new FileOutputStream(metadataFile.toFile())) {
      byte[] data = builder.toString().getBytes();
      fos.write(data, 0, data.length);
      fos.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public record Metadata(int blocksCount, JavaFile[] javaFiles) {
  }
}
