package org.aksw.fox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.IPostProcessing;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.nerlearner.reader.INERReader;
import org.aksw.fox.nerlearner.reader.NERReaderFactory;
import org.aksw.fox.tools.ner.Tools;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * @author rspeck
 * 
 */
public class CrossValidation {

    public static final Logger LOG                          = LogManager.getLogger(CrossValidation.class);
    public static final String CFG_KEY_CROSSVALIDATION_RUNS = CrossValidation.class.getName().concat(".runs");

    protected Tools            tools;

    // cross-validation options
    static int                 seed                         = 1;
    static int                 folds                        = 10;
    static int                 runs                         = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

    // current states
    static String              run                          = "";
    static String              fold                         = "";
    static String              classifierName               = "";

    static StringBuffer        outDetail                    = null;
    static StringBuffer        outTotal                     = null;

    public CrossValidation(Tools tools) {
        this.tools = tools;
    }

    public Tools getTools() {
        return tools;
    }

    public void crossValidation(Classifier cls, String[] inputFiles) throws Exception {
        classifierName = cls.getClass().getName();
        classifierName = classifierName.substring(classifierName.lastIndexOf('.') == -1 ? 0 : classifierName.lastIndexOf('.') + 1);

        // read data
        // read training data
        INERReader reader = NERReaderFactory.getINERReader();
        reader.initFiles(inputFiles);

        TokenManager tokenManager = new TokenManager(reader.getInput());

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
            FoxInstances foxInstances = new FoxInstances();
            Map<String, String> oracle = pp.getLabeledMap(reader.getEntities());
            Map<String, Set<Entity>> toolResults = pp.getLabeledToolResults();
            Set<String> token = pp.getLabeledInput();

            instances = foxInstances.getInstances(token, toolResults, oracle);
        }

        // write arff file training data
        File tmp = new File("tmp");
        if (!tmp.exists())
            tmp.mkdir();
        tmp = null;

        {
            ArffSaver saver = new ArffSaver();
            try {
                saver.setInstances(instances);
                saver.setFile(new File("tmp/training.arff"));
                saver.writeBatch();
            } catch (IOException e) {
                LOG.error("/n", e);
            }
        }
        // perform cross-validation runs
        for (int i = 0; i < runs; i++) {

            seed = i + 1;
            run = new Integer(i + 1).toString();

            // perform cross-validation
            Evaluation evalAll = new Evaluation(instances);
            for (int n = 0; n < folds; n++) {
                LOG.info("Validation run = " + (run));
                LOG.info("Validation fold k = " + (n + 1));
                fold = new Integer(n + 1).toString();

                Instances train = instances.trainCV(folds, n);
                Instances test = instances.testCV(folds, n);

                // build and evaluate classifier
                Classifier clsCopy = Classifier.makeCopy(cls);
                clsCopy.buildClassifier(train);
                Evaluation eval = new Evaluation(instances);

                double[] predictions = eval.evaluateModel(clsCopy, test);
                evalAll.evaluateModel(clsCopy, test);

                if (true) {
                    // write used test data with classification to arff
                    for (int j = 0; j < test.numInstances(); j++)
                        test.instance(j).setClassValue(predictions[j]);

                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(test);
                    try {
                        saver.setFile(new File("tmp/classified_" + (i + 1) + "_" + (n + 1) + ".arff"));
                        saver.writeBatch();
                    } catch (IOException e) {
                        LOG.error("\n", e);
                    }
                }

                // write
                writeConfusionMatrix(eval);

                printMeasures(eval);
                try {
                    LOG.info(eval.toClassDetailsString());
                    LOG.info(eval.toMatrixString());
                } catch (Exception e) {
                    LOG.error("\n", e);
                }

            }
            // write totals
            if (outTotal == null) {
                outTotal = new StringBuffer();
                outTotal.append("run,");
                outTotal.append("classifier,");
                outTotal.append("class,");
                outTotal.append("a,");
                outTotal.append("b,");
                outTotal.append("c,");
                outTotal.append("d");
                outTotal.append('\n');
            }
            double[][] cmMatrix = evalAll.confusionMatrix();
            for (int k = 0; k < EntityClassMap.entityClasses.size(); k++) {
                outTotal.append(i + 1);
                outTotal.append(',');
                outTotal.append(classifierName);
                outTotal.append(',');
                outTotal.append(EntityClassMap.entityClasses.get(k));
                outTotal.append(',');
                outTotal.append(new Double(cmMatrix[k][0]).intValue());
                outTotal.append(',');
                outTotal.append(new Double(cmMatrix[k][1]).intValue());
                outTotal.append(',');
                outTotal.append(new Double(cmMatrix[k][2]).intValue());
                outTotal.append(',');
                outTotal.append(new Double(cmMatrix[k][3]).intValue());
                outTotal.append('\n');
            }
            myprint(evalAll, cls, instances);
        }
        File eval = new File("eval");
        if (!eval.exists())
            eval.mkdir();
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

