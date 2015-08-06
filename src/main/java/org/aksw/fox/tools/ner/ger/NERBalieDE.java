package org.aksw.fox.tools.ner.ger;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.BalieTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import ca.uottawa.balie.Balie;

public class NERBalieDE extends BalieTool {

    public NERBalieDE() {
        super(Balie.LANGUAGE_GERMAN);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERBalieDE().retrieve(FoxConst.NER_GER_EXAMPLE_1))
            NERBalieDE.LOG.info(e);
    }
}