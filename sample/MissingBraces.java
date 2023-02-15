public class MissingBraces {
  public static void main(String[] args) {
    int x = 0;
    if (x == 0)return;
    if (true) if (x == 0) return;else return;

    while (false) if (false) return;

    x = 1;
    while (false) while(true)
      if(1==2)
        return;
      else
        x+=1;

    x = 0;
    do x+=1; while (x<5);

    int[] array = new int[5];
    for (int i = 0; i < 5; i++)
      array[i] = i;
    for (int val : array) System.out.println(val);

    x = 2;
    switch (x) {
      case 1: {
        i += 3;
        break;
      }
      case 2: case 3:
      case 4:
        i *= 2;
        i = i - 1;
      case 5:
        break;
      default: break;
    }
  }
}