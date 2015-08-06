package org.aksw.fox.tools.ner.en;

import java.util.Properties;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.StanfordTool;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * @author rspeck
 * 
 */
public class NERStanfordEn extends StanfordTool {

    private static Properties props = new Properties();
    static {
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("tokenize.language", "en");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");
    }

    public NERStanfordEn() {
        super(props);

        entityClasses.put("ORGANIZATION", EntityClassMap.O);
        entityClasses.put("LOCATION", EntityClassMap.L);
        entityClasses.put("PERSON", EntityClassMap.P);
        entityClasses.put("PEOPLE", EntityClassMap.P);
        entityClasses.put("O", EntityClassMap.N);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERStanfordEn().retrieve(FoxConst.NER_EN_EXAMPLE_1))
            LOG.info(e);
    }
}
