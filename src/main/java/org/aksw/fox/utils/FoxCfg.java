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

    public static String test_input1 = "The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.";
    public static String test_input2 = "Berlin is an American New Wave band. Despite its name, Berlin did not have any known major connections with Germany, but instead was formed in Los Angeles, California in 1978.";

    public static String parameter_input = "input";
    public static String parameter_task = "task";
    public static String parameter_urilookup = "urilookup";
    public static String parameter_output = "output";
    public static String parameter_foxlight = "foxlight";
    public static String parameter_nif = "nif";

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
