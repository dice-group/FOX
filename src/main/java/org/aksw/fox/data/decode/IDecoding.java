package org.aksw.fox.data.decode;

import java.util.List;

import org.aksw.fox.data.Entity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Interface for decoding algorithms to decode encoded instances of {@link Entity}, e.g.,
 * {@link GreedyLeftToRight}.
 *
 * @author rspeck
 *
 */
public interface IDecoding {

  Logger LOG = LogManager.getLogger(IDecoding.class);

  /**
   * Decodes the given entities to a specific coding.
   *
   * @param encoded entities
   * @return decoded entities
   */
  List<Entity> decode(final List<Entity> entities);
}
