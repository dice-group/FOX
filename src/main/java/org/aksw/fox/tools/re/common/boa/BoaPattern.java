package org.aksw.fox.tools.re.common.boa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Only used inside this class to encapsulate the Solr query results.
 */
public class BoaPattern {

  public Map<String, Double> features = new HashMap<String, Double>();
  public String naturalLanguageRepresentationNormalized = "";
  public String naturalLanguageRepresentationWithoutVariables = "";
  public String naturalLanguageRepresentation = "";
  public String language = "";
  public Double boaScore = 0D;
  public Double naturalLanguageScore = 0D;
  public String posTags = "";
  private String normalizedPattern = null;
  public String generalized = "";

  public BoaPattern(final String naturalLanguageRepresentation, final String language) {

    this.naturalLanguageRepresentation = naturalLanguageRepresentation;
    this.language = language;
  }

  public BoaPattern() {
    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final StringBuilder builder = new StringBuilder();
    builder.append("Pattern [factFeatures=");
    builder.append(features);
    builder.append(", naturalLanguageRepresentation=");
    builder.append(naturalLanguageRepresentation);
    builder.append(", boaScore=");
    builder.append(boaScore);
    builder.append(", naturalLanguageScore=");
    builder.append(naturalLanguageScore);
    builder.append(", POS=");
    builder.append(posTags);
    builder.append("]");
    return builder.toString();
  }

  /**
   *
   * @return
   */
  public String normalize() {

    if (normalizedPattern == null) {

      if (naturalLanguageRepresentationNormalized.isEmpty()) {

        naturalLanguageRepresentationNormalized =
            naturalLanguageRepresentationWithoutVariables.replaceAll(",", "").replace("`", "")
                .replace(" 's", "'s").replaceAll("  ", " ").replaceAll("'[^s]", "")
                .replaceAll("-LRB-", "").replaceAll("-RRB-", "").replaceAll("[0-9]{4}", "").trim();
        // ensure that we match the pattern and nothing more

        if (naturalLanguageRepresentationNormalized.equals("'s")) {
          naturalLanguageRepresentationNormalized = naturalLanguageRepresentationNormalized + " ";
        } else {
          naturalLanguageRepresentationNormalized =
              " " + naturalLanguageRepresentationNormalized + " ";
        }

      }

      final Set<String> naturalLanguageRepresentationChunks = new HashSet<String>(
          // Arrays.asList(naturalLanguageRepresentationNormalized.toLowerCase().trim().split("
          // ")));
          Arrays.asList(naturalLanguageRepresentationNormalized.trim().split(" ")));

      // TODO: !!! REMOVE STOPWORDS
      // naturalLanguageRepresentationChunks.removeAll(Constants.STOP_WORDS);

      normalizedPattern = " " + StringUtils.join(naturalLanguageRepresentationChunks, " ") + " ";
    }

    return normalizedPattern;
  }

  public String getNormalized() {

    String s = naturalLanguageRepresentationNormalized;

    if (s.isEmpty()) {

      s = naturalLanguageRepresentationWithoutVariables.replaceAll(",", "").replace("`", "")
          .replace(" 's", "'s").replaceAll("  ", " ").// replaceAll("'[^s]", "").
          replaceAll("-LRB-", "").replaceAll("-RRB-", "").replaceAll("[0-9]{4}", "").trim();
      // ensure that we match the pattern and nothing more

      if (s.equals("'s")) {
        s = s + " ";
      } else {
        s = " " + s + " ";
      }
    }

    final List<String> naturalLanguageRepresentationChunks =
        // new ArrayList<String>(Arrays.asList(s.toLowerCase().trim().split(" ")));
        new ArrayList<String>(Arrays.asList(s.trim().split(" ")));

    // TODO: !!! REMOVE STOPWORDS
    // naturalLanguageRepresentationChunks.removeAll(Constants.NEW_STOP_WORDS);

    return " "
        + StringUtils.join(naturalLanguageRepresentationChunks, " ").trim().replaceAll(" +", " ")
        + " ";
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((language == null) ? 0 : language.hashCode());
    result = (prime * result)
        + ((naturalLanguageRepresentation == null) ? 0 : naturalLanguageRepresentation.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BoaPattern other = (BoaPattern) obj;
    if (language == null) {
      if (other.language != null) {
        return false;
      }
    } else if (!language.equals(other.language)) {
      return false;
    }
    if (naturalLanguageRepresentation == null) {
      if (other.naturalLanguageRepresentation != null) {
        return false;
      }
    } else if (!naturalLanguageRepresentation.equals(other.naturalLanguageRepresentation)) {
      return false;
    }
    return true;
  }

  /**
   * @return true if the pattern starts with ?D?
   */
  public boolean isDomainFirst() {

    return naturalLanguageRepresentation.startsWith("?D?") ? true : false;
  }
}
