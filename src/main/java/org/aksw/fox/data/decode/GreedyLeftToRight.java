package org.aksw.fox.data.decode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;

public class GreedyLeftToRight extends ADecoding implements IDecoding {

  /**
   * Greedy left to right decoding.
   *
   * @param tokenBasedBILOU Entities have token only (mention without space), types are encoded and
   *        indices are sorted and size of 1.
   * @return decoded entities with merged mentions and indicies.
   */
  @Override
  public Set<Entity> decode(final List<Entity> tokenBasedBILOU) {

    if (!isTokenbasedBILOU(tokenBasedBILOU)) {
      throw new IllegalStateException("Not BILOU-encoded!");
    }

    final Set<Entity> set = new HashSet<>();

    Entity begin = null;

    // for each entity
    for (final Entity entity : tokenBasedBILOU) {
      final String type = entity.getType();

      if (begin == null) {
        if (BILOUEncodingToEntityTypes.isUnit(type)) {
          entity.setType(BILOUEncodingToEntityTypes.toEntiyType(type));
          set.add(entity);
        } else if (BILOUEncodingToEntityTypes.isBegin(type)) {
          begin = entity;
          begin.setType(BILOUEncodingToEntityTypes.toEntiyType(type));
        }
      } else {

        if (BILOUEncodingToEntityTypes.isLast(type)) {
          begin.addText(entity.getText());
          set.add(begin);
          begin = null;
        } else if (BILOUEncodingToEntityTypes.isInside(type)) {
          begin.addText(entity.getText());
        }
      }
    }
    LOG.info("Decoded entity size: " + set.size());
    return set;
  }
}
