package org.aksw.fox.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.riot.Lang;

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

  public enum Linking {

    OFF("off");

    private final String para;

    Linking(final String parameter) {
      para = parameter;
    }

    @Override
    public String toString() {
      return para;
    }

    public static Linking fromString(final String parameter) {
      if (parameter != null) {
        for (final Linking b : Linking.values()) {
          if (parameter.equalsIgnoreCase(b.para)) {
            return b;
          }
        }
      }
      return null;
    }
  }

  public enum Output {

    RDFXML(Lang.RDFXML.getName()), //
    TURTLE(Lang.TURTLE.getName()), //
    RDFJSON(Lang.RDFJSON.getName()), //
    JSONLD(Lang.JSONLD.getName()), //
    TRIG(Lang.TRIG.getName()), //
    NQUADS(Lang.NQUADS.getName());
    /* FileUtils.langXMLAbbrev, */
    // Lang.N3.getName(),

    private final String type;

    Output(final String type) {
      this.type = type;

    }

    @Override
    public String toString() {
      return type;
    }

    public static Output fromString(final String type) {
      if (type != null) {
        for (final Output b : Output.values()) {
          if (type.equalsIgnoreCase(b.type)) {
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
    ALL("all"), //
    NER("ner"), //
    RE("re"), //
    KE("ke");

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

  public static Map<String, String> getDefaultParameter() {
    final Map<String, String> map = new HashMap<>();
    map.put(FoxParameter.Parameter.INPUT.toString(), "");
    map.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
    map.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
    map.put(FoxParameter.Parameter.NIF.toString(), FoxParameter.NIF.OFF.toString());
    map.put(FoxParameter.Parameter.FOXLIGHT.toString(), FoxParameter.FoxLight.OFF.toString());
    return map;
  }

  /**
   * Checks the given parameter. In case something is missing or wrong it returns an error message,
   * otherwise the return will be empty.
   *
   * @param parameter
   * @return error message or empty
   */
  public static String allowedParameterValues(final Map<String, String> parameter) {

    {// checks types
      final FoxParameter.Type value;
      value = FoxParameter.Type.fromString(parameter.get(FoxParameter.Parameter.TYPE.toString()));
      if (value == null) {
        return "Value of parameter type is wrong.";
      }
    }
    {
      // checks task
      final FoxParameter.Task value;
      value = FoxParameter.Task.fromString(parameter.get(FoxParameter.Parameter.TASK.toString()));
      if (value == null) {
        return "Value of parameter task is wrong.";
      }
    }
    {
      // checks output
      final FoxParameter.Output value;
      value =
          FoxParameter.Output.fromString(parameter.get(FoxParameter.Parameter.OUTPUT.toString()));
      if (value == null) {
        return "Value of parameter output is wrong.";
      }
    }
    {
      // TODO: check more!?
    }
    return "";
  }

  public static Set<String> allowedHeaderFields() {
    return new HashSet<>(Arrays.asList(//
        FoxParameter.Parameter.TYPE.toString(), //
        FoxParameter.Parameter.INPUT.toString(), //
        FoxParameter.Parameter.LANG.toString(), //
        FoxParameter.Parameter.LINKING.toString(), //
        FoxParameter.Parameter.FOXLIGHT.toString(), //
        FoxParameter.Parameter.TASK.toString(), //
        FoxParameter.Parameter.OUTPUT.toString()//
    ));
  }

  public static Map<String, String> defaultParameter() {
    final Map<String, String> parameter = new HashMap<>();
    parameter.put(FoxParameter.Parameter.TYPE.toString(), FoxParameter.Type.TEXT.toString());
    // parameter.put(FoxParameter.Parameter.LANG.toString(), FoxParameter.Langs.EN.toString());
    parameter.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
    parameter.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.TURTLE.getName());
    return parameter;
  }
}
