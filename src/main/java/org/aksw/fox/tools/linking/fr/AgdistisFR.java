package org.aksw.fox.tools.linking.fr;

import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

/**
 * This class uses the Agdistis web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisFR extends Agdistis {
  public AgdistisFR() {
    super(CfgManager.getCfg(AgdistisFR.class));
  }
}
