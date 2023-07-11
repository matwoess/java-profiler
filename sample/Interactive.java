import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interactive {
  public static final Pattern namePattern = Pattern.compile("[A-Z,a-z]*");
  
  public static void main(String[] args) {
    Scanner userInput = new Scanner(System.in);
    System.out.println("Please enter your firstname: ");
    String firstname = userInput.next();
    Matcher nameMatcher = namePattern.matcher(firstname);
    if (!nameMatcher.matches()) {
      System.out.println("invalid characters in firstname: " + firstname);
      return;
    }
    System.out.println("Please enter your lastname: ");

    String lastname = userInput.next();
    nameMatcher = namePattern.matcher(lastname);
    if (!nameMatcher.matches()) {
      System.out.println("invalid characters in lastname: " + lastname);
      return;
    }
    if (firstname.isBlank() || lastname.isBlank()) {
      System.out.println("The firstname or lastname is blank.");
    } else {
      System.out.printf("Hello, %s %s!\n", firstname, lastname);
    }
  }
}
