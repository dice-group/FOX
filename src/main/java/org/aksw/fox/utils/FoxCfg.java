package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class FoxCfg {

    public static Logger logger = Logger.getLogger(FoxCfg.class);;
    public static Properties FoxProperties = new Properties();
    public static String CFG_FILE = "fox.properties";

    // loads CFG_FILE to FoxProperties
    static {

        logger.info("Loads cfg ...");
        FileInputStream in = null;
        try {
            in = new FileInputStream(CFG_FILE);
        } catch (FileNotFoundException e) {
            logger.error("file: " + CFG_FILE + " not found!");
        }
        try {
            if (in != null)
                FoxProperties.load(in);
        } catch (IOException e) {
            logger.error("can't read cfg file.");
        }
        try {
            in.close();
        } catch (Exception e) {
            logger.error("something went wrong.\n", e);
        }
    }

    public static String get(String key) {
        return FoxProperties.getProperty(key);
    }
}
