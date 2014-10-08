package org.aksw.fox.nerlearner.reader;

import java.util.Map;

public interface INERReader {

    public String getInput();

    public Map<String, String> getEntities();
}
