package org.aksw.fox;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.utils.FoxWebLog;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * An implementation of {@link org.aksw.fox.IFox}.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
abstract class AFox implements IFox {

  public static final Logger LOG = LogManager.getLogger(AFox.class);

  protected String lang = null;
  protected FoxWebLog foxWebLog = new FoxWebLog();
  protected CountDownLatch countDownLatch = null;
  protected Map<String, String> parameter = null;

  @Override
  public void run() {
    foxWebLog = new FoxWebLog();
  }

  @Override
  public void setCountDownLatch(final CountDownLatch cdl) {
    countDownLatch = cdl;
  }

  @Override
  public void setParameter(final Map<String, String> parameter) {
    this.parameter = parameter;
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public String getLog() {
    return foxWebLog.getConsoleOutput();
  }

  protected void infoLog(final String m) {
    if (foxWebLog != null) {
      foxWebLog.setMessage(m);
    }
    LOG.info(m);
  }
}
