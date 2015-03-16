package org.aksw.fox.tools.re;

import org.aksw.fox.tools.re.en.REStanford;

public class FoxRETools {

    protected IRE relationTool = null;

    public FoxRETools() {
        relationTool = new REStanford();

    }

    public IRE getRETool() {
        return relationTool;
    }
}
