package org.aksw.fox.tools.ner.nl;

import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxConst;

public class SpotlightNL extends SpotlightCommon {
  public SpotlightNL() {
    super(new Locale("nl"));
  }

  public static void main(final String[] a) {
    LOG.info(new SpotlightNL().retrieve(FoxConst.NER_NL_EXAMPLE_2));
  }
}
