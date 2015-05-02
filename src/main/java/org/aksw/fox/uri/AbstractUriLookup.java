package org.aksw.fox.uri;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aksw.fox.data.Entity;

public abstract class AbstractUriLookup implements ILookup {

    protected Set<Entity>    entities = null;
    protected CountDownLatch cdl      = null;
    protected String         input    = null;

    @Override
    public void run() {
        setUris(entities, input);
        if (cdl != null)
            cdl.countDown();
    }

    @Override
    public void setCountDownLatch(CountDownLatch cdl) {
        this.cdl = cdl;

    }

    @Override
    public void setInput(Set<Entity> entities, String input) {
        this.input = input;
        this.entities = entities;
    }

    @Override
    public Set<Entity> getResults() {
        return entities;
    }
}
