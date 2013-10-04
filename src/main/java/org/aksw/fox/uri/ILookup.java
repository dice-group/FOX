package org.aksw.fox.uri;

import java.util.Set;

import org.aksw.fox.data.Entity;

public interface ILookup {

    public void setUris(Set<Entity> entities, String input);

}