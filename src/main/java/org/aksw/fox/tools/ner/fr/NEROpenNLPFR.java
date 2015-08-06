package org.aksw.fox.tools.ner.fr;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.OpenNLPTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NEROpenNLPFR extends OpenNLPTool {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/fr-ner-location.bin",
                                    "data/openNLP/fr-ner-organization.bin",
                                    "data/openNLP/fr-ner-person.bin"
                                    };

    public NEROpenNLPFR() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NEROpenNLPFR().retrieve(FoxConst.NER_FR_EXAMPLE_1))
            LOG.info(e);
    }
}
