package org.aksw.fox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.aksw.fox.exception.LoadingNotPossibleException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Gets FOX properties stored in the {@link #CFG_FILE} file.
 *
 * @author rspeck
 *
 */
public class FoxCfg {

  public static final Logger LOG = LogManager.getLogger(FoxCfg.class);;

  public static final String CFG_FILE = "fox.properties";
  protected static Properties foxProperties = null;

  /**
   * Loads a given file to use as properties.
   *
   * @param cfgFile properties file
   */
  public static boolean loadFile(final String cfgFile) {
    boolean loaded = false;
    LOG.info("Loads cfg ...");

    foxProperties = new Properties();
    FileInputStream in = null;
    try {
      in = new FileInputStream(cfgFile);
    } catch (final FileNotFoundException e) {
      LOG.error("file: " + cfgFile + " not found!");
    }
    if (in != null) {
      try {
        foxProperties.load(in);
        loaded = true;
      } catch (final IOException e) {
        LOG.error("Can't read `" + cfgFile + "` file.");
      }
      try {
        in.close();
      } catch (final Exception e) {
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
   * @param key property key
   * @return property value
   */
  public static String get(final String key) {
    try {
      if (foxProperties == null) {
        loadFile(CFG_FILE);
      }

      return foxProperties.getProperty(key).trim();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      LOG.info("given key is: " + key);
    }
    return null;
  }

  /**
   * Gets an object of the given class.
   *
   * @param classPath path to class
   * @return object of a class
   * @throws LoadingNotPossibleException
   */
  public synchronized static Object getClass(final String classPath)
      throws LoadingNotPossibleException {
    LOG.info("Loading class: " + classPath);

    Class<?> clazz = null;
    try {
      clazz = Class.forName(classPath.trim());
      final Constructor<?> constructor = clazz.getConstructor();
      return constructor.newInstance();

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new LoadingNotPossibleException("Could not load class: " + classPath);
    }
  }
}
