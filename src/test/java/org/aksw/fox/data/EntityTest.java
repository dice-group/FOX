package org.aksw.fox.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EntityTest {

    @Test
    public void test() {
        PropertyConfigurator.configure("log4j.properties");

        equals();
        contains();
        copy();
    }

    public void equals() {

        assertEquals(new Entity("a", "b"), new Entity("a", "b"));
        assertEquals(new Entity("a", "b", 1), new Entity("a", "b"));
        assertEquals(new Entity("a", "b", 1, "c"), new Entity("a", "b"));

        assertNotEquals(new Entity("a", "b"), new Entity("a", "a"));
        assertNotEquals(new Entity("a", "b"), new Entity("b", "b"));
        assertNotEquals(new Entity("a", "a"), new Entity("a", "b"));
        assertNotEquals(new Entity("b", "b"), new Entity("a", "b"));
    }

    public void contains() {
        Set<Entity> set = new HashSet<>();
        set.add(new Entity("B.H. Lim", EntityClassMap.P));

        assertTrue(set.contains(new Entity("B.H. Lim", EntityClassMap.P)));

        assertFalse(set.contains(new Entity("B.H. Lim ", EntityClassMap.P)));
        assertFalse(set.contains(new Entity(" B.H. Lim", EntityClassMap.P)));
        assertFalse(set.contains(new Entity("B.H. Lim", EntityClassMap.O)));
    }

    public void copy() {
        Set<Entity> set = new HashSet<>();
        set.add(new Entity("B.H. Lim", EntityClassMap.P));
        set.add(new Entity("B.H.", EntityClassMap.P));

        for (Entity e : set) {
            e.setText("");
            e.addText("B.H.");
            e.addText("Lim");
        }
        assertTrue(set.size() == 2);
        set = new HashSet<Entity>(set);
        assertTrue(set.size() == 1);
    }

}
