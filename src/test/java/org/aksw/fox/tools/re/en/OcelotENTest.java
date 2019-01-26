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
    {

      final String bob = "Bob Right";
      final String alice = "Alice Right";
      final String karl = "Karl Mueller";
      final String paula = "Paula Petersen";

      final String sentenceA = bob.concat(" married ").concat(alice).concat(".");

      final String sentenceB = karl.concat(" and ").concat(paula).concat(" are married.");
      final String text = sentenceA.concat(" ").concat(sentenceB);

      final List<Entity> entities = new ArrayList<>();

      final Entity eBob = new Entity(bob, EntityTypes.P);
      eBob.addIndicies(text.indexOf(bob));

      final Entity eAlice = new Entity(alice, EntityTypes.P);
      eAlice.addIndicies(text.indexOf(alice));

      final Entity eKarl = new Entity(karl, EntityTypes.P);
      eKarl.addIndicies(text.indexOf(karl));

      final Entity ePaula = new Entity(paula, EntityTypes.P);
      ePaula.addIndicies(text.indexOf(paula));

      entities.add(eBob);
      entities.add(eAlice);
      entities.add(eKarl);
      entities.add(ePaula);

      final Set<Relation> relations = ocelot._extract(text, entities);
      relations.forEach(LOG::info);

      // checks one relation
      // bob married/spouse alice
      {
        boolean found = false;
        for (final Relation relation : relations) {
          final boolean sub = relation.getSubjectEntity().equals(eBob);
          final boolean obj = relation.getObjectEntity().equals(eAlice);
          final boolean spouse = relation.getRelation().contains(//
              new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
          );

          if (sub && obj && spouse) {
            found = true;
          }
        }
        Assert.assertTrue(found);
      }
      {
        boolean found = false;
        for (final Relation relation : relations) {
          final boolean sub = relation.getSubjectEntity().equals(eKarl);
          final boolean obj = relation.getObjectEntity().equals(ePaula);
          final boolean spouse = relation.getRelation().contains(//
              new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
          );

          if (sub && obj && spouse) {
            found = true;
          }
        }
        Assert.assertTrue(found);
      }
    }
  }

  @Test
  public void testB() throws URISyntaxException {
    {

      final String bob = "Bob";
      final String alice = "Alice";
      final String karl = "Karl";
      final String paula = "Paula";

      final String sentenceA = bob.concat(" married ").concat(alice).concat(".");

      final String sentenceB = karl.concat(" and ").concat(paula).concat(" are married.");
      final String text = sentenceA.concat(" ").concat(sentenceB);

      final List<Entity> entities = new ArrayList<>();

      final Entity eBob = new Entity(bob, EntityTypes.P);
      eBob.addIndicies(text.indexOf(bob));

      final Entity eAlice = new Entity(alice, EntityTypes.P);
      eAlice.addIndicies(text.indexOf(alice));

      final Entity eKarl = new Entity(karl, EntityTypes.P);
      eKarl.addIndicies(text.indexOf(karl));

      final Entity ePaula = new Entity(paula, EntityTypes.P);
      ePaula.addIndicies(text.indexOf(paula));

      entities.add(eBob);
      entities.add(eAlice);
      entities.add(eKarl);
      entities.add(ePaula);

      final Set<Relation> relations = ocelot._extract(text, entities);
      relations.forEach(LOG::info);

      // checks one relation
      // bob married/spouse alice
      {
        boolean found = false;
        for (final Relation relation : relations) {
          final boolean sub = relation.getSubjectEntity().equals(eBob);
          final boolean obj = relation.getObjectEntity().equals(eAlice);
          final boolean spouse = relation.getRelation().contains(//
              new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
          );

          if (sub && obj && spouse) {
            found = true;
          }
        }
        Assert.assertTrue(found);
      }
      {
        boolean found = false;
        for (final Relation relation : relations) {
          final boolean sub = relation.getSubjectEntity().equals(eKarl);
          final boolean obj = relation.getObjectEntity().equals(ePaula);
          final boolean spouse = relation.getRelation().contains(//
              new URI(DBpedia.ns_dbpedia_ontology.concat("spouse"))//
          );

          if (sub && obj && spouse) {
            found = true;
          }
        }
        Assert.assertTrue(found);
      }
    }
  }
}
