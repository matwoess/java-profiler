import java.util.Arrays;

class Algorithms {
  public static void main(String[] args) {
    { // Fibonacci
      int N = Integer.parseInt(args[0]);
      for (int i = 1; i < N; i++) {
        System.out.print(fib(i) + " ");
      }
      System.out.println();
    }
    { // Bubble sort
      int[] array = {63, 24, 56, 12, 22, 17, 90};
      bubbleSort(array);
      System.out.println(Arrays.toString(array));
    }
    { // Prime number
      System.out.println(isPrime(7));
      System.out.println(isPrime(11));
      System.out.println(isPrime(155574));
      System.out.println(isPrime(79));
    }
  }

  static int fib(int n) {
    if (n <= 1)
      return n;
    return fib(n - 1) + fib(n - 2);
  }

  static void bubbleSort(int[] array) {
    int len = array.length;
    int tmp = 0;
    for (int i = 0; i < len; i++) {
      for (int j = 1; j < (len - i); j++) {
        if (array[j] < array[j - 1]) {
          tmp = array[j - 1];
          array[j - 1] = array[j];
          array[j] = tmp;
        }
      }
    }
    System.out.println("Array sorted.");
  }

  static boolean isPrime(int number) {
    if (number <= 1) {
      return false;
    }
    int sqrt = (int) Math.sqrt(number);
    for (int i = 2; i <= sqrt; i++) {
      if (number % i == 0) {
        System.out.println("Number is not prime.");
        return false;
      }
    }
    System.out.println("Number is prime.");
    return true;
  }
}