import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"unchecked"})
@Deprecated
public class Annotations {

  @SuppressWarnings({"unused", "unchecked"})
  @Deprecated
  public static void main(String[] args) {
    @SuppressWarnings("ParseError")
    Integer i1 = Integer.parseInt("asdf");
    @SuppressWarnings("unused")
    List<String> filenames = new ArrayList<>(List.of("file1.java", "file2.txt"));
  }

  @Override
  @SuppressWarnings({"unused", "unchecked"})
  public boolean equals(@SuppressWarnings("null") Object obj) {
    return super.equals(obj);
  }
}
