package org.aksw.fox.tools.ke;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import weka.core.Utils;

import com.entopix.maui.filters.MauiFilter.MauiFilterException;
import com.entopix.maui.main.MauiModelBuilder;
import com.entopix.maui.main.MauiTopicExtractor;
import com.entopix.maui.main.MauiWrapper;
import com.entopix.maui.stemmers.PorterStemmer;
import com.entopix.maui.util.Topic;

public class Maui {

    public static void runMaui(String[] options) throws Exception {

        String inputString = options[0];
        // checking if it's a file or an input string
        File testFile = new File(inputString);
        if (testFile.exists()) {
            inputString = FileUtils.readFileToString(testFile);
        }

        String modelName = Utils.getOption('m', options);
        if (modelName.length() == 0) {
            throw new Exception("Name of model required argument.");
        }

        String vocabularyName = Utils.getOption('v', options);
        if (vocabularyName.length() == 0) {
            throw new Exception("Use \"none\" or supply the name of vocabulary .");
        }

        String vocabularyFormat = Utils.getOption('f', options);
        if (vocabularyFormat.length() > 0 &&
                (!vocabularyFormat.equals("skos") && (!vocabularyFormat.equals("text")))) {
            throw new Exception(
                    "Vocabulary format should be either \"skos\" or \"text\".");
        }

        int topicsPerDocument = 10;
        String numPhrases = Utils.getOption('n', options);
        if (numPhrases.length() > 0) {
            topicsPerDocument = Integer.parseInt(numPhrases);
        }

        MauiWrapper mauiWrapper = null;
        try {
            // Use default stemmer, stopwords and language
            // MauiWrapper also can be initalized with a pre-loaded vocabulary
            // and a pre-loaded MauiFilter (model) objects
            mauiWrapper = new MauiWrapper(modelName, vocabularyName, "skos");

            // the last three items should match what was used in the wrapper
            // constructor
            // i.e. null if the defaults were used
            mauiWrapper.setModelParameters(vocabularyName, new PorterStemmer(), null, null);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        try {

            ArrayList<Topic> keywords = mauiWrapper.extractTopicsFromText(inputString, topicsPerDocument);
            for (Topic keyword : keywords) {
                System.out.println("Keyword: " + keyword.getTitle() + " " + keyword.getProbability());
            }
        } catch (MauiFilterException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String[] getTrainingArgs() {

        String[] args = new String[9];
        args[0] = "train";
        args[1] = "-l";
        args[2] = "data/maui/SemEval/SemEval2010-Maui/maui-semeval2010-train/";
        args[3] = "-m";
        args[4] = "data/maui/model/maui-semeval2010-train.data";
        args[5] = "-v";
        args[6] = "none";
        args[7] = "-o";
        args[8] = "2";
        return args;
    }

    public static String[] getRunArgs() {

        String[] args = new String[6];
        args[0] = "run";
        args[1] = "data/maui/wiki20/documents/287.txt";
        args[2] = "-m";
        args[3] = "data/maui/model/maui-semeval2010-train.data";
        args[4] = "-v";
        args[5] = "none";
        // args[6] = "-f";
        // args[7] = "skos";
        return args;
    }

    public static void main(String[] args) throws Exception {

        args = getRunArgs();

        String command = args[0].toLowerCase();

        if (args.length == 0 || (!command.equals("train") && !command.equals("test") && !command.equals("run"))) {
            System.out.printf("Maui Standalone Runner\n"
                    + "java -jar standalone.jar [train|test|run] options...\n"
                    + "Please specify train or test and then the appropriate parameters.\n");

            System.exit(-1);
        }

        String[] remainingArgs = new String[args.length - 1];

        System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);

        if (command.equals("train")) {
            MauiModelBuilder.main(remainingArgs);
        } else if (command.equals("test")) {
            MauiTopicExtractor.main(remainingArgs);
        } else {
            runMaui(remainingArgs);
        }
    }
}
