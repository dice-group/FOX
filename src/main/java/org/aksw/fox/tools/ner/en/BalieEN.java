package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class BalieEN extends BalieCommon {

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new BalieEN().retrieve(FoxConst.NER_EN_EXAMPLE_1));
    }
}
