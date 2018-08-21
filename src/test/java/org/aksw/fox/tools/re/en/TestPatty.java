package org.aksw.fox.tools.re.en;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class TestPatty {
  public static Logger LOG = LogManager.getLogger(TestPatty.class);

  @Test
  public void test() {

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

    final Entity e = new Entity(bob, EntityClassMap.P);
    e.addIndicies(text.indexOf(bob));

    final Entity ee = new Entity(alice, EntityClassMap.P);
    ee.addIndicies(text.indexOf(alice));

    final Entity eee = new Entity(karl, EntityClassMap.P);
    eee.addIndicies(text.indexOf(karl));

    final Entity eeee = new Entity(paula, EntityClassMap.P);
    eeee.addIndicies(text.indexOf(paula));

    entities.add(e);
    entities.add(ee);
    entities.add(eee);
    entities.add(eeee);

    final Set<Relation> relations = p._extract(text, entities);
    relations.forEach(LOG::info);
  }
}
