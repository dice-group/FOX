package org.aksw.fox.ui;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AUI {
  static {
    PropertiesLoader.setPropertiesFile("fox.properties");
  }
  public static Logger LOG = LogManager.getLogger(AUI.class);
}
