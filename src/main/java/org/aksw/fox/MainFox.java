package org.aksw.fox;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.utils.FoxCfg;
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
    /**
     * The main method.
     * 
     * @param a
     *            not used
     * @throws Exception
     */
    public static void main(String[] a) throws Exception {

        PropertyConfigurator.configure("log4j.properties");
        /*
         * { // input 2 int max = 20; String[] files = new String[max]; for (int
         * i = 1; i <= max; i++) files[i - 1] = "input/2/" + i;
         * 
         * MainFox.training(files); // MainFox.retrieve(files); }
         */
        { // input 1
            int max = 4;
            String[] files = new String[max];
            for (int i = 1; i <= max; i++)
                files[i - 1] = "input/1/" + i;

            MainFox.training(new String[] { "input/1/2" });
            // MainFox.training(files);
            // MainFox.retrieve(new String[] { "input/2/2" });
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
    public static void training(String[] inputFiles) throws Exception {

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

        foxClassifier.training(input, foxNERTools.getToolResult(), oracle);
        foxClassifier.writeClassifier();
        foxClassifier.eva();
    }
}
