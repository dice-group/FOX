package org.aksw.fox.webservice.oke;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.transfer.nif.Document;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class OKEDataReader {

  public static Logger LOG = LogManager.getLogger(OKEDataReader.class);

  public Map<String, Map<String, Set<String>>> domainRangeRelation = new HashMap<>();

  public Set<String> relations = new HashSet<>();

  /**
   *
   * Constructor.
   *
   */
  public OKEDataReader() {
    domainRangeRelation.put(EntityClassMap.L, new HashMap<>());
    domainRangeRelation.put(EntityClassMap.P, new HashMap<>());
    domainRangeRelation.put(EntityClassMap.O, new HashMap<>());

    domainRangeRelation.get(EntityClassMap.L).put(EntityClassMap.L, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.L).put(EntityClassMap.P, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.L).put(EntityClassMap.O, new HashSet<>());

    domainRangeRelation.get(EntityClassMap.P).put(EntityClassMap.L, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.P).put(EntityClassMap.P, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.P).put(EntityClassMap.O, new HashSet<>());

    domainRangeRelation.get(EntityClassMap.O).put(EntityClassMap.L, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.O).put(EntityClassMap.P, new HashSet<>());
    domainRangeRelation.get(EntityClassMap.O).put(EntityClassMap.O, new HashSet<>());

    addRelations();
  }

  /**
   *
   * @param d domain
   * @param r range
   * @param p predicate
   */
  protected void _addRelations(final String d, final String r, final String... p) {
    for (final String _p : p) {
      domainRangeRelation.get(d).get(r).add(_p);
      relations.add(_p);
    }
  }

  public void addRelations() {
    //
    _addRelations(EntityClassMap.L, EntityClassMap.L, "country", "department", "district",
        "locatedInArea", "location");
    _addRelations(EntityClassMap.L, EntityClassMap.P, "leaderName");
    _addRelations(EntityClassMap.L, EntityClassMap.O, "tenant");

    //
    _addRelations(EntityClassMap.P, EntityClassMap.L, "country", "deathPlace", "birthPlace",
        "hometown", "location", "nationality");
    _addRelations(EntityClassMap.P, EntityClassMap.P, "child", "doctoralAdvisor", "doctoralStudent",
        "parent", "relative", "spouse", "trainer");
    _addRelations(EntityClassMap.P, EntityClassMap.O, "almaMater", "club", "debutTeam", "employer",
        "formerTeam");

    //
    _addRelations(EntityClassMap.O, EntityClassMap.L, "country", "foundationPlace", "headquarter",
        "hometown", "location");
    _addRelations(EntityClassMap.O, EntityClassMap.P, "bandMember", "ceo", "formerBandMember",
        "president");
    _addRelations(EntityClassMap.O, EntityClassMap.O, "affiliation", "subsidiary");
  }

  public List<Document> parseInput(final String in) {
    List<Document> docs = null;
    try {
      docs = new TurtleNIFParser().parseNIF(in);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return docs;
  }
}
