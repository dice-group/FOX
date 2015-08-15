package org.aksw.fox.tools.ner.linking.en;

import org.aksw.fox.tools.ner.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;

public class AgdistisEN extends Agdistis {

    public AgdistisEN() {
        super(CfgManager.getCfg(AgdistisEN.class));
    }
}
