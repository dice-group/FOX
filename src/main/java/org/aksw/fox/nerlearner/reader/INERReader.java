package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.util.Map;

public interface INERReader {

  public void initFiles(String[] initFiles) throws IOException;

  public String getInput();

  public Map<String, String> getEntities();
}
