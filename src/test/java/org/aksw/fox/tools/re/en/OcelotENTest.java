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

public class OcelotENTest {
  public static Logger LOG = LogManager.getLogger(OcelotENTest.class);

  final OcelotEN ocelot = new OcelotEN();

  @Test
  public void testA() throws URISyntaxException {

    final String bob = "Bob Right";
    final String alice = "Alice Right";

    final String text = bob.concat(" married ").concat(alice).concat(".");

    final List<Entity> entities = new ArrayList<>();

    final Entity eBob = new Entity(bob, EntityTypes.P, text.indexOf(bob));
    final Entity eAlice = new Entity(alice, EntityTypes.P, text.indexOf(alice));

    entities.add(eBob);
    entities.add(eAlice);

    final Set<Relation> relations = ocelot._extract(text, entities);

    for (final Relation relation : relations) {
      Assert.assertTrue(relation.getSubjectEntity().equals(eBob));
      Assert.assertTrue(relation.getObjectEntity().equals(eAlice));
      Assert.assertTrue(relation.getRelation().contains(//
          new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
      ));
    }
  }

  @Test
  public void testB() throws URISyntaxException {

    final String karl = "Karl Mueller";
    final String paula = "Paula Petersen";

    final String text = karl.concat(" and ").concat(paula).concat(" are married.");

    final List<Entity> entities = new ArrayList<>();

    final Entity eKarl = new Entity(karl, EntityTypes.P, text.indexOf(karl));
    final Entity ePaula = new Entity(paula, EntityTypes.P, text.indexOf(paula));

    entities.add(eKarl);
    entities.add(ePaula);

    final Set<Relation> relations = ocelot._extract(text, entities);

    for (final Relation relation : relations) {
      Assert.assertTrue(relation.getSubjectEntity().equals(eKarl));
      Assert.assertTrue(relation.getObjectEntity().equals(ePaula));
      Assert.assertTrue(relation.getRelation().contains(//
          new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
      ));
    }
  }

  @Test
  public void testC() throws URISyntaxException {
    final String bob = "Bob Right";
    final String alice = "Alice Right";

    final String sentenceA = bob.concat(" married ").concat(alice).concat(".");

    final String karl = "Karl Mueller";
    final String paula = "Paula Petersen";

    final String sentenceB = karl.concat(" and ").concat(paula).concat(" are married.");

    final String text = sentenceA.concat(" ").concat(sentenceB);

    final List<Entity> entities = new ArrayList<>();

    final Entity eBob = new Entity(bob, EntityTypes.P, text.indexOf(bob));
    final Entity eAlice = new Entity(alice, EntityTypes.P, text.indexOf(alice));

    final Entity eKarl = new Entity(karl, EntityTypes.P, text.indexOf(karl));
    final Entity ePaula = new Entity(paula, EntityTypes.P, text.indexOf(paula));

    entities.add(eBob);
    entities.add(eAlice);
    entities.add(eKarl);
    entities.add(ePaula);

    final Set<Relation> relations = ocelot._extract(text, entities);

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
      final boolean b = relation.getObjectEntity().equals(ePaula);
      final boolean c = relation.getRelation().contains(//
          new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
      );
      if (a && b && c) {
        found++;
      }
    }
    Assert.assertTrue(found == 2);
  }
}
