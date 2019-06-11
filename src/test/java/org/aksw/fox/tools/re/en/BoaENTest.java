package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.Relation;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class BoaENTest {
  public final static Logger LOG = LogManager.getLogger(BoaENTest.class);

  protected BoaEN boaen = new BoaEN();

  private Entity newEntity(final String text, final String type, final int index) {
    return new Entity(text, type, 0, "tool", index);
  }

  @Test
  public void test() throws URISyntaxException {

    final String bob = "Bob Right";
    final String alice = "Alice Right";

    final String sentenceA = bob.concat(" married ").concat(alice).concat(".");

    final String karl = "Karl Mueller";
    final String leipzig = "Leipzig";

    final String sentenceB = karl.concat(" was born in ").concat(leipzig).concat(".");

    final String text = sentenceA.concat(" ").concat(sentenceB);

    final List<Entity> entities = new ArrayList<>();

    final Entity eBob = newEntity(bob, EntityTypes.P, text.indexOf(bob));
    final Entity eAlice = newEntity(alice, EntityTypes.P, text.indexOf(alice));

    final Entity eKarl = newEntity(karl, EntityTypes.P, text.indexOf(karl));
    final Entity eLeipzig = newEntity(leipzig, EntityTypes.L, text.indexOf(leipzig));

    entities.add(eBob);
    entities.add(eAlice);
    entities.add(eKarl);
    entities.add(eLeipzig);
    boaen.setInput(text, entities);
    final Set<Relation> relations = boaen.extract();

    int found = 0;
    for (final Relation relation : relations) {
      final boolean a = relation.getSubjectEntity().equals(eBob);
      final boolean b = relation.getObjectEntity().equals(eAlice);
      final boolean c = relation.getRelation().contains(//
          new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
      );
      if (a && b && c) {
        found++;
      }
    }
    for (final Relation relation : relations) {
      final boolean a = relation.getSubjectEntity().equals(eKarl);
      final boolean b = relation.getObjectEntity().equals(eLeipzig);
      final boolean c = relation.getRelation().contains(//
          new URI(DBpedia.ns_dbpedia_ontology.concat("birthPlace"))//
      );
      if (a && b && c) {
        found++;
      }
    }
    Assert.assertTrue(found == 2);
  }

  @Test
  public void test2() throws URISyntaxException {
    final URI uri = new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"));
    final String text = "Alice who married Bob.";
    final Entity s = newEntity("Alice", EntityTypes.P, 0);
    final Entity o = newEntity("Bob", EntityTypes.P, 18);

    final List<Entity> entities = new ArrayList<>();
    entities.add(s);
    entities.add(o);
    boaen.setInput(text, entities);
    final Set<Relation> relations = boaen.extract();

    Assert.assertEquals(1, relations.size());

    final Relation relation = relations.iterator().next();

    Assert.assertEquals(true, relation.getRelation().contains(uri));
  }
}
