package org.aksw.fox.tools.ner.de;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxConst;

public class SpotlightDE extends SpotlightCommon {
  public SpotlightDE() {
    super(Locale.GERMAN);
  }

  public static void main(final String[] a) {
    LOG.info(new SpotlightDE().retrieve(FoxConst.NER_GER_EXAMPLE_1));
  }
}
