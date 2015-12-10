package org.aksw.fox.nerlearner;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * FoxClassifier.
 * 
 * @author rspeck
 * 
 */
public class FoxClassifier {

    public static Logger       LOG                      = LogManager.getLogger(FoxClassifier.class);

    public static final String CFG_KEY_MODEL_PATH       = FoxClassifier.class.getName().concat(".modelPath");
    public static final String CFG_KEY_LEARNER          = FoxClassifier.class.getName().concat(".learner");
    public static final String CFG_KEY_LEARNER_OPTIONS  = FoxClassifier.class.getName().concat(".learnerOptions");
    public static final String CFG_KEY_LEARNER_TRAINING = FoxClassifier.class.getName().concat(".training");

    protected Classifier       classifier               = null;
    protected Instances        instances                = null;
    protected FoxInstances     foxInstances             = null;
    private boolean            isTrained                = false;

    /**
     * FoxClassifier.
     */
    public FoxClassifier() {
        LOG.info(FoxClassifier.class + " ...");
        foxInstances = new FoxInstances();
    }

    /**
     * Builds the {@link #classifier} with {@link #instances}.
     * 
     * @throws Exception
     */
    protected void buildClassifier() throws Exception {
        if (LOG.isDebugEnabled())
            LOG.debug("buildClassifier ...");

        if (instances != null) {
            classifier.buildClassifier(instances);
            isTrained = true;
        } else {
            LOG.error("Initialize instances first.");
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
        LOG.info("init. instances ...");
        instances = (oracle == null) ? foxInstances.getInstances(input, toolResults) : foxInstances.getInstances(input, toolResults, oracle);
    }

    protected String getName(String lang) {
        return FoxCfg.get(FoxClassifier.CFG_KEY_MODEL_PATH) + File.separator + lang + File.separator + FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER);
    }

    /**
     * 
     * @param classifier
     * @param file
     */
    public void writeClassifier(String file, String lang) {
        String name = getName(lang);
        LOG.info("writeClassifier: " + name);
        String path = FilenameUtils.getPath(name);
        try {
            FileUtils.forceMkdir(new File(path));
            SerializationHelper.write(name, classifier);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads a serialized Classifier from file that is specified in the fox
     * properties.
     */
    public void readClassifier(String lang) {
        String name = getName(lang);
        LOG.info("readClassifier: " + name);
        try {
            classifier = (Classifier) SerializationHelper.read(name.trim());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        LOG.info("readClassifier done.");
    }

    /**
     * Rewrites results and input to labels, uses a serialized classifier to
     * classify this labels and rewrites the labels.
     * 
     * @param sentence
     * @param toolResults
     * @return classified token
     */
    public Set<Entity> classify(IPostProcessing pp) {
        LOG.info("classify ...");

        // rewrite to use labels
        initInstances(pp.getLabeledInput(), pp.getLabeledToolResults(), null);

        Instances classified = new Instances(instances);
        for (int i = 0; i < instances.numInstances(); i++) {
            try {
                classified.instance(i).setClassValue(classifier.classifyInstance(instances.instance(i)));
            } catch (Exception e) {
                LOG.error("\n", e);
            }
        }
        // TRACE
        if (LOG.isTraceEnabled())
            LOG.trace("classified: \n" + classified);
        // TRACE

        Set<Entity> set = pp.instancesToEntities(classified);
        LOG.info("classify done, size: " + set.size());
        if (LOG.isDebugEnabled())
            LOG.debug(classifier);
        return set;
    }

    /**
     * Reads files, init. instances and builds a classifier.
     * 
     * @param files
     *            files to read as training data
     */
    public void training(String input, Map<String, Set<Entity>> toolResults, Map<String, String> oracle) throws Exception {
        LOG.info("training ...");

        // init. training data
        IPostProcessing pp = new PostProcessing(new TokenManager(input), toolResults);
        Map<String, String> labeledOracle = pp.getLabeledMap(oracle);
        Map<String, Set<Entity>> labledToolResults = pp.getLabeledToolResults();

        // DEBUG
        if (LOG.isDebugEnabled()) {
            LOG.debug("labeled entity:");

            Set<Entity> set = new LinkedHashSet<>();
            for (Entry<String, Set<Entity>> e : labledToolResults.entrySet())
                set.addAll(e.getValue());

            for (Entity e : set)
                LOG.trace(e.getText());
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
            LOG.info("summary\n" + eva.toSummaryString());

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

            LOG.info("confusion matrix\n" + cm.toString());

            // measure
            for (String cl : EntityClassMap.entityClasses) {
                LOG.info("class: " + cl);
                LOG.info("fMeasure: " + eva.fMeasure(EntityClassMap.entityClasses.indexOf(cl)));
                LOG.info("precision: " + eva.precision(EntityClassMap.entityClasses.indexOf(cl)));
                LOG.info("recall: " + eva.recall(EntityClassMap.entityClasses.indexOf(cl)));
            }

        } else {
            LOG.error("Build/training a classifier first.");
        }
    }

    public void setIsTrained(boolean bool) {
        this.isTrained = bool;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public Classifier getClassifier() {
        return classifier;
    }
}
