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
import org.junit.Before;
import org.junit.Test;

public class PattyTest {
  public static Logger LOG = LogManager.getLogger(PattyTest.class);

  PattyEN patty = null;

  String paraphrases = "data/patty/dbpedia-relation-paraphrases.txt";
  String posTagMap = "data/patty/en-ptb.map";

  /**
   * Loads Patty.
   */
  @Before
  public void init() {

    // load class
    patty = new PattyEN(paraphrases, posTagMap);
  }

  // final Map<String, Set<String>> map = p.getDBpediaRelationParaphrases();
  // map.forEach((k, v) -> LOG.info(k + " " + v.size()));
  // map.get("spouse").forEach(LOG::info);

  // spouse married;
  // spouse [[num]] married in;
  // spouse is married;
  // spouse later married to;
  // spouse is married [[con]];

  @Test
  public void testA() throws URISyntaxException {

    final String bob = "Bob Foobar";
    final String alice = "Alice Foobar";
    final String karl = "Karl Foobar";
    final String paula = "Paula Foobar";

    final String sentenceA = bob.concat(" married ").concat(alice).concat(".");
    final String sentenceB = karl.concat(" small cousin of ").concat(paula).concat(".");
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

    final Set<Relation> relations = patty._extract(text, entities);
    relations.forEach(LOG::info);

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
            new URI(DBpedia.ns_dbpedia_ontology.concat("parent"))//
        );

        if (sub && obj && spouse) {
          found = true;
        }
      }
      Assert.assertTrue(found);
    }
  }
}
