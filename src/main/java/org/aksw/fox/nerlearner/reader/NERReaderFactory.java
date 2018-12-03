package org.aksw.fox.nerlearner.reader;

import java.io.IOException;

import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NERReaderFactory {

  public static final Logger LOG = LogManager.getLogger(NERReaderFactory.class);
  public final static String INER_READER_KEY =
      NERReaderFactory.class.getName().concat(".readerclass");

  public static INERReader getINERReader() throws IOException {
    return getINERReader(PropertiesLoader.get(INER_READER_KEY));

  }

  public static INERReader getINERReader(final String classes) throws IOException {
    if (classes != null && !classes.trim().isEmpty()) {
      return (INERReader) PropertiesLoader.getClass(classes);
    } else {
      return null;
    }
  }
}
