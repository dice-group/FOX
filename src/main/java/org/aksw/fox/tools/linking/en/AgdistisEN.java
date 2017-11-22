package org.aksw.fox.tools.linking.en;

import org.aksw.fox.tools.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

/**
 * This class uses the Agdistis web service.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AgdistisEN extends Agdistis {
  public AgdistisEN() {
    super(CfgManager.getCfg(AgdistisEN.class));
  }
}
