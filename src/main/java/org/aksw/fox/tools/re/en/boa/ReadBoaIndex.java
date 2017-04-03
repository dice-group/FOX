package org.aksw.fox.tools.re.en.boa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class ReadBoaIndex extends BoaIndex {

  /**
   *
   * Constructor.
   *
   * @param file boa index
   */
  public ReadBoaIndex(final String file) {
    super(file);
  }

  /**
   * Test.
   *
   * @param args
   * @throws IOException <code>
  public static void main(final String[] args) throws IOException {
    PropertyConfigurator.configure("log4j.properties");
    final String file = "/media/rspeck/store/Data/boa_backup/solr/data/boa/en/index/";
    final ReadBoaIndex index = new ReadBoaIndex(file);
  
    final String p = "http://dbpedia.org/ontology/spouse";
    index.processSearch(p);
  }
  </code>
   */
  public Map<String, Pattern> processSearch(final String p) throws IOException {
    final Map<String, Pattern> patterns = new HashMap<String, Pattern>();

    final IndexSearcher searcher = openIndexSearcher();

    //
    final BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(ReadBoaEnum.URI.getLabel(), p)), Occur.MUST);

    final int numResults = 50;

    final Sort sort = new Sort(new SortField(//
        ReadBoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel(), SortField.Type.DOUBLE, true//
    ));

    // search
    final ScoreDoc[] hits = searcher.search(query, numResults, sort).scoreDocs;

    LOG.info("hits:" + hits.length);

    final Set<String> gPattern = new HashSet<>();
    final Set<String> noVarPattern = new HashSet<>();

    for (int i = 0; i < hits.length; i++) {
      final Document doc = searcher.doc(hits[i].doc);

      gPattern.add(doc.getField(ReadBoaEnum.NLR_GEN.getLabel()).stringValue().trim());
      noVarPattern.add(doc.getField(ReadBoaEnum.NLR_NO_VAR.getLabel()).stringValue().trim());

      final Pattern pattern = new Pattern();
      pattern.naturalLanguageRepresentation =
          doc.getField(ReadBoaEnum.NLR_VAR.getLabel()).stringValue().trim();
      pattern.generalized = doc.getField(ReadBoaEnum.NLR_GEN.getLabel()).stringValue().trim();
      pattern.naturalLanguageRepresentationWithoutVariables =
          doc.getField(ReadBoaEnum.NLR_NO_VAR.getLabel()).stringValue().trim();
      pattern.posTags = doc.getField(ReadBoaEnum.POS.getLabel()).stringValue().trim();
      pattern.boaScore =
          new Double(doc.getField(ReadBoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel())
              .numericValue().floatValue()//
          );
      pattern.language = "en";

      final int maxpattern = 10;
      if (!pattern.getNormalized().trim().isEmpty()
          && !patterns.containsKey(pattern.getNormalized()) && (patterns.size() < maxpattern)) {
        patterns.put(pattern.getNormalized().trim(), pattern);
      }
    }

    LOG.info("patterns.size:" + patterns.size());
    closeIndexSearcher();

    // sort
    class StringComparator implements Comparator<String> {
      @Override
      public int compare(final String o1, final String o2) {
        return Integer.compare(o2.length(), o1.length());
      }
    }
    final List<String> list = new ArrayList<>(noVarPattern);
    Collections.sort(list, new StringComparator());

    LOG.info("longest pattern:" + list.iterator().next());

    return patterns;
  }
}
