package org.aksw.fox.tools.linking.de;

import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

/**
 * This class uses the Agdistis web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisDE extends Agdistis {
  public AgdistisDE() {
    super(CfgManager.getCfg(AgdistisDE.class));
  }
}
