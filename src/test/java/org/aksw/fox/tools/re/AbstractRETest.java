package org.aksw.fox.tools.re;

import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.Relation;
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.junit.Assert;
import org.junit.Test;

public class AbstractRETest extends AbstractRE {

  String dbpediaPerson = DBpedia.ns_dbpedia_ontology.concat("Person");
  String dbpediaPlace = DBpedia.ns_dbpedia_ontology.concat("Place");
  String dbpediaSpouse = DBpedia.ns_dbpedia_ontology.concat("spouse");
  String dbpediaBirthPlace = DBpedia.ns_dbpedia_ontology.concat("birthPlace");

  /**
   * Tests the checkDomainRange method.
   */
  @Test
  public void testA() {
    Assert.assertTrue(checkDomainRange(dbpediaPerson, dbpediaSpouse, dbpediaPerson));
    Assert.assertFalse(checkDomainRange(dbpediaPlace, dbpediaSpouse, dbpediaPerson));
    Assert.assertFalse(checkDomainRange(dbpediaPlace, dbpediaSpouse, dbpediaPlace));
    Assert.assertFalse(checkDomainRange(dbpediaPerson, dbpediaSpouse, dbpediaPlace));

    Assert.assertTrue(checkDomainRange(dbpediaPerson, dbpediaBirthPlace, dbpediaPlace));
    Assert.assertFalse(checkDomainRange(dbpediaPlace, dbpediaBirthPlace, dbpediaPerson));
  }

  @Test
  public void testC() {
    Assert.assertEquals(mapFoxTypesToDBpediaTypes(EntityClassMap.L),
        DBpedia.ns_dbpedia_ontology.concat("Place"));
    Assert.assertEquals(mapFoxTypesToDBpediaTypes(EntityClassMap.P),
        DBpedia.ns_dbpedia_ontology.concat("Person"));
    Assert.assertEquals(mapFoxTypesToDBpediaTypes(EntityClassMap.O),
        DBpedia.ns_dbpedia_ontology.concat("Organisation"));
  }

  @Override
  protected Set<Relation> _extract(final String text, final List<Entity> entities) {
    return null;
  }
}
