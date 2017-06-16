package org.aksw.fox.tools.ner.linking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.aksw.fox.data.Entity;

public class NoLinking extends AbstractLinking {

  @Override
  public void setUris(final Set<Entity> entities, final String input) {
    // TODO: update me
    for (final Entity entity : entities) {
      URI uri;
      try {
        uri = new URI("http", "scms.eu", "/" + entity.getText().replaceAll(" ", "_"), null);
        entity.uri = uri.toASCIIString();
      } catch (final URISyntaxException e1) {

      }
    }
    this.entities = entities;
  }

}
