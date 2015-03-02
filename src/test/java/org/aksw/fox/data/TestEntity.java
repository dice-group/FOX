package org.aksw.fox.data;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TestEntity {

    @Test
    public void entityEqualsTest() {

        Entity leipzig_a = new Entity("Leipzig", EntityClassMap.L);
        Entity leipzig_b = new Entity("Leipzig", EntityClassMap.L);

        Entity leipzig_c = new Entity("Leipzig", EntityClassMap.O);
        Entity leipzig_d = new Entity("An other", EntityClassMap.O);

        Assert.assertTrue(leipzig_a.equals(leipzig_b));
        Assert.assertFalse(leipzig_a.equals(leipzig_c));
        Assert.assertFalse(leipzig_a.equals(leipzig_d));
        Assert.assertFalse(leipzig_c.equals(leipzig_d));

        Set<Entity> set = new HashSet<>();
        set.add(leipzig_a);
        set.add(leipzig_b);
        Assert.assertTrue(set.size() == 1);

        set.add(leipzig_c);
        set.add(leipzig_c);
        set.add(leipzig_d);
        Assert.assertTrue(set.size() == 3);
    }
}
