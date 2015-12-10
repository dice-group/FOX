package org.aksw.fox.tools.ke;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.aksw.fox.data.Keyword;
import org.aksw.fox.tools.ner.AbstractKE;
import org.aksw.fox.tools.ner.IKE;
import org.aksw.fox.utils.FoxCfg;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;

import weka.core.Utils;

import com.entopix.maui.filters.MauiFilter.MauiFilterException;
import com.entopix.maui.main.MauiWrapper;
import com.entopix.maui.stemmers.PorterStemmer;
import com.entopix.maui.stopwords.Stopwords;
import com.entopix.maui.stopwords.StopwordsEnglish;
import com.entopix.maui.util.Topic;

public class Maui extends AbstractKE {
    static {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
    }

    @Override
    public Set<Keyword> retrieve(String input) {
        String[] args = new String[6];
        args[0] = "run";
        args[1] = input;
        args[2] = "-m";
        args[3] = "data/maui/model/maui-semeval2010-train.data";
        args[4] = "-v";
        args[5] = "none";

        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);

        Set<Keyword> keywords = new HashSet<>();
        try {
            keywords.addAll(runMaui(remainingArgs));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        // keywords.forEach(LOG::info);
        return keywords;
    }

    public Set<Keyword> runMaui(String[] options) throws Exception {

        String inputString = options[0];
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
            Stopwords stopwords = new StopwordsEnglish();

            mauiWrapper.setModelParameters(vocabularyName, new PorterStemmer(), stopwords, "en");

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        Set<Keyword> keywords = new HashSet<Keyword>();
        try {
            for (Topic keyword : mauiWrapper.extractTopicsFromText(inputString, topicsPerDocument)) {
                keywords.add(new Keyword(keyword.getTitle(), Double.valueOf(keyword.getProbability()).floatValue(), getToolName()));
            }
        } catch (MauiFilterException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return keywords;
    }

    public String[] getTrainingArgs() {

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

    public String[] getRunArgs() {

        String[] args = new String[6];
        args[0] = "run";
        args[1] = "Leipzig has been a trade city since at least the time of the Holy Roman Empire. The city sits at the intersection of the Via Regia and Via Imperii, two important Medieval trade routes. Leipzig was once one of the major European centers of learning and culture in fields such as music and publishing. Leipzig became a major urban center within the German Democratic Republic (East Germany) after World War II, but its cultural and economic importance declined despite East Germany being the richest economy in the Soviet Bloc.";
        args[2] = "-m";
        args[3] = "data/maui/model/maui-semeval2010-train.data";
        args[4] = "-v";
        args[5] = "none";
        // args[6] = "-f";
        // args[7] = "skos";
        return args;
    }

    public static void main(String[] args) throws Exception {
        {

            String input = "" +
                    "Leipzig has been a trade city since at least the time of the Holy Roman Empire. "
                    + "The city sits at the intersection of the Via Regia and Via Imperii, "
                    + "two important Medieval trade routes. "
                    + "Leipzig was once one of the major European centers of learning and culture in "
                    + "fields such as music and publishing. Leipzig became a major urban center within "
                    + "the German Democratic Republic (East Germany) after World War II, but its cultural "
                    + "and economic importance declined despite East Germany being the richest economy in the Soviet Bloc.";
            IKE m = new Maui();
            Set<Keyword> keywords = m.retrieve(input);
            keywords.forEach(LOG::info);
        }
        /*
        {
            Maui m = new Maui();
            args = m.getRunArgs();

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
                Set<Keyword> keywords = m.runMaui(remainingArgs);

                keywords.forEach(LOG::info);
            }
        }
        */
    }
}
