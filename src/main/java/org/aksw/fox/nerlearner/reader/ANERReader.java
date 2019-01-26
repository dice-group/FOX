package org.aksw.fox.nerlearner.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;

public abstract class ANERReader implements INERReader {
  static {
    PropertiesLoader.setPropertiesFile("fox.properties");
  }

  protected File[] inputFiles;
  final static String key_maxsentences = ".maxSentences";

  public static final String CFG_KEY_MAX_SENTENCES =
      INERReader.class.getName().concat(key_maxsentences);

  public static final int maxSentences =
      Integer.valueOf(PropertiesLoader.get(CFG_KEY_MAX_SENTENCES));

  /**
   *
   * @param inputPaths
   * @throws IOException
   */
  public ANERReader(final String[] inputPaths) throws IOException {
    initFiles(inputPaths);
  }

  /**
   * Empty constructor to create class with Reflections.
   */
  public ANERReader() {}

  /**
   * String to File.
   */
  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    inputFiles = new File[initFiles.length];
    for (int i = 0; i < initFiles.length; i++) {
      inputFiles[i] = new File(initFiles[i]);
      if (!inputFiles[i].exists()) {
        throw new FileNotFoundException(initFiles[i]);
      }
    }
  }
}
