package org.aksw.fox.tools.linking.es;

import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

/**
 * This class uses the Agdistis web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisES extends Agdistis {
  public AgdistisES() {
    super(CfgManager.getCfg(AgdistisES.class));
  }
}
