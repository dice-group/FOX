package org.aksw.fox.tools.re;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Relation;

public interface IRE extends Runnable {

    public Set<Relation> extract(String text);

    /**
     * Sets a CountDownLatch object.
     * 
     * @param cdl
     */
    public void setCountDownLatch(CountDownLatch cdl);

    /**
     * Sets the input.
     * 
     * @param input
     */
    public void setInput(String input);

    /**
     * Returns results.
     * 
     * @return results
     */
    public Set<Relation> getResults();

}
