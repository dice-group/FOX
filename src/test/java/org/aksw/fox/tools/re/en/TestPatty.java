package org.aksw.fox.tools.re.en;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class TestPatty {
  public static Logger LOG = LogManager.getLogger(TestPatty.class);

  @Test
  public void test() throws URISyntaxException {

    // spouse married;
    // spouse [[num]] married in;
    // spouse is married;
    // spouse later married to;
    // spouse is married [[con]];

    final String paraphrases = "data/patty/dbpedia-relation-paraphrases.txt";
    final String posTagMap = "data/patty/en-ptb.map";

    // load class
    final PattyEN p = new PattyEN(paraphrases, posTagMap);
    // final Map<String, Set<String>> map = p.getDBpediaRelationParaphrases();

    // map.forEach((k, v) -> LOG.info(k + " " + v.size()));
    // map.get("spouse").forEach(LOG::info);

    // example extraction
    final String bob = "Bob";
    final String alice = "Alice";
    final String karl = "Karl";
    final String paula = "Paula";

    final String sentenceA = bob.concat(" married ").concat(alice).concat(".");

    final String sentenceB = karl.concat(" small cousin of ").concat(paula).concat(".");
    final String text = sentenceA.concat(" ").concat(sentenceB);

    final List<Entity> entities = new ArrayList<>();

    final Entity eBob = new Entity(bob, EntityClassMap.P);
    eBob.addIndicies(text.indexOf(bob));

    final Entity eAlice = new Entity(alice, EntityClassMap.P);
    eAlice.addIndicies(text.indexOf(alice));

    final Entity eKarl = new Entity(karl, EntityClassMap.P);
    eKarl.addIndicies(text.indexOf(karl));

    final Entity ePaula = new Entity(paula, EntityClassMap.P);
    ePaula.addIndicies(text.indexOf(paula));

    entities.add(eBob);
    entities.add(eAlice);
    entities.add(eKarl);
    entities.add(ePaula);

    final Set<Relation> relations = p._extract(text, entities);

    // checks one relation
    // bob spouse alice
    boolean found = false;
    for (final Relation relation : relations) {
      final boolean sub = relation.getSubjectEntity().equals(eBob);
      final boolean obj = relation.getObjectEntity().equals(eAlice);
      final boolean spouse = relation.getRelation().contains(//
          new URI(PattyEN.dbpediaOntology.concat("spouse"))//
      );

      if (sub && obj && spouse) {
        found = true;
      }
    }
    Assert.assertTrue(found);
  }
}
