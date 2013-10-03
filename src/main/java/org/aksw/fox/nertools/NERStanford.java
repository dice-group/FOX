package org.aksw.fox.nertools;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;
import org.apache.log4j.PropertyConfigurator;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.crf.CRFCliqueTree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

/**
 * 
 * @author rspeck
 * 
 */
public class NERStanford extends AbstractNER {

    protected CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(FoxCfg.get("serializedClassifier"));

    /*
     * Gets a list with (CoreLabel, likelihood) with max likelihood at each
     * point in a document.
     */
    protected List<Pair<CoreLabel, Double>> getProbsDocumentToList(List<CoreLabel> document) {
        List<Pair<CoreLabel, Double>> list = new ArrayList<>();

        Triple<int[][][], int[], double[][][]> triple = classifier.documentToDataAndLabels(document);
        CRFCliqueTree<String> cliqueTree = classifier.getCliqueTree(triple);
        for (int i = 0; i < cliqueTree.length(); i++) {
            CoreLabel cl = document.get(i);
            double maxprob = -1;
            for (Iterator<String> iter = classifier.classIndex.iterator(); iter.hasNext();) {
                String label = iter.next();
                double prob = cliqueTree.prob(i, classifier.classIndex.indexOf(label));
                if (prob > maxprob) {
                    maxprob = prob;
                }
            }
            list.add(new Pair<>(cl, maxprob));
        }
        return list;
    }

    @Override
    public Set<Entity> retrieve(String input) {

        logger.info("retrieve ...");
        List<Entity> list = new ArrayList<Entity>();

        for (String sentence : FoxTextUtil.getSentences(input)) {
            // DEBUG
            if (logger.isDebugEnabled())
                logger.debug("sentence: " + sentence);
            // DEBUG

            // token
            String options = "americanize=false,asciiQuotes=true,ptb3Escaping=false";
            PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(
                    new StringReader(sentence),
                    new CoreLabelTokenFactory(),
                    options);

            sentence = null;
            StringBuilder sb = new StringBuilder();
            for (CoreLabel label; tokenizer.hasNext();) {
                label = tokenizer.next();
                sb.append(label);
                if (tokenizer.hasNext())
                    sb.append(" ");
            }
            sentence = sb.toString();

            // classify
            for (List<CoreLabel> lcl : classifier.classify(sentence)) {
                // gets probs map
                List<Pair<CoreLabel, Double>> probs = getProbsDocumentToList(lcl);
                // DEBUG
                if (logger.isDebugEnabled())
                    logger.debug("getProbs: " + probs);
                // DEBUG
                for (Pair<CoreLabel, Double> prob : probs) {

                    String type = EntityClassMap.stanford(prob.first().getString(AnswerAnnotation.class));
                    // String txt = p.first().word().replaceAll("\\s+",
                    // " ").trim();
                    String currentToken = prob.first().get(CoreAnnotations.TextAnnotation.class);
                    currentToken = currentToken.replaceAll("\\s+", " ").trim();
                    // check for multiword entities
                    boolean contains = false;
                    boolean equalTypes = false;
                    Entity lastEntity = null;
                    if (!list.isEmpty()) {
                        lastEntity = list.get(list.size() - 1);
                        contains = sentence.contains(lastEntity.getText() + " " + currentToken + " ");
                        equalTypes = type.equals(lastEntity.getType());
                    }
                    if (contains && equalTypes) {
                        lastEntity.addText(currentToken);
                        // TODO:
                        // UPDATE relevance!
                    } else {
                        if (type != EntityClassMap.getNullCategory()) {
                            float p = Entity.DEFAULT_RELEVANCE;
                            if (FoxCfg.get("stanfordDefaultRelevance") != null && !Boolean.valueOf(FoxCfg.get("stanfordDefaultRelevance")))
                                p = prob.second().floatValue();
                            list.add(getEntity(currentToken, type, p, getToolName()));
                        }
                    }
                }
            }
        }
        return new HashSet<Entity>(list);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure("log4j.properties");
        for (Entity e : new NERStanford().retrieve("Stanford University is located in California. It is a great university."))
            NERStanford.logger.info(e);

        // Properties props = new Properties();
        // props.setProperty("annotators",
        // "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
        // );
        // StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //
        // Annotation ann = new Annotation(
        // "Stanford University is located in California. It is a great university."
        // );
        // pipeline.annotate(ann);
        // for (CoreMap sentence : ann.get(SentencesAnnotation.class)) {
        // Tree tree = sentence.get(TreeAnnotation.class);
        // System.out.println(tree);
        // System.out.println(tree.score());
        // }
    }
}
