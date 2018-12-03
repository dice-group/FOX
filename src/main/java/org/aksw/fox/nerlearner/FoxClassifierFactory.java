package org.aksw.fox.nerlearner;

import java.io.IOException;

import org.aksw.fox.nerlearner.classifier.ClassVoteClassifier;
import org.aksw.fox.nerlearner.classifier.FoxVote;
import org.aksw.fox.nerlearner.classifier.ResultVoteClassifier;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.SelectedTag;
import weka.core.Utils;

public class FoxClassifierFactory {
  public static Logger logger = Logger.getLogger(FoxClassifierFactory.class);

  /**
   * Sets ResultVote as classifier with a combination rule.
   *
   * @param prefix tool attribute prefixes
   */
  public static Classifier getClassifierResultVote(final String[] prefix) {
    return getClassifierVote("result", prefix,
        new SelectedTag(FoxVote.RESULT, FoxVote.FOXTAGS_RULES));
  }

  /**
   * Sets ClassVote as classifier with a combination rule.
   *
   * @param prefix tool attribute prefixes
   */
  public static Classifier getClassifierClassVote(final String[] prefix) {
    return getClassifierVote("class", prefix,
        new SelectedTag(FoxVote.CLASS, FoxVote.FOXTAGS_RULES));
  }

  private static Classifier getClassifierVote(final String type, final String[] prefix,
      final SelectedTag rule) {

    final Classifier[] classifier = new Classifier[prefix.length];
    for (int i = 0; i < prefix.length; i++) {
      switch (type) {
        case "class":
          classifier[i] = new ClassVoteClassifier(prefix[i]);
          break;
        case "result":
          classifier[i] = new ResultVoteClassifier(prefix[i]);
          break;
      }
    }

    final FoxVote vote = new FoxVote();
    vote.setClassifiers(classifier);
    vote.setCombinationRule(rule);

    return vote;
  }

  /**
   * Sets MultilayerPerceptron as classifier.
   *
   * @throws IOException
   *
   * @throws LoadingNotPossibleException
   */
  // public static Classifier getClassifierMultilayerPerceptron() {
  // MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
  /*
   * 'a' = (attribs + classes) / 2, 'i' = attribs, 'o' = classes , 't' = attribs + classes so
   * "a, 6", which would give you (attribs + classes) / 2 nodes in the first hidden layer and 6 in
   * the second.
   */

  // mp.setHiddenLayers(hidden);
  // return multilayerPerceptron;
  // }

  public static Classifier get(final String wekaClassifier, final String quotedOptionString)
      throws IOException {
    String[] options = null;
    if (quotedOptionString != null) {
      try {
        options = Utils.splitOptions(quotedOptionString);
      } catch (final Exception e) {
        logger.error("Unterminated string, unknown character or a parse error.");
      }
    }
    final Object object = PropertiesLoader.getClass(wekaClassifier);
    Classifier classifier = null;
    if (object != null && object instanceof Classifier) {
      try {
        classifier = (Classifier) object;
        if (options != null) {
          classifier.setOptions(options);
        }
      } catch (final Exception e) {
        logger.error("\n", e);
      }
    }
    logger.info("classifier options: " + Utils.joinOptions(classifier.getOptions()));
    return classifier;
  }
}
