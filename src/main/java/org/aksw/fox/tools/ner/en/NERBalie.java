package org.aksw.fox.tools.ner.en;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.BalieTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NERBalie extends BalieTool {

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERBalie().retrieve(FoxConst.NER_EN_EXAMPLE_1))
            NERBalie.LOG.info(e);
    }
}
