package org.aksw.fox.nerlearner;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.classifier.ClassVoteClassifier;
import org.aksw.fox.nerlearner.classifier.ResultVoteClassifier;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxFileUtil;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.Vote;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SerializationHelper;

/**
 * FoxClassifier.
 * 
 * @author rspeck
 * 
 */
public class FoxClassifier {

    public static Logger logger = Logger.getLogger(FoxClassifier.class);

    protected Classifier classifier = null;
    protected Instances instances = null;
    protected FoxInstances foxInstances = null;
    private boolean isTrained = false;

    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * FoxClassifier.
     */
    public FoxClassifier() {
        logger.info("FoxClassifier ...");

        this.foxInstances = new FoxInstances();

    }

    /**
     * Builds the {@link #classifier} with {@link #instances}.
     * 
     * @throws Exception
     */
    protected void buildClassifier() throws Exception {
        if (logger.isDebugEnabled())
            logger.debug("buildClassifier ...");

        if (instances != null) {
            classifier.buildClassifier(instances);
            isTrained = true;
        } else {
            logger.error("Initialize instances first.");
        }
    }

    /**
     * Initializes {@link #instances}.
     * 
     * @param input
     * @param toolResults
     * @param oracel
     */
    protected void initInstances(Set<String> input, Map<String, Set<Entity>> toolResults, Map<String, String> oracle) {
        logger.info("initInstances ...");

        instances = (oracle == null) ? foxInstances.getInstances(input, toolResults) : foxInstances.getInstances(input, toolResults, oracle);
    }

    /**
     * Writes the MultilayerPerceptron model to file.
     * 
     * @param classifier
     * @param file
     */
    public void writeClassifier(String file) {
        logger.info("writeClassifier ...");

        FoxFileUtil.createFileStructure(file);
        try {
            SerializationHelper.write(file, classifier);
        } catch (Exception e) {
            logger.error("\n", e);
        }
    }

    /**
     * Writes the MultilayerPerceptron model to a file that is specified in the
     * fox properties.
     */
    public void writeClassifier() {
        writeClassifier(FoxCfg.get("modelPath") + System.getProperty("file.separator") + FoxCfg.get("learner").trim());
    }

    /**
     * Reads a serialized MultilayerPerceptron.
     * 
     * @param filename
     *            path to model
     */
    public void readClassifier(String filename) {
        logger.info("readClassifier ...");

        try {
            classifier = (Classifier) SerializationHelper.read(filename);
        } catch (Exception e) {
            logger.error("\n", e);
        }
    }

    /**
     * Reads a serialized Classifier from file that is specified in the fox
     * properties.
     */
    public void readClassifier() {
        readClassifier(FoxCfg.get("modelPath") + System.getProperty("file.separator") + FoxCfg.get("learner").trim());
    }

    /**
     * Rewrites results and input to labels, uses a serialized classifier to
     * classify this labels and rewrites the labels.
     * 
     * @param input
     * @param toolResults
     * @return classified token
     */
    public Set<Entity> classify(PostProcessingInterface pp) {

        // rewrite to use labels
        initInstances(pp.getLabeledInput(), pp.getLabeledToolResults(), null);

        //
        Instances classified = new Instances(instances);
        for (int i = 0; i < instances.numInstances(); i++) {
            try {
                classified.instance(i).setClassValue(classifier.classifyInstance(instances.instance(i)));
            } catch (Exception e) {
                logger.error("\n", e);
            }
        }
        return pp.instancesToEntities(classified);
    }

    /**
     * Reads files, init. instances and builds a classifier.
     * 
     * @param files
     *            files to read as training data
     */
    public void training(String input, Map<String, Set<Entity>> toolResults, Map<String, String> oracle) throws Exception {
        logger.info("training ...");

        // init. training data
        PostProcessingInterface pp = new PostProcessing(new TokenManager(input), toolResults);
        Map<String, String> labeledOracle = pp.getLabeledMap(oracle);
        Map<String, Set<Entity>> labledToolResults = pp.getLabeledToolResults();

        // DEBUG
        if (logger.isDebugEnabled()) {
            logger.debug("labeled entity:");

            Set<Entity> set = new LinkedHashSet<>();
            for (Entry<String, Set<Entity>> e : labledToolResults.entrySet())
                set.addAll(e.getValue());

            for (Entity e : set)
                logger.trace(e.getText());
        }
        // DEBUG

        initInstances(pp.getLabeledInput(), labledToolResults, labeledOracle);
        buildClassifier();
    }

    /**
     * Evaluation
     * 
     * @throws Exception
     */
    public void eva() throws Exception {

        if (isTrained) {
            Evaluation eva = new Evaluation(instances);
            eva.evaluateModel(classifier, instances);

            // print summary
            logger.info("summary\n" + eva.toSummaryString());

            // print the confusion matrix
            StringBuffer cm = new StringBuffer();
            double[][] cmMatrix = eva.confusionMatrix();
            for (String cl : EntityClassMap.entityClasses) {
                cm.append(cl + "\t");
            }
            cm.append("\n");
            for (int i = 0; i < cmMatrix.length; i++) {
                for (int ii = 0; ii < cmMatrix[i].length; ii++)
                    cm.append(cmMatrix[i][ii] + "\t\t");
                cm.append("\n");
            }

            logger.info("confusion matrix\n" + cm.toString());

            // measure
            for (String cl : EntityClassMap.entityClasses) {
                logger.info("class: " + cl);
                logger.info("fMeasure: " + eva.fMeasure(EntityClassMap.entityClasses.indexOf(cl)));
                logger.info("precision: " + eva.precision(EntityClassMap.entityClasses.indexOf(cl)));
                logger.info("recall: " + eva.recall(EntityClassMap.entityClasses.indexOf(cl)));
            }

        } else {
            logger.error("Build/training a classifier first.");
        }
    }

    /**
     * Sets ResultVote as classifier with a combination rule.
     * 
     * @param prefix
     *            tool attribute prefixes
     * @param rule
     *            combination rule
     */
    public void setClassifierResultVote(String[] prefix, SelectedTag rule) {
        setClassifierVote("class", prefix, rule);
    }

    /**
     * Sets ClassVote as classifier with a combination rule.
     * 
     * @param prefix
     *            tool attribute prefixes
     * @param rule
     *            combination rule
     */
    public void setClassifierClassVote(String[] prefix, SelectedTag rule) {
        setClassifierVote("result", prefix, rule);
    }

    private void setClassifierVote(String type, String[] prefix, SelectedTag rule) {

        isTrained = true;

        Classifier[] classifier = new Classifier[prefix.length];
        for (int i = 0; i < prefix.length; i++) {
            switch (type) {
            case "class":
                classifier[i] = new ClassVoteClassifier(prefix[i]);
                break;
            case "result":
                classifier[i] = new ResultVoteClassifier(prefix[i]);
                break;
            }
        }

        this.classifier = new Vote();
        ((Vote) this.classifier).setClassifiers(classifier);
        ((Vote) this.classifier).setCombinationRule(rule);
    }

    /**
     * Sets MultilayerPerceptron as classifier.
     */
    public void setClassifierMultilayerPerceptron() {
        Classifier classifier = new MultilayerPerceptron();
        /*
         * 'a' = (attribs + classes) / 2, 'i' = attribs, 'o' = classes , 't' =
         * attribs + classes so "a, 6", which would give you (attribs + classes)
         * / 2 nodes in the first hidden layer and 6 in the second.
         */

        // mp.setHiddenLayers(hidden);
        this.classifier = classifier;
    }
}
