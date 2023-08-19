import at.jku.classes.DeepPackage;
import helper.Helper;

public class Packages {
  public static void main(String[] args) {
    Helper.globalVar++;
    if (Helper.globalVar == 1) {
       DeepPackage.printHello();
    }
    DeepPackage.printGoodbye();
  }
}
