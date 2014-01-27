package org.aksw.fox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.IPostProcessing;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
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

    public static Logger logger = Logger.getLogger(CrossValidation.class);

    public static FoxNERTools foxNERTools = new FoxNERTools();

    // cross-validation options
    static int seed = 1, folds = 10, runs = 10;

    // current states
    static String run = "";
    static String fold = "";
    static String classifierName = "";

    static StringBuffer outDetail = null;
    static StringBuffer outTotal = null;

    public static void crossValidation(Classifier cls, String[] inputFiles) throws Exception {
        classifierName = cls.getClass().getName();
        classifierName = classifierName.substring(classifierName.lastIndexOf('.') == -1 ? 0 : classifierName.lastIndexOf('.') + 1);

        // read data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        TokenManager tokenManager = new TokenManager(trainingInputReader.getInput());

        // prepare data
        IPostProcessing pp = null;
        {

            foxNERTools.setTraining(true);
            foxNERTools.getEntities(tokenManager.getInput());
            pp = new PostProcessing(tokenManager, foxNERTools.getToolResult());
        }

        // init. instances
        Instances instances = null;
        {
            FoxInstances foxInstances = new FoxInstances();
            Map<String, String> oracle = pp.getLabeledMap(trainingInputReader.getEntities());
            Map<String, Set<Entity>> toolResults = pp.getLabeledToolResults();
            Set<String> token = pp.getLabeledInput();

            instances = foxInstances.getInstances(token, toolResults, oracle);
        }

        // write arff file training data
        {
            ArffSaver saver = new ArffSaver();
            try {
                saver.setInstances(instances);
                saver.setFile(new File("./tmp/training.arff"));
                saver.writeBatch();
            } catch (IOException e) {
                logger.error("/n", e);
            }
        }
        // perform cross-validation runs
        for (int i = 0; i < runs; i++) {

            seed = i + 1;
            run = new Integer(i + 1).toString();

            // randomize instances
            Instances randInstances = new Instances(instances);
            {
                Random rand = new Random(seed);
                randInstances.randomize(rand);
                if (randInstances.classAttribute().isNominal())
                    randInstances.stratify(folds);
            }

            // perform cross-validation
            Evaluation evalAll = new Evaluation(randInstances);
            for (int n = 0; n < folds; n++) {
                logger.info("Validation run = " + (run));
                logger.info("Validation fold k = " + (n + 1));
                fold = new Integer(n + 1).toString();

                Instances train = randInstances.trainCV(folds, n);
                Instances test = randInstances.testCV(folds, n);

                // build and evaluate classifier
                Classifier clsCopy = Classifier.makeCopy(cls);
                clsCopy.buildClassifier(train);
                Evaluation eval = new Evaluation(randInstances);

                double[] predictions = eval.evaluateModel(clsCopy, test);
                evalAll.evaluateModel(clsCopy, test);

                if (true) {
                    // write used test data with classification to arff
                    for (int j = 0; j < test.numInstances(); j++)
                        test.instance(j).setClassValue(predictions[j]);

                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(test);
                    try {
                        saver.setFile(new
                                File("./tmp/classified_" + (i + 1) + "_" + (n + 1) +
                                        ".arff"));
                        saver.writeBatch();
                    } catch (IOException e) {
                        logger.error("\n", e);
                    }
                }

                // write
                writeConfusionMatrix(eval);

                // prints
                /*
                 * System.out.println(eval.toMatrixString(
                 * "=== Confusion matrix for fold " + (n + 1) + "/" + folds +
                 * "(" + (i + 1) + ")" + " ===\n") );
                 */
                printMeasures(eval);
                try {
                    logger.info(eval.toClassDetailsString());
                    logger.info(eval.toMatrixString());
                } catch (Exception e) {
                    logger.error("\n", e);
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
            /*
             * System.out.println(evalAll.toSummaryString( "=== " + folds +
             * "-fold Cross-validation ===", false) );
             */
            myprint(evalAll, cls, randInstances);

        }
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

            logger.info("=== classes ===");
            logger.info("class: " + cl);
            logger.info("fMeasure: " + f1);
            logger.info("precision: " + p);
            logger.info("recall: " + r);
        }
    }

    public static void myprint(Evaluation eval, Classifier classifier, Instances instances) {

        logger.info("=== Run information ===\n\n");
        logger.info("Scheme: " + classifier.getClass().getName() + " Options: " + Utils.joinOptions(classifier.getOptions()));
        logger.info("Relation: " + instances.relationName());
        logger.info("Instances: " + instances.numInstances());
        logger.info("Attributes: " + instances.numAttributes());

        logger.info("=== Classifier model ===\n\n");
        logger.info(classifier.toString());

        logger.info("=== Summary ===\n");
        logger.info(eval.toSummaryString());

        try {
            logger.info(eval.toClassDetailsString());
            logger.info(eval.toMatrixString());
        } catch (Exception e) {
            logger.error("\n", e);
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
