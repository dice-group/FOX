package org.aksw.fox.tools.linking.nl;

import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

/**
 * This class uses the Agdistis web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisNL extends Agdistis {
  public AgdistisNL() {
    super(CfgManager.getCfg(AgdistisNL.class));
  }
}
