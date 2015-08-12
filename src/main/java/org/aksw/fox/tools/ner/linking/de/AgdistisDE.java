package org.aksw.fox.tools.ner.linking.de;

import org.aksw.fox.tools.ner.linking.common.Agdistis;
import org.aksw.fox.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;

public class AgdistisDE extends Agdistis {

    public static final XMLConfiguration CFG = CfgManager.getCfg(AgdistisDE.class);

    public AgdistisDE() {
        super(CFG.getString(CFG_KEY_AGDISTIS_ENDPOINT));
    }
}
