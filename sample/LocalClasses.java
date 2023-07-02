import java.util.Comparator;

public class LocalClasses {
  public static void main(String[] args) {
    enum StatusCode {
      OK, UNAUTHORIZED, FORBIDDEN, NOTFOUND
    }
    StatusCode statusCode = StatusCode.FORBIDDEN;
    int sc = switch (statusCode) {
      case OK -> 200;
      case UNAUTHORIZED -> 401;
      case FORBIDDEN -> 403;
      case NOTFOUND -> {
        yield 404;
      }
      default -> throw new RuntimeException("invalid status code");
    };
    if (sc == 403) {
      class ResponseBuilder {
       final Comparator<String> lengthComparator = new Comparator<>() {
         @Override
         public int compare(String s1, String s2) {
           return s1.length() - s2.length();
         }
       };
        interface IBuilder {
          default void build() {
            System.out.println("building");
          }
        }
        void make() {
          class Builder implements IBuilder {
            void create() {
              this.build();
            }
          }
          Builder b = new Builder();
          b.create();
        }
      }
      ResponseBuilder rb = new ResponseBuilder();
      rb.make();;
    }
  }
}
