package tool.instrument;

import tool.model.Block;
import tool.model.Component;
import tool.model.JClass;
import tool.model.Method;

public class Logger {
  public static final String GREEN = "\u001B[32m";
  public static final String RED = "\u001B[31m";
  public static final String BRIGHT = "\u001B[97m";
  public static final String RESET = "\u001B[0m";

  Parser parser;
  public static int indent = 1;
  public boolean active;

  public Logger(Parser p) {
    parser = p;
  }

  public void log(String logMessage) {
    if (!active) return;
    System.out.printf("%s%3d:%s%-" + indent + "s%s%n", BRIGHT, parser.t.line, RESET, "", logMessage);
  }

  public void log(String formatString, Object... values) {
    log(String.format(formatString, values));
  }

  void enter(Component comp) {
    log(describe(comp, false) + GREEN + " -->" + RESET);
    indent += 2;
  }

  void leave(Component comp) {
    indent -= 2;
    log(describe(comp, true) + RED + " <--" + RESET);
  }

  private String describe(Component comp, boolean leave) {
    if (comp instanceof JClass clazz) return "class <" + clazz.getFullName() + ">";
    if (comp instanceof Method meth) return meth + "()";
    if (comp instanceof Block block)
      return String.format(
          "%s%s [%d]%s",
          block.blockType,
          block.isSingleStatement ? ", SS" : "",
          leave ? block.end.pos() : block.beg.pos(),
          block.jumpStatement != null ? " (" + block.jumpStatement.name() + ")" : ""
      );
    throw new RuntimeException("unknown component type: " + comp.getClass());
  }
}
