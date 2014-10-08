package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Gets FOX properties stored in the {@link #CFG_FILE} file.
 * 
 * @author rspeck
 * 
 */
public class FoxCfg {

    public static final String  parameter_input    = "input";
    public static final String  parameter_task     = "task";
    public static final String  parameter_output   = "output";
    public static final String  parameter_foxlight = "foxlight";
    public static final String  parameter_nif      = "nif";
    public static final String  parameter_type     = "type";

    public static final Logger  LOG                = LogManager.getLogger(FoxCfg.class); ;

    public static final String  LOG_FILE           = "log4j.properties";
    public static final String  CFG_FILE           = "fox.properties";
    protected static Properties FoxProperties      = new Properties();

    // loads CFG_FILE to FoxProperties
    static {
        LOG.info("Loads cfg ...");

        FileInputStream in = null;
        try {
            in = new FileInputStream(CFG_FILE);
        } catch (FileNotFoundException e) {
            LOG.error("file: " + CFG_FILE + " not found!");
        }
        if (in != null) {
            try {
                FoxProperties.load(in);
            } catch (IOException e) {
                LOG.error("Can't read `" + CFG_FILE + "` file.");
            }
            try {
                in.close();
            } catch (Exception e) {
                LOG.error("Something went wrong.\n", e);
            }
        } else {
            LOG.error("Can't read `" + CFG_FILE + "` file.");
        }
    }

    public static String get(String key) {
        return FoxProperties.getProperty(key);
    }

    public synchronized static Object getClass(String classPath) {
        LOG.info("Load class: " + classPath);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(classPath.trim());
            if (clazz != null) {
                Constructor<?> constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
        } catch (ClassNotFoundException e) {
            LOG.error("\n", e);
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.error("\n", e);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("\n", e);
        }
        return null;
    }
}
