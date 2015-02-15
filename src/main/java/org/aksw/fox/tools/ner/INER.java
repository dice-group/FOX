package org.aksw.fox.tools.ner;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;

/**
 * An interface for the use of a ner tool.
 * 
 * The method {@link #retrieve(String input)} can be used to get the result list
 * of {@link Entity} objects.
 * 
 * @author rspeck
 * 
 */
public interface INER extends Runnable {

    /**
     * Retrieves Entity objects from the give input String with sentences as
     * plain text.
     * 
     * @param input
     *            sentences as plain text
     * @return list entities
     */
    public List<Entity> retrieve(String input);

    /**
     * Returns the tools name.
     * 
     * @return name
     */
    public String getToolName();

    /**
     * Sets a CountDownLatch object.
     * 
     * @param cdl
     */
    public void setCountDownLatch(CountDownLatch cdl);

    /**
     * Sets the input with sentences as plain text.
     * 
     * @param input
     */
    public void setInput(String input);

    /**
     * Returns results. Uses the {@link #retrieve(String input)} method to get
     * the result list.
     * 
     * @return results
     */
    public List<Entity> getResults();
}
