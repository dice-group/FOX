package org.aksw.fox.ui;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;

public abstract class AUI {
  static {
    PropertiesLoader.setPropertiesFile("fox.properties");
  }
}
