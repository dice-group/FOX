package org.aksw.fox.tools.re;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Relation;

public abstract class AbstractRE implements IRE {

    protected Set<Relation>  relations = null;
    protected CountDownLatch cdl       = null;
    protected String         input     = null;

    @Override
    public void run() {
        extract(input);
        if (cdl != null)
            cdl.countDown();
    }

    @Override
    public void setCountDownLatch(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    @Override
    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public Set<Relation> getResults() {
        return relations;
    }
}
