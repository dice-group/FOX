package org.aksw.fox;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.FoxClassifierFactory;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import weka.classifiers.Classifier;

/**
 * Main class for cli support.
 * 
 * @author rspeck
 * 
 */
public class MainFox {

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    public static Logger logger = Logger.getLogger(MainFox.class);

    /**
     * 
     * @param args
     *            <p>
     *            -i for an input file or all files in a given directory (sub
     *            directories aren't included),<br>
     *            -a for an action {train | retrieve | validate}
     *            </p>
     * 
     * @exception Exception
     *                if something wrong
     */
    public static void main(String[] args) throws Exception {

        final Getopt getopt = new Getopt("Fox", args, "i:x a:x");

        int arg;
        String in = "", a = "";
        while ((arg = getopt.getopt()) != -1) {
            switch (arg) {
            case 'i':
                in = String.valueOf(getopt.getOptarg());
                break;
            case 'a':
                a = String.valueOf(getopt.getOptarg());
                if (a.toLowerCase().startsWith("tr")) {
                    a = "train";
                    if (FoxCfg.get("tainFox").toLowerCase().startsWith("false")) {
                        throw new Exception("You need to change the fox.properties file and set tainFox to true. Also you should set an output file for the new model.");
                    }
                } else if (a.toLowerCase().startsWith("re")) {
                    a = "retrieve";
                    if (FoxCfg.get("tainFox").toLowerCase().startsWith("tr")) {
                        throw new Exception("You need to change the fox.properties file and set tainFox to false. Also you should set file for a trained model.");
                    }
                } else if (a.toLowerCase().startsWith("va")) {
                    a = "validate";
                    if (FoxCfg.get("tainFox").toLowerCase().startsWith("true")) {
                        throw new Exception("You need to change the fox.properties file and set tainFox to false.");
                    }
                } else {
                    throw new Exception("Wrong action value.");
                }
                break;
            }
        }
        List<String> files = new ArrayList<>();

        File file = new File(in);
        if (!file.exists()) {
            throw new IOException("Can't find file or directory.");
        } else {
            if (file.isDirectory()) {
                // read all files in a directory
                for (File fileEntry : file.listFiles()) {
                    if (fileEntry.isFile() && !fileEntry.isHidden()) {
                        files.add(fileEntry.getAbsolutePath());
                    }
                }
            } else if (file.isFile()) {
                files.add(file.getAbsolutePath());
            } else {
                throw new IOException("Input isn't a valid file or directory.");
            }
        }

        logger.info(files.toString());
        String[] files_array = files.toArray(new String[files.size()]);

        switch (a) {
        case "train": {
            MainFox.training(files_array);
            break;
        }
        case "retrieve": {
            MainFox.retrieve(files_array);
            break;
        }
        case "validate": {
            MainFox.validate(files_array);
            break;
        }
        default:
            throw new IOException("Don't know what to do. Please set the action parameter.");
        }
    }

    public static void validate(String[] inputFiles) {
        // TODO: remove FoxClassifier dependency?
        FoxClassifier foxClassifier = new FoxClassifier();

        Set<String> toolResultKeySet = CrossValidation.foxNERTools.getToolResult().keySet();
        String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

        logger.info("tools used: " + toolResultKeySet);
        setClassifier(foxClassifier, prefix);

        Classifier cls = foxClassifier.getClassifier();

        try {
            CrossValidation.crossValidation(cls, inputFiles);
        } catch (Exception e) {
            logger.error("\n", e);
        }
    }

    /**
     * Retrieve entities.
     * 
     * @param inputFiles
     *            files
     * @throws IOException
     */
    public static void retrieve(String[] inputFiles) throws IOException {

        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        String input = trainingInputReader.getInput();

        Fox fox = new Fox();
        Map<String, String> para = fox.getDefaultParameter();
        para.put("input", input);
        fox.setParameter(para);
        fox.run();
        // logger.info(fox.getResults());
    }

    /**
     * Training FoxClassifier
     * 
     * @param inputFiles
     *            files
     * @throws Exception
     */
    public static void training(String[] inputFiles) throws IOException {

        FoxNERTools foxNERTools = new FoxNERTools();
        FoxClassifier foxClassifier = new FoxClassifier();

        Set<String> toolResultKeySet = foxNERTools.getToolResult().keySet();
        String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

        logger.info("tools used: " + toolResultKeySet);
        setClassifier(foxClassifier, prefix);

        // read training data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        String input = trainingInputReader.getInput();
        Map<String, String> oracle = trainingInputReader.getEntities();

        // retrieve entities (tool results)
        foxNERTools.setTraining(true);
        foxNERTools.getEntities(input);

        try {
            foxClassifier.training(input, foxNERTools.getToolResult(), oracle);
            String file = FoxCfg.get("modelPath") + System.getProperty("file.separator") + FoxCfg.get("learner").trim();
            foxClassifier.writeClassifier(file);
            foxClassifier.eva();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setClassifier(FoxClassifier foxClassifier, String[] prefix) {
        switch (FoxCfg.get("learner").trim()) {
        case "result_vote": {
            foxClassifier.setIsTrained(true);
            foxClassifier.setClassifier(FoxClassifierFactory.getClassifierResultVote(prefix));
            break;
        }
        case "class_vote": {
            foxClassifier.setIsTrained(true);
            foxClassifier.setClassifier(FoxClassifierFactory.getClassifierClassVote(prefix));
            break;
        }

        case "j48": {
            foxClassifier.setClassifier(FoxClassifierFactory.get("weka.classifiers.trees.J48"));
            break;
        }
        default:
        case "mp":
            foxClassifier.setClassifier(FoxClassifierFactory.getClassifierMultilayerPerceptron());
        }
    }
}
