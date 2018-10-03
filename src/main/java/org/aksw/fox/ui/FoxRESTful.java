package org.aksw.fox.ui;

import java.io.IOException;

import org.aksw.fox.webservice.FoxServer;
import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
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
   * @throws IOException
   *
   * @throws PortInUseException
   */
  public static void main(final String[] args) throws IOException {


    PropertiesLoader.setPropertiesFile("fox.properties");
    CfgManager.cfgFolder = "data/fox/cfg";

    final FoxServer server = new FoxServer();
    try {
      server.start();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    LOG.info("Fox web service ready.");
  }
}
