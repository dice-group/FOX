package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Gets FOX properties stored in the {@link #CFG_FILE} file.
 * 
 * @author rspeck
 * 
 */
public class FoxCfg {

    public static final Logger  LOG           = LogManager.getLogger(FoxCfg.class); ;

    public static final String  LOG_FILE      = "log4j.properties";
    public static final String  CFG_FILE      = "fox.properties";
    protected static Properties foxProperties = null;

    /**
     * Loads a given file to use as properties.
     * 
     * @param cfgFile
     *            properties file
     */
    public static boolean loadFile(String cfgFile) {
        boolean loaded = false;
        LOG.info("Loads cfg ...");

        foxProperties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(cfgFile);
        } catch (FileNotFoundException e) {
            LOG.error("file: " + cfgFile + " not found!");
        }
        if (in != null) {
            try {
                foxProperties.load(in);
                loaded = true;
            } catch (IOException e) {
                LOG.error("Can't read `" + cfgFile + "` file.");
            }
            try {
                in.close();
            } catch (Exception e) {
                LOG.error("Something went wrong.\n", e);
            }
        } else {
            LOG.error("Can't read `" + cfgFile + "` file.");
        }

        return loaded;
    }

    /**
     * Gets a property.
     * 
     * @param key
     *            property key
     * @return property value
     */
    public static String get(String key) {
        if (foxProperties == null)
            loadFile(CFG_FILE);
        return foxProperties.getProperty(key);
    }

    /**
     * Gets an object of the given class.
     * 
     * @param classPath
     *            path to class
     * @return object of a class
     * @throws LoadingNotPossibleException
     */
    public synchronized static Object getClass(String classPath) throws LoadingNotPossibleException {
        LOG.info("Loading class: " + classPath);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(classPath.trim());
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();

        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            throw new LoadingNotPossibleException("Could not load class: " + classPath);
        }
    }
}
