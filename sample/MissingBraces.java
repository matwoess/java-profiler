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
  }
}