package org.aksw.fox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.TokenManager;
import org.aksw.fox.nerlearner.PostProcessing;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PostProcessingTest {

    String input1 = "That's a tester that test That's a tester that test.";
    String input2 = "Uni of Lpz in Lpz. Uni of Lpz in Lpz.";
    String input3 = "A. Foobar, B. More Foobar, C. Foobar and D. Foobar.";

    Map<String, Set<Entity>> toolResults = new LinkedHashMap<>();
    Map<String, String> oracel = new HashMap<>();

    public PostProcessingTest() {
        PropertyConfigurator.configure("log4j.properties");
    }

    @Test
    public void test() {

        // getLabeledToolResults();
        // oracelLabel();
        // getIndexToken();
        //
        // getLabeledMap();
    }

    public void getLabeledMap() {
        oracel.clear();
        toolResults.clear();
        TokenManager token = new TokenManager(input3);
        PostProcessing pp = new PostProcessing(token, toolResults);
        oracel.put("A. Foobar  B. More Foobar", EntityClassMap.P);

        System.out.println(pp.getLabeledMap(oracel));
    }

    public void getLabeledToolResults() {
        oracel.clear();
        toolResults.clear();

        Set<Entity> toolA = new HashSet<>();
        toolA.add(new Entity("Uni of Lpz", EntityClassMap.O));
        toolA.add(new Entity("Lpz", EntityClassMap.L));

        Map<String, Set<Entity>> map = new LinkedHashMap<>();
        map.put("toolA", toolA);

        TokenManager token = new TokenManager(input2);
        Set<Entity> list = new PostProcessing(token, map).getLabeledToolResults().get("toolA");

        Set<String> entities = new LinkedHashSet<>();
        for (Entity e : list)
            entities.add(e.getText());

        // TODO: we canged the labelss
        assertTrue(entities.contains("Uni0 of4 Lpz7"));
        assertTrue(entities.contains("Lpz14"));
        assertTrue(entities.contains("Uni19 of23 Lpz26"));

        assertFalse(entities.contains("Lpz7"));
    }

    public void oracelLabel() {
        oracel.clear();
        toolResults.clear();

        oracel.put("Lpz", EntityClassMap.entityClasses.get(0));
        oracel.put("Uni of Lpz", EntityClassMap.entityClasses.get(1));
        TokenManager token = new TokenManager(input2);
        PostProcessing pp = new PostProcessing(token, toolResults);
        Map<String, String> oracelLabel = pp.getLabeledMap(oracel);

        assertEquals(oracelLabel.get("Uni0 of4 Lpz7"), EntityClassMap.entityClasses.get(1));
        assertEquals(oracelLabel.get("Lpz14"), EntityClassMap.entityClasses.get(0));
        assertNull(oracelLabel.get("Lpz7"));
        assertEquals(oracelLabel.get("Uni19 of23 Lpz26"), EntityClassMap.entityClasses.get(1));
        assertNull(oracelLabel.get("Lpz26"));
        assertEquals(oracelLabel.get("Lpz33"), EntityClassMap.entityClasses.get(0));
        assertEquals(oracelLabel.size(), 4);
    }

    public void getIndexToken() {
        oracel.clear();
        toolResults.clear();

        Set<Entity> toolA = new HashSet<>();
        toolA.add(new Entity("That's a", EntityClassMap.entityClasses.get(0)));
        toolA.add(new Entity("tester", EntityClassMap.entityClasses.get(1)));

        Set<Entity> toolB = new HashSet<>();
        toolB.add(new Entity("That's a", EntityClassMap.entityClasses.get(1)));
        toolB.add(new Entity("tester", EntityClassMap.entityClasses.get(2)));

        Map<String, Set<Entity>> map = new LinkedHashMap<>();
        map.put("toolA", toolA);
        map.put("toolB", toolB);

        Map<String, String> oracel = new HashMap<>();
        oracel.put("That's a", EntityClassMap.entityClasses.get(0));
        oracel.put("tester", EntityClassMap.entityClasses.get(2));

        TokenManager token = new TokenManager(input1);
        // PostProcessing pp = new PostProcessing(token, map);

        assertEquals(token.getToken(0), "That");
        assertEquals(token.getToken("That".length() + 1), "s");
        assertEquals(token.getToken("That".length() + 1 + 2), "a");
        assertEquals(token.getToken("That".length() + 1 + 2 + 2), "tester");

        assertEquals(token.getLabelIndex(token.getLabel(0)), 0);
    }
}
