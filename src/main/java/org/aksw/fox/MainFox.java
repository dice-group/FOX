package org.aksw.fox;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import weka.classifiers.meta.Vote;
import weka.core.SelectedTag;

/**
 * A class with a main method to start or train FOX programmatically.
 * 
 * @author rspeck
 * 
 */

public class MainFox {

    public static Logger logger = Logger.getLogger(MainFox.class);

    /**
     * 
     * @param args
     *            <p>
     *            -i for an input file or all files in a given directory (sub
     *            directories aren't included),<br>
     *            -a for an action {train|retrieve}
     *            </p>
     * 
     * @exception Exception
     *                if something wrong
     */
    public static void main(String[] args) throws Exception {

        PropertyConfigurator.configure("log4j.properties");

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
                } else {
                    throw new IOException("Wrong action value.");
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

        String[] files_array = files.toArray(new String[files.size()]);

        if (a.equals("train")) {
            MainFox.logger.info(files.toString());
            MainFox.training(files_array);

        } else if (a.equals("retrieve")) {

            MainFox.retrieve(files_array);

        } else {
            throw new IOException("Don't know what to do. Please set the action parameter");
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
        // Fox.logger.info(fox.getResults());
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

        switch (FoxCfg.get("learner").trim()) {
        case "result_vote": {
            SelectedTag st = new SelectedTag(Vote.AVERAGE_RULE, Vote.TAGS_RULES);
            String[] prefix = foxNERTools.getToolResult().keySet().toArray(new String[foxNERTools.getToolResult().size()]);

            foxClassifier.setClassifierResultVote(prefix, st);
            break;
        }
        case "class_vote": {
            SelectedTag st = new SelectedTag(Vote.MAX_RULE, Vote.TAGS_RULES);
            String[] prefix = foxNERTools.getToolResult().keySet().toArray(new String[foxNERTools.getToolResult().size()]);
            foxClassifier.setClassifierClassVote(prefix, st);
            break;
        }
        default:
        case "mp":
            foxClassifier.setClassifierMultilayerPerceptron();
        }

        // read training data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        String input = trainingInputReader.getInput();
        Map<String, String> oracle = trainingInputReader.getEntities();

        // TODO:
        // move to ...?
        {
            // TODO:
            // remove Entity dependency? implement a new method
            // repairEntities for maps

            // remove oracle entities aren't in input
            Set<Entity> set = new HashSet<>();
            for (Entry<String, String> oracleEntry : oracle.entrySet())
                set.add(new Entity(oracleEntry.getKey(), oracleEntry.getValue()));

            // repair entities (use fox token)
            TokenManager tokenManager = new TokenManager(input);
            tokenManager.repairEntities(set);

            // use
            oracle.clear();
            for (Entity e : set)
                oracle.put(e.getText(), e.getType());
        }

        // retrieve entities (tool results)
        foxNERTools.setTraining(true);
        foxNERTools.getNER(input);

        try {
            foxClassifier.training(input, foxNERTools.getToolResult(), oracle);
            foxClassifier.writeClassifier();
            foxClassifier.eva();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
