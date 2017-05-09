package org.aksw.fox.tools.ner.es;

import java.io.IOException;
import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxConst;

public class SpotlightES extends SpotlightCommon {

  public SpotlightES() {
    super(new Locale("es", "ES"));
  }

  public static void main(final String[] a) throws IOException {

    new SpotlightES().retrieve(//
        FoxConst.NER_ES_EXAMPLE_1).forEach(LOG::info);

  }
}
