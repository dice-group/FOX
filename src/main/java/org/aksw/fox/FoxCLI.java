package org.aksw.fox;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.data.exception.UnsupportedLangException;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.FoxClassifierFactory;
import org.aksw.fox.nerlearner.reader.INERReader;
import org.aksw.fox.nerlearner.reader.NERReaderFactory;
import org.aksw.fox.tools.ner.Tools;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import weka.classifiers.Classifier;

/**
 * Main class for cli support.
 * 
 * @author rspeck
 * 
 */
public class FoxCLI {

    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }

    public static Logger LOG = LogManager.getLogger(FoxCLI.class);

    /**
     * 
     * @param args
     *            <p>
     *            -i for an input file or all files in a given directory (sub
     *            directories aren't included),<br>
     *            -a for an action {train | validate}
     *            </p>
     * 
     * @exception Exception
     *                if something wrong
     */
    public static void main(String[] args) throws Exception {

        final Getopt getopt = new Getopt("Fox", args, "l:x i:x a:x");
        int arg;
        // input, action, lang
        String in = "", a = "", l = "";
        while ((arg = getopt.getopt()) != -1) {
            switch (arg) {
            case 'l':
                l = String.valueOf(getopt.getOptarg());
                break;
            case 'i':
                in = String.valueOf(getopt.getOptarg());
                break;
            case 'a':
                a = String.valueOf(getopt.getOptarg());
                if (a.toLowerCase().startsWith("tr")) {
                    a = "train";
                    if (FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase().startsWith("false")) {
                        throw new Exception(
                                "You need to change the fox.properties file and set " + FoxClassifier.CFG_KEY_LEARNER_TRAINING + " to true. "
                                        + "Also you should set an output file for the new model.");
                    }
                    /*} else if (a.toLowerCase().startsWith("re")) {
                        a = "retrieve";
                        if (FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase().startsWith("tr")) {
                            throw new Exception("You need to change the fox.properties file and set " + FoxClassifier.CFG_KEY_LEARNER_TRAINING + " to false. Also you should set file for a trained model.");
                        }*/
                } else if (a.toLowerCase().startsWith("va")) {
                    a = "validate";
                    if (FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase().startsWith("true")) {
                        throw new Exception("You need to change the fox.properties file and set " + FoxClassifier.CFG_KEY_LEARNER_TRAINING + " to false.");
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

        LOG.info(files.toString());
        String[] files_array = files.toArray(new String[files.size()]);

        switch (a) {
        case "train": {
            FoxCLI.training(files_array, l);
            break;
        }
        /*        case "retrieve": {
                    MainFox.retrieve(files_array);
                    break;
                }*/
        case "validate": {
            FoxCLI.validate(files_array, l);
            break;
        }
        default:
            throw new IOException("Don't know what to do. Please set the action parameter.");
        }
    }

    public static void validate(String[] inputFiles, String lang) {
        if (lang == null || lang.isEmpty())
            LOG.warn("Missing lang paramerter!");
        try {
            ToolsGenerator toolsGenerator;

            toolsGenerator = new ToolsGenerator();

            CrossValidation cv = new CrossValidation(toolsGenerator.getNERTools(lang));
            Set<String> toolResultKeySet = cv.getTools().getToolResult().keySet();
            String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

            LOG.info("tools used: " + toolResultKeySet);

            // TODO: remove FoxClassifier dependency?
            FoxClassifier foxClassifier = new FoxClassifier();
            setClassifier(foxClassifier, prefix);
            Classifier cls = foxClassifier.getClassifier();
            cv.crossValidation(cls, inputFiles);

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Retrieve entities.
     * 
     * @param inputFiles
     *            files
     * @throws IOException
     */
    /*    public static void retrieve(String[] inputFiles) throws IOException {

            TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
            String input = trainingInputReader.getInput();

            IFox fox = new Fox();
            Map<String, String> para = fox.getDefaultParameter();
            para.put("input", input);
            fox.setParameter(para);
            fox.run();
        }*/

    /**
     * Training FoxClassifier
     * 
     * @param inputFiles
     *            files
     * @throws UnsupportedLangException
     * @throws LoadingNotPossibleException
     * @throws Exception
     */
    public static void training(String[] inputFiles, String lang) throws IOException, UnsupportedLangException, LoadingNotPossibleException {
        ToolsGenerator toolsGenerator = new ToolsGenerator();

        Tools foxNERTools = toolsGenerator.getNERTools(lang);
        FoxClassifier foxClassifier = new FoxClassifier();

        Set<String> toolResultKeySet = foxNERTools.getToolResult().keySet();
        String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

        LOG.info("tools used: " + toolResultKeySet);
        setClassifier(foxClassifier, prefix);

        // read training data
        INERReader reader = NERReaderFactory.getINERReader();
        reader.initFiles(inputFiles);

        String input = reader.getInput();
        Map<String, String> oracle = reader.getEntities();

        // retrieve entities (tool results)
        foxNERTools.setTraining(true);
        foxNERTools.getEntities(input);

        try {
            foxClassifier.training(input, foxNERTools.getToolResult(), oracle);
            String file = FoxCfg.get(FoxClassifier.CFG_KEY_MODEL_PATH) + File.separator + FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER).trim();
            foxClassifier.writeClassifier(file, lang);
            foxClassifier.eva();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setClassifier(FoxClassifier foxClassifier, String[] prefix) throws LoadingNotPossibleException {
        switch (FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER).trim()) {
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
        default:
            foxClassifier.setClassifier(FoxClassifierFactory.get(FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER), FoxCfg.get(FoxClassifier.CFG_KEY_LEARNER_OPTIONS)));
        }
    }
}
