package org.aksw.fox.nerlearner.classifier;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;

/**
 * It's a weka Classifier and a wrapper class for all NER classifiers.
 *
 * @author rspeck
 *
 */
public class ResultVoteClassifier extends Classifier {

  private static final long serialVersionUID = -4351327405377856444L;
  public final static Logger LOG = LogManager.getLogger(ResultVoteClassifier.class);

  // NER tool names
  protected String attributePrefix = "";

  public ResultVoteClassifier(final String attributePrefix) {
    this.attributePrefix = attributePrefix;
  }

  @Override
  public void buildClassifier(final Instances instances) throws Exception {

    getCapabilities().testWithFail(instances);

    LOG.info("buildClassifier ...");
  }

  /**
   * Returns the classification value for an instance. Only instance attributes with a specific
   * prefix are used({@link #attributePrefix} ).
   */
  @Override
  public double classifyInstance(final Instance instance) {

    int cl = -1;
    for (int i = 0; i < instance.numValues() - 1; i++) {

      if (instance.value(i) > 0) {
        final String name = instance.attribute(i).name();

        if (name.startsWith(attributePrefix)) {
          final String classname = instance.attribute(i).name().replace(attributePrefix, "");

          cl = instance.classAttribute().indexOfValue(classname);
          if (cl == -1) {
            LOG.info("name: " + name);
          }
          break;
        }
      }
    }

    if (cl != -1) {
      return Double.valueOf(cl);
    } else {
      throw new ArrayIndexOutOfBoundsException(
          "Attribute prefix \"" + attributePrefix + "\" not found");
    }
  }

  /**
   *
   */
  @Override
  public Capabilities getCapabilities() {
    final Capabilities result = super.getCapabilities();
    result.disableAll();

    // attributes
    result.enable(Capability.NUMERIC_ATTRIBUTES);

    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);

    // instances
    result.setMinimumNumberInstances(0);

    return result;
  }
}
