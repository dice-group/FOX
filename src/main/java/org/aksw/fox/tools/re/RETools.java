package org.aksw.fox.tools.re;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.exception.LoadingNotPossibleException;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class RETools {

  public static final Logger LOG = LogManager.getLogger(RETools.class);

  /*
   * Contains all tools to be used to retrieve relations.
   */
  protected Map<String, List<IRE>> relationTool = new HashMap<>();

  /**
   *
   * Initializes {@link #relationTool} .
   *
   *
   * @throws LoadingNotPossibleException
   *
   */
  public RETools(final List<String> toolsList, final String lang) {

    LOG.info("RETools loading ...");
    LOG.info("RETools list" + toolsList);

    // init tools
    if (toolsList != null) {
      relationTool.put(lang, new ArrayList<>());
      for (final String cl : toolsList) {
        try {
          relationTool.get(lang).add(((IRE) FoxCfg.getClass(cl)));
        } catch (final LoadingNotPossibleException e) {
          LOG.warn("Could not load " + cl);
        }
      }
    }
    LOG.info("RETools loading done.");
  }

  public List<IRE> getRETool(final String lang) {
    return relationTool.get(lang);
  }
}
