package org.aksw.fox.utils;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CfgManager {
  public final static String CFG_FOLDER = "data/fox/cfg";
  public static final Logger LOG = LogManager.getLogger(CfgManager.class);

  /**
   *
   * @param className
   * @return
   */
  public static XMLConfiguration getCfg(final String className) {

    final String file = CFG_FOLDER + File.separator + className + ".xml";

    if (FileUtil.fileExists(file)) {

      try {
        return new XMLConfiguration(file);
      } catch (final ConfigurationException e) {
        LOG.error("Error while reading " + file, e);
      }
    } else {
      LOG.warn("Could not find " + file);
    }
    return null;
  }

  /**
   *
   * @param className
   * @return
   */
  public static XMLConfiguration getCfg(final Class<?> classs) {
    return CfgManager.getCfg(classs.getName());
  }
}
