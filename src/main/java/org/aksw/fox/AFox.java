package org.aksw.fox;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.tools.ATool;

/**
 * An implementation of {@link org.aksw.fox.IFox}.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
abstract class AFox extends ATool implements IFox {

  protected String lang = null;
  protected CountDownLatch countDownLatch = null;
  protected Map<String, String> parameter = null;

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
}
