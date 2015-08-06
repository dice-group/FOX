package org.aksw.fox.tools.ner.es;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.OpenNLPTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NEROpenNLPES extends OpenNLPTool {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/es-ner-person.bin",
                                    "data/openNLP/es-ner-location.bin",
                                    "data/openNLP/es-ner-organization.bin" };

    public NEROpenNLPES() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NEROpenNLPES().retrieve(FoxConst.NER_ES_EXAMPLE_1))
            NEROpenNLPES.LOG.info(e);
    }
}
