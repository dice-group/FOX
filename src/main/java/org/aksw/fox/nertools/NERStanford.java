package org.aksw.fox.nertools;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * 
 * @author rspeck
 * 
 */
public class NERStanford extends AbstractNER {

    protected String serializedClassifier = FoxCfg.get("serializedClassifier");

    protected AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

    @Override
    public Set<Entity> retrieve(String input) {

        logger.info("retrieve ...");

        List<Entity> list = new ArrayList<Entity>();

        for (String sentence : FoxTextUtil.getSentences(input)) {

            // token
            StringReader sr = new StringReader(sentence);
            CoreLabelTokenFactory clf = new CoreLabelTokenFactory();
            String options = "americanize=false,asciiQuotes=true,ptb3Escaping=false";

            sentence = "";
            PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(sr, clf, options);
            for (CoreLabel label; ptbt.hasNext();) {
                label = ptbt.next();
                sentence += label + " ";
            }
            sentence = sentence.trim();

            for (List<CoreLabel> lcl : classifier.classify(sentence)) {
                for (CoreLabel cl : lcl) {

                    String type = EntityClassMap.stanford(cl.getString(AnswerAnnotation.class));
                    String txt = cl.word().replaceAll("\\s+", " ").trim();

                    boolean testIndex = false;
                    boolean typeEquals = false;
                    if (!list.isEmpty()) {
                        testIndex = sentence.contains(list.get(list.size() - 1).getText() + " " + txt + " ");
                        typeEquals = type.equals(list.get(list.size() - 1).getType());
                    }

                    if (testIndex && typeEquals) {
                        list.get(list.size() - 1).addText(txt);
                    } else {
                        if (type != EntityClassMap.getNullCategory())
                            list.add(getEntiy(txt, type, Entity.DEFAULT_RELEVANCE, getToolName()));
                    }
                }
            }
        }

        return post(new HashSet<Entity>(list));
    }
}
