package org.aksw.fox.tools.ner.linking.en;

import org.aksw.fox.tools.ner.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;

public class AgdistisEN extends Agdistis {
    public static final XMLConfiguration CFG = CfgManager.getCfg(AgdistisEN.class);

    public AgdistisEN() {
        super(CFG.getString(CFG_KEY_AGDISTIS_ENDPOINT));
    }
}
