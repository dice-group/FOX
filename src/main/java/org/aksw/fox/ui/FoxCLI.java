package org.aksw.fox.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.FoxClassifierFactory;
import org.aksw.fox.nerlearner.reader.INERReader;
import org.aksw.fox.nerlearner.reader.NERReaderFactory;
import org.aksw.fox.tools.NERTools;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;

import gnu.getopt.Getopt;
import weka.classifiers.Classifier;

/**
 * Main class for cli support.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxCLI extends AUI {

  /**
   *
   * @param args
   *        <p>
   *        -i for an input file or all files in a given directory (sub directories aren't
   *        included),<br>
   *        -a for an action {train | validate}
   *        </p>
   * @throws IOException
   *
   */
  public static void main(final String[] args) throws IOException {
    // Example
    // String[] args = new String[] {"-l", "en", "-i", "input/2", "-a", "train"};
    // FoxCLI.main(args);

    LOG.info("Fox cl service starting ...");
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
            if (PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase()
                .startsWith("false")) {
              throw new UnsupportedOperationException(
                  "You need to change the fox.properties file and set "
                      + FoxClassifier.CFG_KEY_LEARNER_TRAINING + " to true. "
                      + "Also you should set an output file for the new model.");
            }
            /*
             * } else if (a.toLowerCase().startsWith("re")) { a = "retrieve"; if
             * (PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase().
             * startsWith("tr")) { throw new
             * Exception("You need to change the fox.properties file and set " +
             * FoxClassifier.CFG_KEY_LEARNER_TRAINING +
             * " to false. Also you should set file for a trained model."); }
             */
          } else if (a.toLowerCase().startsWith("va")) {
            a = "validate";
            if (PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER_TRAINING).toLowerCase()
                .startsWith("true")) {
              throw new UnsupportedOperationException(
                  "You need to change the fox.properties file and set "
                      + FoxClassifier.CFG_KEY_LEARNER_TRAINING + " to false.");
            }
          } else {
            throw new UnsupportedOperationException("Wrong action value.");
          }
          break;
      }
    }
    final List<String> files = new ArrayList<>();

    final File file = new File(in);
    if (!file.exists()) {
      throw new UnsupportedOperationException("Can't find file or directory: " + in);
    } else {
      if (file.isDirectory()) {
        // read all files in a directory
        for (final File fileEntry : file.listFiles()) {
          if (fileEntry.isFile() && !fileEntry.isHidden()) {
            files.add(fileEntry.getAbsolutePath());
          }
        }
      } else if (file.isFile()) {
        files.add(file.getAbsolutePath());
      } else {
        throw new UnsupportedOperationException("Input isn't a valid file or directory.");
      }
    }

    LOG.info(files.toString());
    final String[] files_array = files.toArray(new String[files.size()]);

    switch (a) {
      case "train": {
        FoxCLI.training(files_array, l);
        break;
      }
      /*
       * case "retrieve": { MainFox.retrieve(files_array); break; }
       */
      case "validate": {
        FoxCLI.validate(files_array, l);
        break;
      }
      default:
        throw new UnsupportedOperationException(
            "Don't know what to do. Please set the action parameter.");
    }

    LOG.info("Fox cl service ended.");
  }

  public static void validate(final String[] inputFiles, final String lang) {
    if (lang == null || lang.isEmpty()) {
      LOG.warn("Missing lang paramerter!");
    }
    try {
      ToolsGenerator toolsGenerator;

      toolsGenerator = new ToolsGenerator();

      final CrossValidation cv = new CrossValidation(toolsGenerator.getNERTools(lang));
      final Set<String> toolResultKeySet = cv.getTools().getToolResult().keySet();
      final String[] prefix = toolResultKeySet.toArray(new String[toolResultKeySet.size()]);

      LOG.info("tools used: " + toolResultKeySet);

      // TODO: remove FoxClassifier dependency?
      final FoxClassifier foxClassifier = new FoxClassifier();
      setClassifier(foxClassifier, prefix);
      final Classifier cls = foxClassifier.getClassifier();
      cv.crossValidation(cls, inputFiles);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Retrieve entities.
   *
   * @param inputFiles files
   * @throws IOException
   */
  /*
   * public static void retrieve(String[] inputFiles) throws IOException {
   *
   * TrainingInputReader trainingInputReader = new TrainingInputReader(inputFiles); String input =
   * trainingInputReader.getInput();
   *
   * IFox fox = new Fox(); Map<String, String> para = fox.getDefaultParameter(); para.put("input",
   * input); fox.setParameter(para); fox.run(); }
   */

  /**
   * Training FoxClassifier
   *
   * @param inputFiles files
   * @throws UnsupportedLangException
   * @throws LoadingNotPossibleException
   * @throws Exception
   */
  public static void training(final String[] inputFiles, final String lang) throws IOException {
    final ToolsGenerator toolsGenerator = new ToolsGenerator();

    final NERTools foxNERTools = toolsGenerator.getNERTools(lang);

    final FoxClassifier foxClassifier = new FoxClassifier();

    final Set<String> toolNames = foxNERTools.getToolResult().keySet();
    final String[] prefix = toolNames.toArray(new String[toolNames.size()]);
    LOG.info("tools used: " + toolNames);
    setClassifier(foxClassifier, prefix);

    // read training data
    final INERReader reader = NERReaderFactory.getINERReader();
    reader.initFiles(inputFiles);

    final String input = reader.input();
    final Map<String, String> oracle = reader.getEntities();

    // retrieve entities (tool results)
    foxNERTools.setTraining(true);
    foxNERTools.getEntities(input);

    try {
      foxClassifier.training(input, foxNERTools.getToolResult(), oracle);

      final String name = getName(lang);

      foxClassifier.writeClassifier(name);
      foxClassifier.eva();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the path to the serialized classifier.
   *
   * @param lang
   * @return path to the serialized classifier
   */
  public static String getName(final String lang) {
    return PropertiesLoader.get(FoxClassifier.CFG_KEY_MODEL_PATH)//
        .concat(File.separator).concat(lang)//
        .concat(File.separator).concat(PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER));
  }

  private static void setClassifier(final FoxClassifier foxClassifier, final String[] prefix)
      throws IOException {
    LOG.debug("prefix: " + prefix);

    switch (PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER).trim()) {
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
        foxClassifier.setClassifier(
            FoxClassifierFactory.get(PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER),
                PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER_OPTIONS)));
    }
  }
}
