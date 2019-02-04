package org.aksw.fox.data.decode;

import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;

public interface IDecoding {

  Set<Entity> decode(final List<Entity> tokenBasedBILOU);
}
