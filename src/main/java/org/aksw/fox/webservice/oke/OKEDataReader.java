package org.aksw.fox.webservice.oke;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.EntityTypes;
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
    domainRangeRelation.put(EntityTypes.L, new HashMap<>());
    domainRangeRelation.put(EntityTypes.P, new HashMap<>());
    domainRangeRelation.put(EntityTypes.O, new HashMap<>());

    domainRangeRelation.get(EntityTypes.L).put(EntityTypes.L, new HashSet<>());
    domainRangeRelation.get(EntityTypes.L).put(EntityTypes.P, new HashSet<>());
    domainRangeRelation.get(EntityTypes.L).put(EntityTypes.O, new HashSet<>());

    domainRangeRelation.get(EntityTypes.P).put(EntityTypes.L, new HashSet<>());
    domainRangeRelation.get(EntityTypes.P).put(EntityTypes.P, new HashSet<>());
    domainRangeRelation.get(EntityTypes.P).put(EntityTypes.O, new HashSet<>());

    domainRangeRelation.get(EntityTypes.O).put(EntityTypes.L, new HashSet<>());
    domainRangeRelation.get(EntityTypes.O).put(EntityTypes.P, new HashSet<>());
    domainRangeRelation.get(EntityTypes.O).put(EntityTypes.O, new HashSet<>());

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
    _addRelations(EntityTypes.L, EntityTypes.L, "country", "department", "district",
        "locatedInArea", "location");
    _addRelations(EntityTypes.L, EntityTypes.P, "leaderName");
    _addRelations(EntityTypes.L, EntityTypes.O, "tenant");

    //
    _addRelations(EntityTypes.P, EntityTypes.L, "country", "deathPlace", "birthPlace", "hometown",
        "location", "nationality");
    _addRelations(EntityTypes.P, EntityTypes.P, "child", "doctoralAdvisor", "doctoralStudent",
        "parent", "relative", "spouse", "trainer");
    _addRelations(EntityTypes.P, EntityTypes.O, "almaMater", "club", "debutTeam", "employer",
        "formerTeam");

    //
    _addRelations(EntityTypes.O, EntityTypes.L, "country", "foundationPlace", "headquarter",
        "hometown", "location");
    _addRelations(EntityTypes.O, EntityTypes.P, "bandMember", "ceo", "formerBandMember",
        "president");
    _addRelations(EntityTypes.O, EntityTypes.O, "affiliation", "subsidiary");
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
