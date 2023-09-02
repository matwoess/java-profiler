class Strings {
  static String emptyString = "";
  static String startEndWithBSlash = "\\asdf\tqwerty\\";
  static String emptyTextBlock = """
      """;
  static String withNestedBlock = """
      Text with
      multiple (!)
      lines!\n
      can contain "strings", \"escaped strings\"
      and \t""\"
      Text blocks!
      with ""\\\\"
        "nested(!) text blocks",
        ""\\\\"
      \"""
      """;

  public static String getMultiLineString() {
    return withNestedBlock;
  }

  public static void main(String[] args) {
    char ch1 = ' ', ch2 = 'x', ch3 = '\t';
    char ch4 = '\n', ch5 = '\\', ch6 = '\032';
    char ch7 = '\u1258', ch8 = '\uAFEc';
    var s1 = ch1 + ch2 + ch3 + ch6 + "\u2135" + "||\\//$123098ß43=)§%=)(\"";
    // with trailing double-slash
    int y = 1;
    if (y > 0) {
      String s2 = String.format("y has the value: %s\\", y);
      System.out.println(s2 + s1 + getMultiLineString());
      System.out.println(printStrings());
      TextBlocksWithQuotesBeforeInlineEnd.main(new String[]{});
      System.out.println(TextBlocksWithQuotesBeforeInlineEnd.getTextBlock());
    } else {
      System.out.println("""
              <body>
              <span>  static String startEndWithBSlash = "\\\\asdf\\tqwerty\\\\";</span>
              <span>  static String emptyTextBlock = ""\"</span>
              <span>  ""\";</span>
              <span>  static String withNestedBlock = ""\"</span>
              <span>      Text with</span>
              <span>      multiple (!)</span>
              <span>      lines!\\n</span>
              <span>      can contain "strings", \\"escaped strings\\"</span>
              <span>      and \\t""\\"</span>
              <span>      Text blocks!</span>
              <span>      with ""\\\\\\\\"</span>
              <span>        "nested(!) text blocks",</span>
              <span>        ""\\\\\\\\"</span>
              <span>      \\""\"</span>
              <span>      ""\";</span>
              <span></span>
              </body>
          """);
    }
  }

  static String printStrings() {
    {
      System.out.print("""
          """);
    }
    {
      System.out.println("""
          x""");
    }
    {
      System.out.println("""
          \"""");
    }
    {
      System.out.println("""
          "str1" "str2"
          """);
    }
    {
      System.out.println("""
          ""str1"" \"\"str2\"\"
          """);
    }
    {
      System.out.println("""
          ""str"" ßß$"!)(@æſĸðf\t\n\
          "\"""");
    }
    return """
        \"""
        nested "str"
        ""\"
        """;
  }

  class TextBlocksWithQuotesBeforeInlineEnd {
    static String tBlock = """
        \"\"\"
        "TextBlock",
        \"""\"
        ""\"""";

    public static void main(String[] args) {
      tBlock += """
          \""\"""";
      if (args.length == 0) {
        tBlock += """
            \"""
            TextBlock",
            ""
            \""\"
            "\""
            \"\"\"
            ""\"""";
      }
    }

    public static String getTextBlock() {
      return tBlock;
    }
  }
}