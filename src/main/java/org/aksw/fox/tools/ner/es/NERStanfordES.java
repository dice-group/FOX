package org.aksw.fox.tools.ner.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author rspeck
 * 
 */
public class NERStanfordES extends AbstractNER {
    // https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/pipeline/StanfordCoreNLP-spanish.properties
    Properties      props    = new Properties();
    StanfordCoreNLP pipeline = null;

    public NERStanfordES() {
        /*
         props.setProperty("annotators","tokenize, ssplit, pos, lemma, ner, parse");
         */
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("tokenize.language", "es");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
        props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        NERStanfordES n = new NERStanfordES();
        List<Entity> x = n.retrieve(FoxConst.NER_ES_EXAMPLE_1);
        System.out.println(x.size());
    }

    @Override
    public List<Entity> retrieve(String text) {
        LOG.info("retrieve ...");
        Annotation ann = new Annotation(text);
        pipeline.annotate(ann);

        List<Entity> list = new ArrayList<>();

        for (CoreMap sentence : ann.get(SentencesAnnotation.class)) {
            String tokensentence = "";
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                tokensentence += token.word() + " ";
                String type = EntityClassMap.stanfordes(token.get(NamedEntityTagAnnotation.class));
                String currentToken = token.originalText();
                // check for multiword entities
                boolean contains = false;
                boolean equalTypes = false;
                Entity lastEntity = null;
                if (!list.isEmpty()) {
                    lastEntity = list.get(list.size() - 1);
                    contains = tokensentence.contains(lastEntity.getText() + " " + currentToken + " ");
                    equalTypes = type.equals(lastEntity.getType());
                }
                if (contains && equalTypes) {
                    lastEntity.addText(currentToken);
                } else {
                    if (type != EntityClassMap.getNullCategory()) {
                        float p = Entity.DEFAULT_RELEVANCE;
                        list.add(getEntity(currentToken, type, p, getToolName()));
                    }
                }
            }
        }
        // TRACE
        if (LOG.isTraceEnabled()) {
            LOG.trace(list);
        } // TRACE
        LOG.info("retrieve done.");
        return list;
    }
}