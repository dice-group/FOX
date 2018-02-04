package org.aksw.fox.ui;

import org.aksw.fox.exception.PortInUseException;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.webservice.FoxServer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Starts FOX web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxRESTful {
  public static Logger LOG = LogManager.getLogger(FoxRESTful.class);

  /**
   * Starts FOX web service.
   *
   * @throws PortInUseException
   */
  public static void main(final String[] args) throws PortInUseException {
    LOG.info("Fox web service starting ...");
    final FoxServer server = new FoxServer();
    if (FoxCfg.loadFile(FoxCfg.CFG_FILE)) {
      try {
        server.start();
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    LOG.info("Fox web service ready.");
  }
}
