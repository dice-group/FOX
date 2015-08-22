package org.aksw.fox.tools.re;

import java.util.HashMap;
import java.util.Map;

import org.aksw.fox.tools.re.en.REStanford;

public class FoxRETools {

    protected Map<String, IRE> relationTool = new HashMap<>();

    public FoxRETools() {
        // TODO: move RE to config
        relationTool.put("en", new REStanford());
    }

    public IRE getRETool(String lang) {
        return relationTool.get(lang);
    }
}