    public static void writeConfusionMatrix(Evaluation eval) {

        double[][] cmMatrix = eval.confusionMatrix();

        // header
        StringBuffer cm = new StringBuffer();
        for (String cl : EntityClassMap.entityClasses) {
            cm.append(cl + "\t");
        }
        cm.append("\n");

        // values
        for (int i = 0; i < cmMatrix.length; i++) {
            for (int ii = 0; ii < cmMatrix[i].length; ii++)
                cm.append(cmMatrix[i][ii] + "\t\t");
            cm.append("\n");
        }

        // write buffer for file
        for (int i = 0; i < EntityClassMap.entityClasses.size(); i++) {
            writeBuffer(
                    run, fold,
                    classifierName,
                    EntityClassMap.entityClasses.get(i),
                    String.valueOf(new Double(cmMatrix[i][0]).intValue()),
                    String.valueOf(new Double(cmMatrix[i][1]).intValue()),
                    String.valueOf(new Double(cmMatrix[i][2]).intValue()),
                    String.valueOf(new Double(cmMatrix[i][3]).intValue()));
        }
    }

    public static void printMeasures(Evaluation eval) {

        final List<String> cat = EntityClassMap.entityClasses;
        for (String cl : EntityClassMap.entityClasses) {

            double f1 = eval.fMeasure(cat.indexOf(cl));
            double p = eval.precision(cat.indexOf(cl));
            double r = eval.recall(cat.indexOf(cl));

            LOG.info("=== classes ===");
            LOG.info("class: " + cl);
            LOG.info("fMeasure: " + f1);
            LOG.info("precision: " + p);
            LOG.info("recall: " + r);
        }
    }

    public static void myprint(Evaluation eval, Classifier classifier, Instances instances) {

        LOG.info("=== Run information ===\n\n");
        LOG.info("Scheme: " + classifier.getClass().getName() + " Options: " + Utils.joinOptions(classifier.getOptions()));
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
        } catch (Exception e) {
            LOG.error("\n", e);
        }
    }

    public static void writeBuffer(String run, String fold, String classifier, String classs, String a, String b, String c, String d) {
        if (outDetail == null) {
            outDetail = new StringBuffer();
            outDetail.append("run,");
            outDetail.append("fold,");
            outDetail.append("classifier,");
            outDetail.append("class,");
            outDetail.append("a,");
            outDetail.append("b,");
            outDetail.append("c,");
            outDetail.append("d");
            outDetail.append('\n');
        }
        outDetail.append(run);
        outDetail.append(',');
        outDetail.append(fold);
        outDetail.append(',');
        outDetail.append(classifier);
        outDetail.append(',');
        outDetail.append(classs);
        outDetail.append(',');
        outDetail.append(a);
        outDetail.append(',');
        outDetail.append(b);
        outDetail.append(',');
        outDetail.append(c);
        outDetail.append(',');
        outDetail.append(d);
        outDetail.append('\n');
    }
}
