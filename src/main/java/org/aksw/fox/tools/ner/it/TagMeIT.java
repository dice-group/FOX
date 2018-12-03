package org.aksw.fox.tools.ner.it;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.TagMeCommon;

public class TagMeIT extends TagMeCommon {

  public TagMeIT() {
    super(Locale.ITALIAN, "http://it.dbpedia.org/sparql", "http://it.dbpedia.org");
  }

}
