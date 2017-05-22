package org.aksw.fox;

import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.Tools;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FoxUtil {
  public static final Logger LOG = LogManager.getLogger(FoxUtil.class);

  /**
   * Prints debug infos about entities for each tool and final entities in fox.
   *
   * @param entities final entities
   */
  public void infotrace(final Tools nerTools, final Set<Entity> entities) {
    if (LOG.isTraceEnabled()) {

      LOG.trace("entities:");
      for (final String toolname : nerTools.getToolResult().keySet()) {
        if (nerTools.getToolResult().get(toolname) == null) {
          return;
        }

        LOG.trace(toolname + ": " + nerTools.getToolResult().get(toolname).size());
        for (final Entity e : nerTools.getToolResult().get(toolname)) {
          LOG.trace(e);
        }
      }

      LOG.trace("fox" + ": " + entities.size());
      for (final Entity e : entities) {
        LOG.trace(e);
      }
    }
  }

}
