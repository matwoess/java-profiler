import java.lang.annotation.*;
import java.util.*;


@Target(ElementType.TYPE_USE)
@interface VersionID {
  int id();
}

@Target(ElementType.TYPE_USE)
@interface Nullable {
}

@SuppressWarnings({"unchecked"})
@VersionID(id = 5)
public class Annotations {
  @Deprecated
  int var = 0;

  private static class RuntimeRetentionPolicy {
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface AuthorMetadata {
      String author();
      String date();
      int revision() default 1;
      String[] comments() default {"no comment"};
    }
  }

  @SuppressWarnings({"unused", "unchecked"})
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
    printData("data");
    printData(new @VersionID(id = 5) String[] { nullValue }[0]);
  }

  private static final String nullValue = (String) getFirst(new @Nullable @VersionID(id = 1) Object[] { null });

  private static Object getFirst(@Nullable Object[] objects) {
    return objects[0];
  }

  @Override
  @SuppressWarnings({"unused", "unchecked"})
  @RuntimeRetentionPolicy.AuthorMetadata(
      author = "Myself",
      date = "01.03.2020",
      comments = {"Important!", "Is documented\n"}
  )
  @Deprecated
  public boolean equals(@SuppressWarnings("null") Object obj) {
    return super.equals(obj);
  }

  @RuntimeRetentionPolicy.AuthorMetadata(
      author = """
          Me,
          Others,
          Annonymous
          """,
      date = "01.03.2020",
      comments = {"Additional Markdown data below:", """
      | Name  | Age |
      |-------|-----|
      | Mark  | 21  |
      | John  | 45  |
      | Maria | 19  |
      """}
  )
  static void printData(String data) {
    System.out.println(data);
  }

  enum Status {
    ACTIVE,
    @Deprecated // Deprecated annotation
    @Native
    @SuppressWarnings({"unused"})
    HYBRID,
    @Native
    INACTIVE
  }
}
