package org.aksw.fox.tools.ner.en;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.OpenNLPTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NEROpenNLP extends OpenNLPTool {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/en-ner-person.bin",
                                    "data/openNLP/en-ner-location.bin",
                                    "data/openNLP/en-ner-organization.bin"
                                    };

    public NEROpenNLP() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NEROpenNLP().retrieve(FoxConst.NER_EN_EXAMPLE_1))
            LOG.info(e);
    }
}
