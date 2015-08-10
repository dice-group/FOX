package org.aksw.fox.tools.ner.nl;

import org.aksw.fox.tools.ner.common.OpenNLPCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class OpenNLPNL extends OpenNLPCommon {

    static final String[] modelPath =
                                    {
                                    "data/openNLP/nl-ner-person.bin",
                                    "data/openNLP/nl-ner-location.bin",
                                    "data/openNLP/nl-ner-organization.bin"
                                    };

    public OpenNLPNL() {
        super(modelPath);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new OpenNLPNL().retrieve(FoxConst.NER_NL_EXAMPLE_2));
    }
}
