package org.aksw.fox.tools.re;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IRE extends Runnable {

  public Set<Relation> extract(String text, List<Entity> entities);

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
  public void setInput(String input, List<Entity> entities);

  /**
   * Returns results.
   *
   * @return results
   */
  public Set<Relation> getResults();

}
