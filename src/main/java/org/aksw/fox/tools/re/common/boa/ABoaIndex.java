package org.aksw.fox.tools.re.common.boa;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;
import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 *
 * We mapped leaderName, team, deathPlace, birthPlace, spouse, team, foundationPlace, subsidiary.
 *
 * <code>
en:
http://dbpedia.org/ontology/author
http://dbpedia.org/ontology/award
http://dbpedia.org/ontology/birthPlace
http://dbpedia.org/ontology/deathPlace
http://dbpedia.org/ontology/foundationPlace <---
http://dbpedia.org/ontology/leaderName
http://dbpedia.org/ontology/spouse
http://dbpedia.org/ontology/starring
http://dbpedia.org/ontology/subsidiary
http://dbpedia.org/ontology/team

de
http://dbpedia.org/ontology/author
http://dbpedia.org/ontology/award
http://dbpedia.org/ontology/birthPlace
http://dbpedia.org/ontology/deathPlace
http://dbpedia.org/ontology/leaderName
http://dbpedia.org/ontology/spouse
http://dbpedia.org/ontology/starring
http://dbpedia.org/ontology/subsidiary
http://dbpedia.org/ontology/team

fr
http://dbpedia.org/ontology/author
http://dbpedia.org/ontology/award
http://dbpedia.org/ontology/birthPlace
http://dbpedia.org/ontology/deathPlace
http://dbpedia.org/ontology/foundationPlace
http://dbpedia.org/ontology/leaderName
http://dbpedia.org/ontology/spouse
http://dbpedia.org/ontology/starring
http://dbpedia.org/ontology/subsidiary
http://dbpedia.org/ontology/team

</code>
 */
abstract public class ABoaIndex extends AbstractRE {

  protected String luceneIndexFolder = null;
  protected IndexReader indexReader = null;
  protected IndexSearcher indexSearcher = null;
  protected Directory dir = null;

  protected String lang = FoxParameter.Langs.EN.name().toLowerCase();

  // domain to range and relation
  Map<String, Map<String, Set<String>>> supportedRelations = new HashMap<>();

  /**
   *
   * Constructor.
   *
   * @param file boa index
   */
  public ABoaIndex(final String lang) {
    this.lang = lang;

    luceneIndexFolder = "data/boa/" + lang;
    if (!Files.exists(Paths.get(luceneIndexFolder))) {

      final File tarFile = Paths.get("data/boa/boa_" + lang + "_10.tar.gz").toFile();
      final Project p = new Project();
      final Untar ut = new Untar();
      ut.setProject(p);
      ut.setSrc(tarFile);
      if (tarFile.getName().endsWith(".gz")) {
        ut.setCompression((UntarCompressionMethod) EnumeratedAttribute
            .getInstance(UntarCompressionMethod.class, "gzip"));
      }
      ut.setDest(Paths.get(luceneIndexFolder).toFile());
      ut.perform();
    }

    createSupportedBoaRelations();
  }

  public void closeIndexSearcher() throws IOException {
    if (indexReader != null) {
      indexReader.close();
      dir.close();
    }
  }

  public IndexSearcher openIndexSearcher() throws IOException {
    closeIndexSearcher();
    dir = FSDirectory.open(new File(luceneIndexFolder));
    indexReader = DirectoryReader.open(dir);
    indexSearcher = new IndexSearcher(indexReader);
    return indexSearcher;
  }

  /**
   * Gets all possible relations for the type pair combination.
   *
   * @param sType subject type
   * @param oType object type
   * @return matching uris
   */
  public Set<String> getSupportedBoaRelations(final String domain, final String range) {
    return supportedRelations.get(domain).get(range);
  }

