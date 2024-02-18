import java.io.IOException;

public class AllSamples {
  public static void main(String[] args) throws IOException {
    Simple.main(args);
    BasicElements.main(args);
    Classes.main(args);
    Enums.main(args);
    Interfaces.main(args);
    MissingBraces.main(args);
    Packages.main(args);
    Annotations.main(args);
    Switches.main(args);
    Lambdas.main(args);
    AnonymousClasses.main(args);
    NestedBlockTypes.main(args);
    Algorithms.main(new String[]{"10"});
    LocalClasses.main(args);
    Records.main(args);
    Strings.main(args);
    JumpStatements.main(args);
    ParallelSum.main(new String[]{String.valueOf(100), "1"});
  }
}
