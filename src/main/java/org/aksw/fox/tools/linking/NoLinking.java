package org.aksw.fox.tools.linking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Voc;

public class NoLinking extends AbstractLinking {

  @Override
  public void setUris(final List<Entity> entities, final String input) {
    for (final Entity entity : entities) {
      try {
        entity.setUri(new URI(//
            Voc.akswNotInWiki + entity.getText().replaceAll(" ", "_")//
        ).toASCIIString());
      } catch (final URISyntaxException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    this.entities = entities;
  }
}
