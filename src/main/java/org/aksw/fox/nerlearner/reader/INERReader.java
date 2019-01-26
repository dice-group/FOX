package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface INERReader {

  Logger LOG = LogManager.getLogger(INERReader.class);

  void initFiles(String[] initFiles) throws IOException;

  String getInput();

  Map<String, String> getEntities();
}
