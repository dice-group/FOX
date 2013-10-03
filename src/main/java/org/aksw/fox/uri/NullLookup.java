package org.aksw.fox.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.aksw.fox.data.Entity;

public class NullLookup implements InterfaceURI {

    @Override
    public void setUris(Set<Entity> entities, String input) {

        for (Entity entity : entities) {
            URI uri;
            try {
                uri = new URI(
                        "http",
                        "scms.eu",
                        "/" + entity.getText().replaceAll(" ", "_"),
                        null);
                entity.uri = uri.toASCIIString();
            } catch (URISyntaxException e1) {

            }
        }
    }
}