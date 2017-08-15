package org.aksw.fox.tools;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

abstract public class ATool implements ITool {

  public static final Logger LOG = LogManager.getLogger(ATool.class);

  protected static Properties versions = new Properties();
  static {
    try {
      versions.load(ITool.class.getResourceAsStream("/versions.properties"));
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  protected CountDownLatch cdl = null;

  public static String getToolVersion(final String name) {
    String version = versions.getProperty(name, "n/a");
    if (version == null) {
      LOG.warn("Version not found for: " + name);
      version = "n/a";
    }
    return version;
  }

  @Override
  public String getToolVersion() {
    return ATool.getToolVersion(this.getClass().getName());
  }

  @Override
  public String getToolName() {
    return getClass().getName();
  }

  @Override
  public abstract void run();

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    this.cdl = cdl;
  }
}
