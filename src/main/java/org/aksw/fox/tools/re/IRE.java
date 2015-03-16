package org.aksw.fox.tools.re;

import java.util.Set;

import org.aksw.fox.data.Relation;

public interface IRE {

    public Set<Relation> extract(String text);

}
