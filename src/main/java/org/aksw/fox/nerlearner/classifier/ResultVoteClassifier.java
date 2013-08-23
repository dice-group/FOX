package org.aksw.fox.nerlearner.classifier;

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
    public static final Logger logger = Logger.getLogger(ResultVoteClassifier.class);

    protected String attributePrefix = "";

    public ResultVoteClassifier(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    @Override
    public void buildClassifier(Instances instances) throws Exception {
        getCapabilities().testWithFail(instances);
        logger.info("buildClassifier ...");
        logger.debug(instances);
        // we wrapped a learned classifier. So nothing to do here.
    }

    /**
     * Returns the classification value for an instance. Only instance
     * attributes with a specific prefix are used({@link #attributePrefix} ).
     */
    @Override
    public double classifyInstance(Instance instance) {
        int cols = instance.numValues() - 1;
        int cl = -1;
        int toolSize = (instance.numAttributes() - 1) / instance.classAttribute().numValues();

        for (int i = 0; i < cols; i++) {

            if (instance.attribute(i).name().startsWith(attributePrefix)) {
                if (instance.value(i) > 0) {
                    cl = i % toolSize;// TODO: That is tools size, remove magic
                                      // number
                    break;
                }
            }
        }
        if (cl != -1)
            return Double.valueOf(cl + "");
        else
            throw new ArrayIndexOutOfBoundsException("Attribute prefix \"" + attributePrefix + "\" not found.");
    }

    /**
     * 
     */
    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes
        // result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        // result.enable(Capability.DATE_ATTRIBUTES);
        // result.enable(Capability.STRING_ATTRIBUTES);
        // result.enable(Capability.RELATIONAL_ATTRIBUTES);
        // result.enable(Capability.MISSING_VALUES);

        // class
        result.enable(Capability.NOMINAL_CLASS);
        // result.enable(Capability.NUMERIC_CLASS);
        // result.enable(Capability.DATE_CLASS);
        result.enable(Capability.MISSING_CLASS_VALUES);

        // instances
        result.setMinimumNumberInstances(0);

        return result;
    }
}