package org.aksw.fox;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * An interface for the use of FOX.
 * 
 * @author rspeck
 * 
 */
public interface InterfaceRunnableFox extends Runnable {

    /**
     * Sets a CountDownLatch object.
     * 
     * @param cdl
     */
    public void setCountDownLatch(CountDownLatch cdl);

    /**
     * Sets the parameter Map object.
     * 
     * @param parameter
     */
    public void setParameter(Map<String, String> parameter);

    /**
     * Returns default Parameter.
     * 
     * @return defaults
     */
    public Map<String, String> getDefaultParameter();

    /**
     * Returns results.
     * 
     * @return results
     */
    public String getResults();
}
