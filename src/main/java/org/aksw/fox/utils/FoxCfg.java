package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Gets FOX properties stored in the {@link #CFG_FILE} file.
 * 
 * @author rspeck
 * 
 */
public class FoxCfg {

    public static Logger logger = Logger.getLogger(FoxCfg.class);;
    protected static Properties FoxProperties = new Properties();
    public static final String CFG_FILE = "fox.properties";

    // loads CFG_FILE to FoxProperties
    static {
        logger.info("Loads cfg ...");

        FileInputStream in = null;
        try {
            in = new FileInputStream(CFG_FILE);
        } catch (FileNotFoundException e) {
            logger.error("file: " + CFG_FILE + " not found!");
        }
        if (in != null) {
            try {
                FoxProperties.load(in);
            } catch (IOException e) {
                logger.error("Can't read `fox.properties` file.");
            }
            try {
                in.close();
            } catch (Exception e) {
                logger.error("Something went wrong.\n", e);
            }
        } else {
            logger.error("Can't read `fox.properties` file.");
        }
    }

    public static String get(String key) {
        return FoxProperties.getProperty(key);
    }

    public synchronized static Object getClass(String classPath) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classPath.trim());
            if (clazz != null) {
                Constructor<?> constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
        } catch (ClassNotFoundException e) {
            logger.error("\n", e);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("\n", e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("\n", e);
        }
        return null;
    }
}
