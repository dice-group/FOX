package org.aksw.fox.tools.ner.es;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.BalieTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import ca.uottawa.balie.Balie;

public class NERBalieES extends BalieTool {

    public NERBalieES() {
        super(Balie.LANGUAGE_SPANISH);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERBalieES().retrieve(FoxConst.NER_ES_EXAMPLE_1))
            NERBalieES.LOG.info(e);
    }
}
