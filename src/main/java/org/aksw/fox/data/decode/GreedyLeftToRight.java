package org.aksw.fox.data.decode;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.encode.BILOUEncoding;

public class GreedyLeftToRight extends ADecoding implements IDecoding {

  /**
   * Greedy left to right decoding.
   *
   * @param tokenBasedBILOU Entities have token only (mention without space), types are encoded and
   *        indices are sorted and size of 1.
   * @return decoded entities with merged mentions and indicies.
   */
  @Override
  public List<Entity> decode(final List<Entity> tokenBasedBILOU) {

    if (!isTokenbasedBILOU(tokenBasedBILOU)) {
      throw new IllegalStateException("Not BILOU-encoded!");
    }

    final List<Entity> decoded = new ArrayList<>();

    String startType = BILOUEncoding.O;
    int index = -1;
    String text = "";

    for (final Entity entity : tokenBasedBILOU) {
      if (BILOUEncodingToEntityTypes.isUnit(entity.getType())) {

        text = entity.getText();
        startType = BILOUEncodingToEntityTypes.toEntiyType(entity.getType());
        index = entity.getBeginIndex();

        decoded.add(entity(text, startType, index));

        text = "";
        index = -1;
        startType = BILOUEncoding.O;

      } else if (BILOUEncodingToEntityTypes.isBegin(entity.getType())) {
        text = entity.getText();
        startType = BILOUEncodingToEntityTypes.toEntiyType(entity.getType());
        index = entity.getBeginIndex();
      } else if (BILOUEncodingToEntityTypes.isInside(entity.getType())) {
        text += " " + entity.getText();
      } else if (BILOUEncodingToEntityTypes.isLast(entity.getType())) {
        decoded.add(entity(text + " " + entity.getText(), startType, index));

        text = "";
        index = -1;
        startType = BILOUEncoding.O;
      }
    }
    return decoded;
  }

  private Entity entity(final String text, final String startType, final int index) {
    return new Entity(text, startType, Entity.DEFAULT_RELEVANCE, "fox", index);
  }
}
