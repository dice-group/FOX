package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;

public class TestBoaEN {
  public final static Logger LOG = LogManager.getLogger(TestBoaEN.class);

  protected BoaEN boaen = new BoaEN();

  @Test
  public void example() throws URISyntaxException {
    final URI uri = new URI("http://dbpedia.org/ontology/spouse");
    final String text = "Alice who married Bob.";

    final Entity s = new Entity("Alice", EntityClassMap.P);
    s.addIndicies(0);

    final Entity o = new Entity("Bob", EntityClassMap.P);
    o.addIndicies(18);

    final Set<Entity> entities = new HashSet<>();
    entities.add(s);
    entities.add(o);
    boaen.setInput(text, entities);
    final Set<Relation> relations = boaen.extract();

    Assert.assertEquals(1, relations.size());
    LOG.info(relations);

    final Relation relation = relations.iterator().next();

    Assert.assertEquals(true, relation.getRelation().contains(uri));
  }
}
