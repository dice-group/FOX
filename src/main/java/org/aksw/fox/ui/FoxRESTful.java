package org.aksw.fox.ui;

import java.io.IOException;

import org.aksw.fox.Fox;
import org.aksw.fox.webservice.FoxServer;
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Starts FOX web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxRESTful extends AUI {
  public static Logger LOG = LogManager.getLogger(FoxRESTful.class);

  /**
   * Starts FOX web service.
   *
   * @throws IOException
   *
   * @throws PortInUseException
   */
  public static void main(final String[] args) throws IOException {

    final CfgManager cfgManager = new CfgManager(Fox.cfgFolder);
    final FoxServer server = FoxServer.instance(cfgManager.getCfg(FoxRESTful.class));
    try {
      server.start();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    LOG.info("Fox web service ready.");
  }
}
