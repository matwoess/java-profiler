import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@SuppressWarnings({"unchecked"})
@Deprecated
public class Annotations {

  @SuppressWarnings({"unused", "unchecked"})
  @Deprecated
  public static void main(String[] args) {
    @SuppressWarnings("ParseError")
    Integer i1 = Integer.parseInt("000555");
    @SuppressWarnings("unused")
    List<String> filenames = new ArrayList<>(List.of("file1.java", "file2.txt"));
    filenames.sort(new Comparator<String>() {
      @Override
      @SuppressWarnings({"unused", "unchecked"})
      public int compare(String s1, String s2) {
        return s1.compareTo(s2);
      }
    });
  }

  @Override
  @SuppressWarnings({"unused", "unchecked"})
  public boolean equals(@SuppressWarnings("null") Object obj) {
    return super.equals(obj);
  }
}
