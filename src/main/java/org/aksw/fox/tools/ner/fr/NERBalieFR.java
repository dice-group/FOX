package org.aksw.fox.tools.ner.fr;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.BalieTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import ca.uottawa.balie.Balie;

public class NERBalieFR extends BalieTool {

    public NERBalieFR() {
        super(Balie.LANGUAGE_FRENCH);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERBalieFR().retrieve(FoxConst.NER_FR_EXAMPLE_2))
            LOG.info(e);
    }
}