package org.aksw.fox.tools.ner.es;

import java.util.Properties;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.StanfordTool;
import org.aksw.fox.tools.ner.ger.NERBalieDE;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 * @author rspeck
 * 
 */
public class NERStanfordES extends StanfordTool {
    // https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/pipeline/StanfordCoreNLP-spanish.properties
    private static Properties props = new Properties();
    static {
        /*
        props.setProperty("annotators","tokenize, ssplit, pos, lemma, ner, parse");
        */
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("tokenize.language", "es");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
        props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");
        /*props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");*/

    }

    public NERStanfordES() {
        super(props);

        entityClasses.put("ORG", EntityClassMap.O);
        entityClasses.put("LUG", EntityClassMap.L);
        entityClasses.put("PERS", EntityClassMap.P);
        entityClasses.put("O", EntityClassMap.N);
        entityClasses.put("OTROS", EntityClassMap.N);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERStanfordES().retrieve(FoxConst.NER_ES_EXAMPLE_1))
            NERBalieDE.LOG.info(e);
    }

}
