import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ParallelSum {
  static int[] numbersArray;

  public static void main(String[] args) {
    Scanner userInput = new Scanner(System.in);
    int nNumbers, nThreads;
    if (args.length > 0) {
      nNumbers = Integer.parseInt(args[0]);
    } else {
      System.out.println("How many sequential numbers should be added?: ");
      nNumbers = Integer.parseInt(userInput.next());
    }
    if (args.length > 1) {
      nThreads = Integer.parseInt(args[1]);
    } else {
      System.out.println("How many threads should be used?: ");
      nThreads = Integer.parseInt(userInput.next());
    }
    numbersArray = IntStream.rangeClosed(1, nNumbers).toArray();
    ExecutorService pool = Executors.newFixedThreadPool(nThreads);
    Vector<Future<Long>> result = new Vector<>(nThreads);
    int partition = nNumbers / nThreads;
    if (partition * nThreads < nNumbers) {
      partition++;
    }
    for (int i = 0; i < nThreads; i++) {
      int fromIdx = i * partition;
      int toIdx = Math.min(i * partition + partition, numbersArray.length);
      result.add(pool.submit(new ParallelSum.Accumulator(fromIdx, toIdx)));
    }
    long t1 = System.currentTimeMillis();
    long cumSum = 0;
    try {
      for (int i = 0; i < nThreads; i++)
        cumSum += result.get(i).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    long t2 = System.currentTimeMillis();
    pool.shutdown();
    System.out.println("The total sum of the array is: " + cumSum);
    System.out.println();
    System.out.printf("Algorithm took %.3fs (%dms)\n", (t2 - t1) / 1_000.0, (t2 - t1));
  }

  private static class Accumulator implements Callable<Long> {
    static AtomicInteger threadNumberCounter = new AtomicInteger(1);
    int from, to, threadNumber;

    public Accumulator(int fromIdx, int toIdx) {
      from = fromIdx;
      to = toIdx;
      threadNumber = threadNumberCounter.getAndIncrement();
      System.out.printf("T%d: starting accumulation of indices [%d, %d)\n", threadNumber, from, to);
    }

    @Override
    public Long call() {
      long partialSum = 0;
      for (int i = from; i < to; i++) {
        partialSum = accumulate(partialSum, getNumberAtIndex(i));
      }
      return partialSum;
    }

    private long accumulate(long currentValue, long addValue) {
      return currentValue + addValue;
    }

    private int getNumberAtIndex(int idx) {
      return getArray()[idx];
    }

    private int[] getArray() {
      return numbersArray;
    }
  }
}
