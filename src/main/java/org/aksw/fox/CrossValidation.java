package org.aksw.fox;

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

    public static void crossValidation(Classifier cls, String[] inputFiles) throws Exception {

        // read data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        TokenManager tokenManager = new TokenManager(trainingInputReader.getInput());

        // prepare data
        PostProcessingInterface pp = null;
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

        myprint(eval, cls, randInstances);
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
}
