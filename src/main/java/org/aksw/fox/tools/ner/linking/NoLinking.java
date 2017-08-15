package org.aksw.fox.tools.ner.linking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.output.AFoxJenaNew;

public class NoLinking extends AbstractLinking {

  @Override
  public void setUris(final Set<Entity> entities, final String input) {
    for (final Entity entity : entities) {
      try {
        entity.uri = new URI(//
            AFoxJenaNew.akswNotInWiki + entity.getText().replaceAll(" ", "_")//
        ).toASCIIString();
      } catch (final URISyntaxException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    this.entities = entities;
  }
}
