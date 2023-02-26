package common;

import java.util.ArrayList;
import java.util.List;

public class Class {
  public String name;
  public boolean isMain;
  public List<Method> methods = new ArrayList<>();

  public Class(String name) {
    this.name = name;
  }

  public Class(String name, boolean isMain) {
    this.name = name;
    this.isMain = isMain;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Class method = (Class) o;
    if (isMain != method.isMain) return false;
    return name.equals(method.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (isMain ? 1 : 0);
    return result;
  }
}
