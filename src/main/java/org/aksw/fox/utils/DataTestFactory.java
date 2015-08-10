package org.aksw.fox.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;

public class DataTestFactory {
    String input = "The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, "
                         + "and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). "
                         + "The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.";

    private Entity getE1() {
        Entity e = new Entity("University of Leipzig", EntityClassMap.O, 1f, "fox");
        e.addIndicies(22);
        e.uri = "http://dbpedia.org/resource/Leipzig_University";
        return e;
    }

    private Entity getE2() {
        Entity ee = new Entity("Gottfried Wilhelm Leibniz", EntityClassMap.P, 1f, "fox");
        ee.addIndicies(291);
        ee.uri = "http://dbpedia.org/resource/Gottfried_Wilhelm_Leibniz";
        return ee;
    }

    private Entity getE3() {
        Entity eee = new Entity("Leipzig", EntityClassMap.L, 1f, "fox");
        eee.addIndicies(329);
        eee.uri = "http://dbpedia.org/resource/Leipzig";
        return eee;
    }

    /**
     * Test data for entities.
     * 
     * @return map with input text as key and a set of entities as value.
     */
    public Map<String, Set<Entity>> getTestEntities() {
        Map<String, Set<Entity>> map = new HashMap<String, Set<Entity>>();
        map.put(input, new HashSet<>());
        map.get(input).add(getE1());
        map.get(input).add(getE2());
        map.get(input).add(getE3());

        return map;
    }

    /**
     * Test data for relations.
     * 
     * @return map with input text as key and a set of relations as value.
     */
    public Map<String, Set<Relation>> getTestRelations() {
        Map<String, Set<Relation>> map = new HashMap<String, Set<Relation>>();
        map.put(input, new HashSet<>());

        try {
            Relation r = new Relation(getE2(), "was born in", "BornIn", getE3(), Arrays.asList(new URI("http://ns.aksw.org/scms/annotations/stanford/liveIn")), "REfoxTool", 1f);
            map.get(input).add(r);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return map;
    }
}
