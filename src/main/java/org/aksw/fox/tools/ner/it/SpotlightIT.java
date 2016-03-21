package org.aksw.fox.tools.ner.it;

import java.io.IOException;
import java.util.Locale;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightIT extends SpotlightCommon {
  public SpotlightIT() {
    super(Locale.ITALIAN);
  }

  public static void main(final String[] a) throws IOException {
    PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    LOG.info(new SpotlightIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
  }
}
