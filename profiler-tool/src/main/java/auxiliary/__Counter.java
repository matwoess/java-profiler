package auxiliary;

import java.io.*;
import java.util.function.Supplier;

public class __Counter {
  static {
    init("../metadata.dat");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> save(("../counts.dat"))));
  }

  private static int[] blockCounts;

  public static void inc(int n) {
    blockCounts[n]++;
  }
  synchronized public static void incSync(int n) {
    blockCounts[n]++;
  }

  public static void init(String fileName) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
      int nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
      blockCounts = new int[nBlocks];
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static void save(String fileName) {
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(fileName))) {
      dis.writeInt(blockCounts.length);
      for (int blockCount : blockCounts) {
        dis.writeInt(blockCount);
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static void incLambda(int n, Runnable method) {
    __Counter.inc(n);
    method.run();
  }

  public static <T> T incLambda(int n, Supplier<T> function) {
    __Counter.inc(n);
    return function.get();
  }

  synchronized public static void incLambdaSync(int n, Runnable method) {
    __Counter.inc(n);
    method.run();
  }

  synchronized public static <T> T incLambdaSync(int n, Supplier<T> function) {
    __Counter.inc(n);
    return function.get();
  }
}
