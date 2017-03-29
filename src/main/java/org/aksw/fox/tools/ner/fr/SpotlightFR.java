package org.aksw.fox.tools.ner.fr;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxConst;

public class SpotlightFR extends SpotlightCommon {

  public SpotlightFR() {
    super(Locale.FRENCH);
  }

  public static void main(final String[] a) {
    LOG.info(new SpotlightFR().retrieve(FoxConst.NER_FR_EXAMPLE_1));
  }
}
