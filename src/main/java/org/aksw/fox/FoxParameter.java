package org.aksw.fox;

public class FoxParameter {

  public enum Parameter {
    TYPE("type"), //
    INPUT("input"), //
    TASK("task"), //
    OUTPUT("output"), //
    FOXLIGHT("foxlight"), //
    NIF("nif"), //
    LANG("lang"), //
    LINKING("disamb");

    private final String para;

    Parameter(final String parameter) {
      para = parameter;
    }

    @Override
    public String toString() {
      return para;
    }

    public static Parameter fromString(final String parameter) {
      if (parameter != null) {
        for (final Parameter b : Parameter.values()) {
          if (parameter.equalsIgnoreCase(b.para)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum Type {

    TEXT("text"), //
    URL("url");

    private final String type;

    Type(final String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type;
    }

    public static Type fromString(final String type) {
      if (type != null) {
        for (final Type b : Type.values()) {
          if (type.equalsIgnoreCase(b.type)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum Task {

    NER("NER"), //
    RE("RE"), //
    KE("KE");

    private final String task;

    Task(final String task) {
      this.task = task;
    }

    @Override
    public String toString() {
      return task;
    }

    public static Task fromString(final String task) {
      if (task != null) {
        for (final Task b : Task.values()) {
          if (task.equalsIgnoreCase(b.task)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum Langs {

    DE("de"), //
    ES("es"), //
    IT("it"), //
    EN("en"), //
    NL("nl"), //
    FR("fr");

    private final String label;

    Langs(final String text) {
      label = text;
    }

    @Override
    public String toString() {
      return label;
    }

    public static Langs fromString(final String label) {
      if (label != null) {
        for (final Langs b : Langs.values()) {
          if (label.equalsIgnoreCase(b.label)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum NIF {

    OFF("false"), //
    ON("true");

    private final String label;

    NIF(final String text) {
      label = text;
    }

    @Override
    public String toString() {
      return label;
    }

    public static NIF fromString(final String label) {
      if (label != null) {
        for (final NIF b : NIF.values()) {
          if (label.equalsIgnoreCase(b.label)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum FoxLight {

    OFF("off");

    private final String label;

    FoxLight(final String text) {
      label = text;
    }

    @Override
    public String toString() {
      return label;
    }

    public static FoxLight fromString(final String label) {
      if (label != null) {
        for (final FoxLight b : FoxLight.values()) {
          if (label.equalsIgnoreCase(b.label)) {
            return b;
          }
        }
      }
      return null;
    }
  }
}
