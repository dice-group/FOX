package org.aksw.fox.tools.ner.it;

import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightIT extends SpotlightCommon {

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new SpotlightIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
    }
}