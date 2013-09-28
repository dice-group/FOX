package org.aksw.fox.uri;

import java.util.Set;

import org.aksw.fox.data.Entity;

public class NullLookup implements InterfaceURI {

    @Override
    public void setUris(Set<Entity> entities, String input) {
        for (Entity entity : entities) {
            entity.uri = "http://scms.eu/" + entity.getText().replaceAll(" ", "_");
        }
    }
}