  public Map<String, BoaPattern> processSearch(final String p) throws IOException {
    final Map<String, BoaPattern> patterns = new HashMap<String, BoaPattern>();

    final IndexSearcher searcher = openIndexSearcher();

    //
    final BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(BoaEnum.URI.getLabel(), p)), Occur.MUST);

    final int numResults = 50;

    final Sort sort = new Sort(new SortField(//
        BoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel(), SortField.Type.DOUBLE, true//
    ));

    // search
    final ScoreDoc[] hits = searcher.search(query, numResults, sort).scoreDocs;

    LOG.info("hits:" + hits.length);

    final Set<String> gPattern = new HashSet<>();
    final Set<String> noVarPattern = new HashSet<>();

    for (int i = 0; i < hits.length; i++) {
      final Document doc = searcher.doc(hits[i].doc);

      gPattern.add(doc.getField(BoaEnum.NLR_GEN.getLabel()).stringValue().trim());
      noVarPattern.add(doc.getField(BoaEnum.NLR_NO_VAR.getLabel()).stringValue().trim());

      final BoaPattern pattern = new BoaPattern();
      pattern.naturalLanguageRepresentation =
          doc.getField(BoaEnum.NLR_VAR.getLabel()).stringValue().trim();
      pattern.generalized = doc.getField(BoaEnum.NLR_GEN.getLabel()).stringValue().trim();
      pattern.naturalLanguageRepresentationWithoutVariables =
          doc.getField(BoaEnum.NLR_NO_VAR.getLabel()).stringValue().trim();
      pattern.posTags = doc.getField(BoaEnum.POS.getLabel()).stringValue().trim();
      pattern.boaScore =
          new Double(doc.getField(BoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel())
              .numericValue().floatValue()//
          );
      pattern.language = lang;

      final int maxpattern = 10;
      if (!pattern.getNormalized().trim().isEmpty()
          && !patterns.containsKey(pattern.getNormalized()) && (patterns.size() < maxpattern)) {
        patterns.put(pattern.getNormalized().trim(), pattern);
      }
    }
    closeIndexSearcher();
    LOG.info("patterns.size:" + patterns.size());

    // sort pattern length
    /**
     * <code>
      if (!patterns.isEmpty()) {
     class StringComparator implements Comparator<String> {
       &#64;Override
       public int compare(final String o1, final String o2) {
         return Integer.compare(o2.length(), o1.length());
       }
     }
     final List<String> list = new ArrayList<>(noVarPattern);
     Collections.sort(list, new StringComparator());
     LOG.info("longest pattern:" + list.iterator().next());
     }
     </code>
     */
    return patterns;
  }

  @Override
  public Set<Relation> extract() {
    relations.clear();

    if ((entities != null) && !entities.isEmpty()) {
      return _extract(input, breakdownAndSortEntity(entities));
    } else {
      LOG.warn("Entities not given!");
    }

    return relations;
  }

  /**
   *
   * @param text
   * @param entities
   * @return
   */
  private Set<Relation> _extract(final String text, final List<Entity> entities) {

    for (int i = 0; (i + 1) < entities.size(); i++) {
      final Entity subject = entities.get(i);
      final Entity object = entities.get(i + 1);

      final String sType = subject.getType();
      final String oType = object.getType();

      final Set<String> uris = getSupportedBoaRelations(sType, oType);
      LOG.debug("uris that match the entity types: " + uris);

      final int sIndex = subject.getIndices().iterator().next();
      final int oIndex = object.getIndices().iterator().next();

      final String substring = text.substring(sIndex + subject.getText().length(), oIndex).trim();
      LOG.debug("substring with possible pattern: " + substring);
      for (final String uri : uris) {
        final Map<String, BoaPattern> pattern = getPattern(uri);

        if (pattern.keySet().contains(substring)) {
          Relation relation;
          try {
            relation = new Relation(//
                subject, //
                substring, //
                uri, //
                object, //
                Arrays.asList(new URI(uri)), //
                getToolName(), //
                Relation.DEFAULT_RELEVANCE//
            );
            relations.add(relation);
          } catch (final URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
          }
        }
      }
    }
    return relations;
  }

  /**
   * Gets boa pattern from index.
   *
   * @param uri
   * @return pattern
   */
  public Map<String, BoaPattern> getPattern(final String uri) {
    try {
      final Map<String, BoaPattern> pattern = processSearch(uri);
      pattern.keySet().forEach(LOG::info);
      return pattern;
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return new HashMap<>();
  }

  protected void createSupportedBoaRelations() {
    supportedRelations.put(EntityClassMap.L, new HashMap<>());
    supportedRelations.put(EntityClassMap.P, new HashMap<>());
    supportedRelations.put(EntityClassMap.O, new HashMap<>());

    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.L).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.P).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.L, new HashSet<String>());
    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.P, new HashSet<String>());
    supportedRelations.get(EntityClassMap.O).put(EntityClassMap.O, new HashSet<String>());

    supportedRelations.get(EntityClassMap.L).get(EntityClassMap.P)
        .add("http://dbpedia.org/ontology/leaderName");
    supportedRelations.get(EntityClassMap.L).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");

    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/deathPlace");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/birthPlace");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.P)
        .add("http://dbpedia.org/ontology/spouse");
    supportedRelations.get(EntityClassMap.P).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");

    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.L)
        .add("http://dbpedia.org/ontology/foundationPlace");
    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/team");
    supportedRelations.get(EntityClassMap.O).get(EntityClassMap.O)
        .add("http://dbpedia.org/ontology/subsidiary");
  }

}
