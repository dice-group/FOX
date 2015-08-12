package org.aksw.fox.tools.ner.it;

import org.aksw.fox.tools.ner.common.TagMeCommon;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class TagMeIT extends TagMeCommon {

    public TagMeIT() {
        super("it");
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new TagMeIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
    }
}