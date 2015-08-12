package org.aksw.fox.tools.ner.es;

import org.aksw.fox.tools.ner.common.BalieCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import ca.uottawa.balie.Balie;

public class BalieES extends BalieCommon {

    public BalieES() {
        super(Balie.LANGUAGE_SPANISH);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new BalieES().retrieve(FoxConst.NER_ES_EXAMPLE_1));
    }
}
