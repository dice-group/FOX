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

    // NER tool names
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
        int cl = -1;
        for (int i = 0; i < instance.numValues() - 1; i++) {
            if (instance.value(i) > 0)
                if (instance.attribute(i).name().startsWith(attributePrefix)) {
                    String classs = instance.attribute(i).name().replace(attributePrefix, "");
                    cl = instance.classAttribute().indexOfValue(classs);
                    break;
                }
        }

        if (cl != -1)
            return Double.valueOf(cl);
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
        result.enable(Capability.NUMERIC_ATTRIBUTES);

        // class
        result.enable(Capability.NOMINAL_CLASS);
        result.enable(Capability.MISSING_CLASS_VALUES);

        // instances
        result.setMinimumNumberInstances(0);

        return result;
    }
}