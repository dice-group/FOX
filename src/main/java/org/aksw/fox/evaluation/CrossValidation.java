package org.aksw.fox.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.nerlearner.IPostProcessing;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.TokenManager;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.nerlearner.reader.INERReader;
import org.aksw.fox.nerlearner.reader.NERReaderFactory;
import org.aksw.fox.tools.Tools;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class CrossValidation {

  public static final Logger LOG = LogManager.getLogger(CrossValidation.class);
  public static final String CFG_KEY_CROSSVALIDATION_RUNS =
      CrossValidation.class.getName().concat(".runs");

  protected Tools tools;

  // cross-validation options
  static int seed = 1;
  static int folds = 10;
  static int runs = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

  // current states
  static String run = "";
  static String fold = "";
  static String classifierName = "";

  static StringBuffer outDetail = null;
  static StringBuffer outTotal = null;

  /**
   *
   * @param tools
   */
  public CrossValidation(final Tools tools) {
    this.tools = tools;
  }

  public Tools getTools() {
    return tools;
  }

  public void crossValidation(final Classifier cls, final String[] inputFiles) throws Exception {
    classifierName = cls.getClass().getName();
    classifierName = classifierName.substring(//
        classifierName.lastIndexOf('.') == -1 ? 0 : classifierName.lastIndexOf('.') + 1//
    );

    // read data
    // read training data
    final INERReader reader = NERReaderFactory.getINERReader();
    reader.initFiles(inputFiles);

    final TokenManager tokenManager = new TokenManager(reader.getInput());

    // prepare data
    IPostProcessing pp = null;
    {
      tools.setTraining(true);
      tools.getEntities(tokenManager.getInput());
      pp = new PostProcessing(tokenManager, tools.getToolResult());
    }

    // init. instances
    Instances instances = null;
    {
      final FoxInstances foxInstances = new FoxInstances();
      final Map<String, String> oracle = pp.getLabeledMap(reader.getEntities());
      final Map<String, Set<Entity>> toolResults = pp.getLabeledToolResults();
      final Set<String> token = pp.getLabeledInput();

      instances = foxInstances.getInstances(token, toolResults, oracle);
    }

    // write arff file training data
    File tmp = new File("tmp");
    if (!tmp.exists()) {
      tmp.mkdir();
    }
    tmp = null;

    {
      final ArffSaver saver = new ArffSaver();
      try {
        saver.setInstances(instances);
        saver.setFile(new File("tmp/training.arff"));
        saver.writeBatch();
      } catch (final IOException e) {
        LOG.error("/n", e);
      }
    }
    // perform cross-validation runs
    for (int i = 0; i < runs; i++) {

      seed = i + 1;
      run = new Integer(i + 1).toString();

      // perform cross-validation
      final Evaluation evalAll = new Evaluation(instances);
      for (int n = 0; n < folds; n++) {
        LOG.info("Validation run = " + (run));
        LOG.info("Validation fold k = " + (n + 1));
        fold = new Integer(n + 1).toString();

        final Instances train = instances.trainCV(folds, n);
        final Instances test = instances.testCV(folds, n);

        // build and evaluate classifier
        final Classifier clsCopy = Classifier.makeCopy(cls);
        clsCopy.buildClassifier(train);
        final Evaluation eval = new Evaluation(instances);

        final double[] predictions = eval.evaluateModel(clsCopy, test);
        evalAll.evaluateModel(clsCopy, test);

        if (true) {
          // write used test data with classification to arff
          for (int j = 0; j < test.numInstances(); j++) {
            test.instance(j).setClassValue(predictions[j]);
          }

          final ArffSaver saver = new ArffSaver();
          saver.setInstances(test);
          try {
            saver.setFile(new File("tmp/classified_" + (i + 1) + "_" + (n + 1) + ".arff"));
            saver.writeBatch();
          } catch (final IOException e) {
            LOG.error("\n", e);
          }
        }

        // write
        writeConfusionMatrix(eval);

        printMeasures(eval);
        try {
          LOG.info(eval.toClassDetailsString());
          LOG.info(eval.toMatrixString());
        } catch (final Exception e) {
          LOG.error("\n", e);
        }

      }
      // write totals
      if (outTotal == null) {
        outTotal = new StringBuffer();
        outTotal.append("run,").append("classifier,").append("class,").append("a,").append("b,")
            .append("c,").append("d").append('\n');
      }
      final double[][] cmMatrix = evalAll.confusionMatrix();
      for (int k = 0; k < EntityClassMap.entityClasses.size(); k++) {
        outTotal.append(i + 1).append(',').append(classifierName).append(',')
            .append(EntityClassMap.entityClasses.get(k)).append(',')
            .append(new Double(cmMatrix[k][0]).intValue()).append(',')
            .append(new Double(cmMatrix[k][1]).intValue()).append(',')
            .append(new Double(cmMatrix[k][2]).intValue()).append(',')
            .append(new Double(cmMatrix[k][3]).intValue()).append('\n');
      }
      myprint(evalAll, cls, instances);
    }
    File eval = new File("eval");
    if (!eval.exists()) {
      eval.mkdir();
    }
    eval = null;

    String filename = "eval/" + classifierName + "_total.csv";
    CSVWriter writer = new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
    writer.writeNext(outTotal.toString().split(","));
    writer.close();

    //
    filename = "eval/" + classifierName + ".csv";
    writer = new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
    writer.writeNext(outDetail.toString().split(","));
    writer.close();
  }

  public static void writeConfusionMatrix(final Evaluation eval) {

    final double[][] cmMatrix = eval.confusionMatrix();

    // header
    final StringBuffer cm = new StringBuffer();
    for (final String cl : EntityClassMap.entityClasses) {
      cm.append(cl + "\t");
    }
    cm.append("\n");

    // values
    for (int i = 0; i < cmMatrix.length; i++) {
      for (int ii = 0; ii < cmMatrix[i].length; ii++) {
        cm.append(cmMatrix[i][ii] + "\t\t");
      }
      cm.append("\n");
    }

    // write buffer for file
    for (int i = 0; i < EntityClassMap.entityClasses.size(); i++) {
      writeBuffer(run, fold, classifierName, EntityClassMap.entityClasses.get(i),
          String.valueOf(new Double(cmMatrix[i][0]).intValue()),
          String.valueOf(new Double(cmMatrix[i][1]).intValue()),
          String.valueOf(new Double(cmMatrix[i][2]).intValue()),
          String.valueOf(new Double(cmMatrix[i][3]).intValue()));
    }
  }

  public static void printMeasures(final Evaluation eval) {

    final List<String> cat = EntityClassMap.entityClasses;
    for (final String cl : EntityClassMap.entityClasses) {

      final double f1 = eval.fMeasure(cat.indexOf(cl));
      final double p = eval.precision(cat.indexOf(cl));
      final double r = eval.recall(cat.indexOf(cl));

      LOG.info("=== classes ===");
      LOG.info("class: " + cl);
      LOG.info("fMeasure: " + f1);
      LOG.info("precision: " + p);
      LOG.info("recall: " + r);
    }
  }

  public static void myprint(final Evaluation eval, final Classifier classifier,
      final Instances instances) {

    LOG.info("=== Run information ===\n\n");
    LOG.info("Scheme: " + classifier.getClass().getName() + " Options: "
        + Utils.joinOptions(classifier.getOptions()));
    LOG.info("Relation: " + instances.relationName());
    LOG.info("Instances: " + instances.numInstances());
    LOG.info("Attributes: " + instances.numAttributes());

    LOG.info("=== Classifier model ===\n\n");
    LOG.info(classifier.toString());

    LOG.info("=== Summary ===\n");
    LOG.info(eval.toSummaryString());

    try {
      LOG.info(eval.toClassDetailsString());
      LOG.info(eval.toMatrixString());
    } catch (final Exception e) {
      LOG.error("\n", e);
    }
  }

  public static void writeBuffer(final String run, final String fold, final String classifier,
      final String classs, final String a, final String b, final String c, final String d) {
    if (outDetail == null) {
      outDetail = new StringBuffer()//
          .append("run,").append("fold,").append("classifier,").append("class,")//
          .append("a,").append("b,").append("c,").append("d")//
          .append('\n');
    }
    outDetail//
        .append(run).append(',').append(fold).append(',').append(classifier).append(',')
        .append(classs).append(',')//
        .append(a).append(',').append(b).append(',').append(c).append(',').append(d)//
        .append('\n');
  }
}
