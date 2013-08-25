package org.aksw.fox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.PostProcessingInterface;
import org.aksw.fox.nerlearner.reader.FoxInstances;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

/**
 * 
 * @author rspeck
 * 
 */
public class CrossValidation {

    public static Logger logger = Logger.getLogger(CrossValidation.class);

    public static FoxNERTools foxNERTools = new FoxNERTools();
    static int seed = 1, folds = 10;
    static Map<Integer, Map<String, List<Double>>> kMap = new HashMap<>();

    public static void crossValidation(Classifier cls, String[] inputFiles) throws Exception {

        // read data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        TokenManager tokenManager = new TokenManager(trainingInputReader.getInput());

        // prepare data
        PostProcessingInterface pp = null;
        {

            foxNERTools.setTraining(true);
            foxNERTools.getNER(tokenManager.getInput());

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

        // randomize instances
        Instances randInstances = new Instances(instances);
        {
            Random rand = new Random(seed);
            randInstances.randomize(rand);
            if (randInstances.classAttribute().isNominal())
                randInstances.stratify(folds);
        }

        // perform cross-validation
        Evaluation eval = new Evaluation(randInstances);
        for (int n = 0; n < folds; n++) {
            logger.info("Validation fold k = " + (n + 1));
            Instances train = randInstances.trainCV(folds, n);
            Instances test = randInstances.testCV(folds, n);

            // build and evaluate classifier
            Classifier clsCopy = Classifier.makeCopy(cls);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);

            printConfusionMatrix(eval);
            printMeasures(eval);
        }
        printSetup(cls, eval, randInstances);
        printOverall();
    }

    public static void printConfusionMatrix(Evaluation eval) {
        StringBuffer cm = new StringBuffer();
        double[][] cmMatrix = eval.confusionMatrix();
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
    }

    public static void printMeasures(Evaluation eval) {

        // remember values
        int currentK = kMap.keySet().size() + 1;
        kMap.put(currentK, new HashMap<String, List<Double>>());

        //
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

            kMap.get(currentK).put(cl, new ArrayList<Double>());
            kMap.get(currentK).get(cl).add(f1);
            kMap.get(currentK).get(cl).add(p);
            kMap.get(currentK).get(cl).add(r);
        }
    }

    public static void printSetup(Classifier cls, Evaluation eval, Instances instances) {
        logger.info("=== setup ===");
        logger.info("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
        logger.info("Dataset: " + instances.relationName());
        logger.info(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
    }

    public static void printOverall() {

        logger.info("=== total ===");

        int k = kMap.size();

        Map<String, List<Double>> classTotal = new HashMap<>();
        for (int i = 1; i <= k; i++) {

            Map<String, List<Double>> classValues = kMap.get(i);
            for (String cls : classValues.keySet()) {

                // init.
                if (classTotal.get(cls) == null) {
                    classTotal.put(cls, new ArrayList<Double>());

                    classTotal.get(cls).add(0D);
                    classTotal.get(cls).add(0D);
                    classTotal.get(cls).add(0D);
                }
                classTotal.get(cls).set(0, classTotal.get(cls).get(0) + classValues.get(cls).get(0));
                classTotal.get(cls).set(1, classTotal.get(cls).get(1) + classValues.get(cls).get(1));
                classTotal.get(cls).set(2, classTotal.get(cls).get(2) + classValues.get(cls).get(2));
            }
        }
        double f1 = 0, p = 0, r = 0;
        for (String cls : classTotal.keySet()) {

            f1 += classTotal.get(cls).get(0) / k;
            p += classTotal.get(cls).get(1) / k;
            r += classTotal.get(cls).get(2) / k;

            logger.info("f1 " + cls + ": " + classTotal.get(cls).get(0) / k);
            logger.info("p " + cls + ": " + classTotal.get(cls).get(1) / k);
            logger.info("r " + cls + ": " + classTotal.get(cls).get(2) / k);
        }
        logger.info("=== overall ===");
        logger.info("f1: " + f1 / classTotal.keySet().size());
        logger.info("p: " + p / classTotal.keySet().size());
        logger.info("r: " + r / classTotal.keySet().size());

    }
}
