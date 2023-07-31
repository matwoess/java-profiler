package app.model;

public enum RunMode {
  DEFAULT, INSTRUMENT_ONLY, REPORT_ONLY;

  @Override
  public String toString() {
    return switch (this) {
      case DEFAULT -> "Instrument, compile, run, report";
      case INSTRUMENT_ONLY -> "Instrument only";
      case REPORT_ONLY -> "Generate report only";
    };
  }
}