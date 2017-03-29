package org.aksw.fox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.utils.FoxConst;
import org.aksw.fox.utils.FoxWebLog;
import org.apache.jena.riot.Lang;
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
  protected String response = null;
  protected FoxWebLog foxWebLog = null;
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
  public String getResults() {
    return response;
  }

  @Override
  public Map<String, String> getDefaultParameter() {
    final Map<String, String> map = new HashMap<>();
    map.put(FoxParameter.Parameter.INPUT.toString(), FoxConst.NER_EN_EXAMPLE_1);
    map.put(FoxParameter.Parameter.TASK.toString(), FoxParameter.Task.NER.toString());
    map.put(FoxParameter.Parameter.OUTPUT.toString(), Lang.RDFXML.getName());
    map.put(FoxParameter.Parameter.NIF.toString(), FoxParameter.NIF.OFF.toString());
    map.put(FoxParameter.Parameter.FOXLIGHT.toString(), FoxParameter.FoxLight.OFF.toString());
    return map;
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public String getLog() {
    return foxWebLog.getConsoleOutput();
  }
}
