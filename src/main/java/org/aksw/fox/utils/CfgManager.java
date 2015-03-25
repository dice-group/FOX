package org.aksw.fox.utils;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CfgManager {
    public final static String CFG_FOLDER = "data/fox/cfg";
    public final static String CFG_FILE   = "app.xml";

    public static final Logger LOG        = LogManager.getLogger(CfgManager.class);

    /**
     * 
     * @param className
     * @return
     */
    public static XMLConfiguration getCfg(String className) {

        String file = CFG_FOLDER + File.separator + className + ".xml";
        String fileDefault = CFG_FOLDER + File.separator + CFG_FILE;

        if (FileUtil.fileExists(file))
            fileDefault = file;

        LOG.debug("load " + fileDefault);
        try {
            return new XMLConfiguration(fileDefault);
        } catch (ConfigurationException e) {
            LOG.error("\n Error while reading " + fileDefault, e);
            return null;
        }
    }

    /**
     * 
     * @param className
     * @return
     */
    public static XMLConfiguration getCfg(Class<?> classs) {
        return CfgManager.getCfg(classs.getName());
    }
}
