package org.aksw.fox.tools.ner.nl;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.OpenNLPTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NEROpenNLP_NL extends OpenNLPTool {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/nl-ner-person.bin",
                                    "data/openNLP/nl-ner-location.bin",
                                    "data/openNLP/nl-ner-organization.bin"
                                    };

    public NEROpenNLP_NL() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NEROpenNLP_NL().retrieve(FoxConst.NER_NL_EXAMPLE_2))
            LOG.info(e);
    }
}
