package org.aksw.fox;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.PostProcessing;
import org.aksw.fox.nerlearner.PostProcessingInterface;
import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.nertools.FoxNERTools;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FoxTest {

    String[] inputFiles = new String[] { "input/1/2" };

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        // runFoxWithoutThread();
        // try {
        // train();
        // } catch (Exception e) {
        // Fox.logger.error("\n", e);
        // }
    }

    public void runFoxWithoutThread() {

        Fox fox = new Fox();
        fox.setParameter(fox.getDefaultParameter());
        fox.run();

        assertTrue(null != fox.getResults());
    }

    public void retrieve() throws IOException {
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        String input = trainingInputReader.getInput();

        Fox fox = new Fox();
        Map<String, String> para = fox.getDefaultParameter();
        para.put("input", input);
        fox.setParameter(para);
        fox.run();
        // Fox.logger.info(fox.getResults());

    }

    // learn and retrieve with same data set to retrieve oracle???
    public void train() throws Exception {

        // 1. learn
        FoxNERTools foxNERTools = new FoxNERTools();
        foxNERTools.setTraining(true);

        FoxClassifier foxClassifier = new FoxClassifier();

        switch (FoxCfg.get("learner").trim()) {

        case "vote":
            String[] prefix = foxNERTools.getToolResult().keySet().toArray(new String[foxNERTools.getToolResult().size()]);
            foxClassifier.setClassifierResultVote(prefix);
            break;

        default:
        case "mp":
            foxClassifier.setClassifierMultilayerPerceptron();
        }
        // read training data
        TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles);
        String input = trainingInputReader.getInput();
        Map<String, String> oracle = trainingInputReader.getEntities();

        // remove oracle entities not in input
        List<String> remove = new ArrayList<>();
        String tokenInput = new TokenManager(input).getTokenInput();

        for (Entry<String, String> oracleEntry : oracle.entrySet())
            if (getIndices(oracleEntry.getKey(), tokenInput).size() == 0)
                remove.add(oracleEntry.getKey());

        System.out.println(remove);

        for (String key : remove)
            oracle.remove(key);

        // retrieve entities (tool results)
        foxNERTools.getNER(input);
        Map<String, Set<Entity>> toolResults = foxNERTools.getToolResult();

        foxClassifier.training(input, toolResults, oracle);

        foxClassifier.writeClassifier(FoxCfg.get("modelPath") + System.getProperty("file.separator") + "test");
        foxClassifier.eva();

        // 2. retrieve
        foxNERTools.setTraining(false);
        Set<Entity> oracleSet = new HashSet<>();
        for (Entry<String, String> entry : trainingInputReader.getEntities().entrySet())
            oracleSet.add(new Entity(entry.getKey(), entry.getValue()));

        for (Entry<String, Set<Entity>> e : toolResults.entrySet())
            e.setValue(oracleSet);

        PostProcessingInterface pp = new PostProcessing(new TokenManager(input), toolResults);
        foxClassifier.readClassifier(FoxCfg.get("modelPath") + System.getProperty("file.separator") + "test");
        Set<Entity> resultSet = foxClassifier.classify(pp);

        assertTrue(resultSet.containsAll(oracleSet));
    }

    public static synchronized List<Integer> getIndices(String word, String sentence) {

        List<Integer> list = new ArrayList<>();

        if (word.length() > sentence.length())
            return list;

        Matcher matcher = Pattern.compile("\\b" + word + "\\b").matcher(sentence);
        while (matcher.find())
            list.add(matcher.start());

        return list;
    }
}
