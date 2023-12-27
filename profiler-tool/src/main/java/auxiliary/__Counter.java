package auxiliary;

import java.io.*;
import java.util.function.Supplier;

/**
 * This class is used to count the number of times a block of code is executed.
 */
public class __Counter {
  static {
    init(".profiler/metadata.dat");
    Runtime.getRuntime().addShutdownHook(new Thread(() -> save((".profiler/counts.dat"))));
  }

  private static int[] blockCounts;

  /**
   * Increments the counter for the given block.
   *
   * @param n the block id
   */
  public static void inc(int n) {
    blockCounts[n]++;
  }
  /**
   * Increments the counter for the given block, in a synchronized way.
   *
   * @param n the block id
   */
  synchronized public static void incSync(int n) {
    blockCounts[n]++;
  }

  /**
   * Initializes the counter-array with the given number of blocks.
   * <p>
   * The number of blocks is the first value of the metadata file.
   *
   * @param fileName the location of the metadata file
   */
  private static void init(@SuppressWarnings("SameParameterValue") String fileName) {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
      int nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
      blockCounts = new int[nBlocks];
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the counter-array to the given file.
   *
   * @param fileName the location of the file to save the counter-array to
   */
  private static void save(@SuppressWarnings("SameParameterValue") String fileName) {
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(fileName))) {
      dis.writeInt(blockCounts.length);
      for (int blockCount : blockCounts) {
        dis.writeInt(blockCount);
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Call {@link #inc(int)} with the block id and then execute the given lambda <code>Runnable</code>.
   * @param n the block id
   * @param method the lambda <code>Runnable</code> to execute
   */
  public static void incLambda(int n, Runnable method) {
    __Counter.inc(n);
    method.run();
  }

  /**
   * Overloaded version of {@link #incLambda(int, Runnable)} using a Supplier with a generic return type.
   */
  public static <T> T incLambda(int n, Supplier<T> function) {
    __Counter.inc(n);
    return function.get();
  }

  /**
   * Synchronized version of {@link #incLambda(int, Runnable)}.
   */
  synchronized public static void incLambdaSync(int n, Runnable method) {
    __Counter.inc(n);
    method.run();
  }

  /**
   * Synchronized version of {@link #incLambda(int, Supplier)}.
   */
  synchronized public static <T> T incLambdaSync(int n, Supplier<T> function) {
    __Counter.inc(n);
    return function.get();
  }
}
