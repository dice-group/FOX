package org.aksw.fox.nerlearner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.decode.BILOUDecoding;
import org.aksw.fox.data.decode.GreedyLeftToRight;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.nerlearner.reader.EntitiesToInstances;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
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

  public static Logger LOG = LogManager.getLogger(FoxClassifier.class);

  private static final String name = FoxClassifier.class.getName();
  public static final String CFG_KEY_MODEL_PATH = name.concat(".modelPath");
  public static final String CFG_KEY_LEARNER = name.concat(".learner");
  public static final String CFG_KEY_LEARNER_OPTIONS = name.concat(".learnerOptions");
  public static final String CFG_KEY_LEARNER_TRAINING = name.concat(".training");

  protected Classifier classifier = null;
  protected Instances instances = null;

  protected EntitiesToInstances entitiesToInstances = new EntitiesToInstances();
  private boolean isTrained = false;

  /**
   * Holds loaded classifier for a specific language.
   */
  private final Map<String, Classifier> cache = new HashMap<>();

  /**
   * FoxClassifier.
   */
  public FoxClassifier() {
    LOG.info(FoxClassifier.class + " ...");
  }

  /**
   * Builds the {@link #classifier} with {@link #instances}.
   *
   * @throws Exception
   */
  protected void buildClassifier() throws Exception {
    if (instances != null) {
      classifier.buildClassifier(instances);
      isTrained = true;
    } else {
      throw new NullPointerException("Initialize instances first.");
    }
  }

  /**
   * Initializes {@link #instances}.
   *
   * @param input
   * @param toolResults
   * @param oracel
   */
  protected void initInstances(//
      final Set<String> input, final Map<String, Set<Entity>> toolResults,
      final Map<String, String> oracle) {

    LOG.info("Initializes instances ...");

    instances = //
        oracle == null ? //
            entitiesToInstances.getInstances(input, toolResults, null) : //
            entitiesToInstances.getInstances(input, toolResults, oracle);
  }

  /**
   * Gets the path to the serialized classifier.
   *
   * @param lang
   * @return path to the serialized classifier
   */
  protected String getName(final String lang) {
    return PropertiesLoader.get(FoxClassifier.CFG_KEY_MODEL_PATH)//
        .concat(File.separator).concat(lang)//
        .concat(File.separator).concat(PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER));
  }

  /**
   * Serializes the classifier.
   *
   * @param file
   * @param lang
   */
  public void writeClassifier(final String file, final String lang) {

    final String name = getName(lang);
    final String path = FilenameUtils.getPath(name);
    try {
      FileUtils.forceMkdir(new File(path));
      SerializationHelper.write(name, classifier);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Reads a serialized Classifier from file that is specified in the fox properties.
   */
  public void readClassifier(final String lang) {
    classifier = cache.get(lang);
    if (classifier == null) {
      final String name = getName(lang);
      LOG.info("readClassifier: " + name);
      try {
        classifier = (Classifier) SerializationHelper.read(name.trim());
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      LOG.info("readClassifier done.");
      cache.put(lang, classifier);
    }
  }

  /**
   * Rewrites results and input to labels, uses a serialized classifier to classify this labels and
   * rewrites the labels.
   *
   * @param sentence
   * @param toolResults
   * @return classified token
   */
  public Set<Entity> classify(final String input, final Map<String, Set<Entity>> toolResults) {
    LOG.info("classify ...");

    // post
    final TokenManager tm = new TokenManager(input);
    final IPostProcessing pp = new PostProcessing(tm, toolResults);
    // cleaned tool results
    pp.getToolResults();

    // rewrite to use labels
    initInstances(pp.getLabeledInput(), pp.getLabeledToolResults(), null);

    final Instances classifiedInstances = new Instances(instances);
    for (int i = 0; i < instances.numInstances(); i++) {
      try {
        classifiedInstances.instance(i)
            .setClassValue(classifier.classifyInstance(instances.instance(i)));
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }

    final GreedyLeftToRight gftr = new GreedyLeftToRight();
    final BILOUDecoding bilouDecoding = new BILOUDecoding(gftr);
    final Set<Entity> set =
        bilouDecoding.instancesToEntities(tm, classifiedInstances, pp.getLabeledInput());

    LOG.info("Classified  #" + set.size());
    return set;
  }

  /**
   * Reads files, init. instances and builds a classifier.
   *
   * @param files files to read as training data
   * @throws Exception
   */
  public void training(//
      final String input, final Map<String, Set<Entity>> toolResults,
      final Map<String, String> oracle) throws Exception {

    LOG.info("training ...");

    // init. training data
    final IPostProcessing pp = new PostProcessing(new TokenManager(input), toolResults);
    final Map<String, String> labeledOracle = pp.getLabeledMap(oracle);
    final Map<String, Set<Entity>> labledToolResults = pp.getLabeledToolResults();

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
      final Evaluation eva = new Evaluation(instances);
      eva.evaluateModel(classifier, instances);

      // print summary
      LOG.info("summary\n" + eva.toSummaryString());

      // print the confusion matrix
      final StringBuffer cm = new StringBuffer();
      final double[][] cmMatrix = eva.confusionMatrix();
      final Set<String> sortedTypes = new TreeSet<>(BILOUEncoding.AllTypesSet);

      for (final String cl : sortedTypes) {
        cm.append(cl + "\t\t\t");
      }
      cm.append("\n");

      for (int i = 0; i < cmMatrix.length; i++) {
        for (int ii = 0; ii < cmMatrix[i].length; ii++) {
          cm.append(cmMatrix[i][ii] + "\t\t\t");
        }
        cm.append("\n");
      }

      LOG.info("confusion matrix\n" + cm.toString());

      // measure
      int i = 0;
      for (final String cl : sortedTypes) {
        LOG.info("class: " + cl);
        LOG.info("fMeasure: " + eva.fMeasure(i));
        LOG.info("precision: " + eva.precision(i));
        LOG.info("recall: " + eva.recall(i));
        i++;
      }

    } else {
      LOG.error("Build/training a classifier first.");
    }
  }

  public void setIsTrained(final boolean bool) {
    isTrained = bool;
  }

  public void setClassifier(final Classifier classifier) {
    this.classifier = classifier;
  }

  public Classifier getClassifier() {
    return classifier;
  }
}
