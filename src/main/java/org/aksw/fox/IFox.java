package org.aksw.fox;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * FOX interface that extends {@link java.lang.Runnable}.
 * 
 * @author rspeck
 * 
 */
public interface IFox extends Runnable {

    /**
     * Sets an optional {@link java.util.concurrent.CountDownLatch} object. FOX
     * counts it down {@link java.util.concurrent.CountDownLatch#countDown()}
     * when the thread is finished.
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

    /**
     * Returns lang.
     * 
     * @return lang
     */
    public String getLang();

    /**
     * Returns log messages.
     * 
     * @return log
     */
    public String getLog();
}
