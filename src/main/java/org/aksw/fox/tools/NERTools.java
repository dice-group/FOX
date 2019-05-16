package org.aksw.fox.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.aksw.fox.data.Entity;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.tools.ner.INER;
import org.aksw.fox.ui.FoxCLI;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

/**
 *
 * Runs tools and uses this results as input for the {@link FoxClassifier}.
 *
 * @author rspeck
 *
 */
public class NERTools {

  public static final Logger LOG = LogManager.getLogger(NERTools.class);

  public final static String CFG_KEY_LIFETIME = NERTools.class.getName().concat(".lifeTime");

  /*
   * Contains all tools to be used to retrieve entities.
   */
  protected List<INER> tools = new ArrayList<>();
  /*
   * Contains all entities.
   */
  protected Map<String, List<Entity>> toolResults = new HashMap<>();

  /*
   * ML model.
   */
  protected FoxClassifier foxClassifier = new FoxClassifier();

  private boolean doTraining = false;
  private final String lang;

  /**
   * Initializes {@link #tools} and fills {@link #toolResults} with the tool names.
   *
   * @throws LoadingNotPossibleException
   */
  public NERTools(final List<String> toolsList, final String lang) {
    LOG.info("NERTools loading ...");
    LOG.info("NERTools list" + toolsList);

    this.lang = lang;

    // init tools
    if (toolsList != null) {
      for (final String cl : toolsList) {
        try {
          tools.add((INER) PropertiesLoader.getClass(cl));
        } catch (final IOException e) {
          LOG.warn("Could not load " + cl);
        }
      }
    }

    // init toolResults
    tools.forEach(tool -> toolResults.put(tool.getToolName(), null));

    LOG.info("NERTools loading done.");
  }

  /**
   * Starts a thread for each tool to request entities.
   *
   * @param input
   * @return entities
   */
  public List<Entity> getEntities(final String input) {
    LOG.info("get entities ...");

    List<Entity> results = null;

    // use all tools to retrieve entities
    // start all threads
    final List<Fiber> fibers = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(tools.size());
    for (final INER tool : tools) {

      tool.setCountDownLatch(latch);
      tool.setInput(input);

      final Fiber fiber = new ThreadFiber();
      if (fibers.add(fiber)) {
        fiber.start();
        fiber.execute(tool);
      }
    }

    // wait till finished
    final int min = Integer.parseInt(PropertiesLoader.get(CFG_KEY_LIFETIME));
    try {
      latch.await(min, TimeUnit.MINUTES);
    } catch (final InterruptedException e) {
      LOG.error("Timeout after " + min + "min.");
      LOG.error("\n", e);
      LOG.error("input:\n" + input);
    }

    // shutdown threads
    fibers.forEach(Fiber::dispose);

    // get results
    if (latch.getCount() == 0) {

      for (final INER tool : tools) {
        toolResults.put(tool.getToolName(), tool.getResults());
      }

    } else {
      // TODO: handle timeout
      LOG.debug("timeout after " + min + "min.");
    }

    if (!doTraining) {
      final String classifier = FoxCLI.getName(lang);
      LOG.info("Reads classifier " + classifier + "...");
      foxClassifier.readClassifier(classifier);
      results = foxClassifier.classify(input, toolResults);
    }
    LOG.info("get entities done.");
    return results;
  }

  /**
   *
   * @return tools
   */
  public List<INER> getNerTools() {
    return tools;
  }

  /**
   *
   * @return results
   */
  public Map<String, List<Entity>> getToolResult() {
    return toolResults;
  }

  /**
   *
   * @param doTraining
   */
  public void setTraining(final boolean doTraining) {
    this.doTraining = doTraining;
  }
}
