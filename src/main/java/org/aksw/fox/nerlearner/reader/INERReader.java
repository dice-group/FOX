package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.util.Map;

import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface INERReader {

    public static final String CFG_KEY_MAX_SENTENCES = INERReader.class.getName().concat(".maxSentences");
    public static final Logger LOG                   = LogManager.getLogger(INERReader.class);
    public static final int    maxSentences          = Integer.valueOf(FoxCfg.get(CFG_KEY_MAX_SENTENCES));

    public void initFiles(String[] initFiles) throws IOException;

    public String getInput();

    public Map<String, String> getEntities();
}
