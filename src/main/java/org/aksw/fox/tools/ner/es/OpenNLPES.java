package org.aksw.fox.tools.ner.es;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class OpenNLPES extends OpenNLPCommon {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/es-ner-person.bin",
                                    "data/openNLP/es-ner-location.bin",
                                    "data/openNLP/es-ner-organization.bin" };

    public OpenNLPES() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new OpenNLPES().retrieve(FoxConst.NER_ES_EXAMPLE_1));
    }
}
