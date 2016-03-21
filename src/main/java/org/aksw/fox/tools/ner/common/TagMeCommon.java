package org.aksw.fox.tools.ner.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;

public abstract class TagMeCommon extends AbstractNER {

  public static final XMLConfiguration CFG = CfgManager.getCfg(TagMeCommon.class);

  public static final String CFG_KEY_TAGME_KEY = "tagMe.key";
  public static final String CFG_KEY_SPARQL_ENDPOINT = "tagMe.sparqlEndpoint";
  public static final String CFG_KEY_RDFS_PREFIX = "tagMe.rdfsPrefix";
  public static final String CFG_KEY_RDF_PREFIX = "tagMe.rdfPrefix";
  public static final String CFG_KEY_MIN_WEIGHT = "tagMe.minWeight";
  public static final String CFG_KEY_ENDPOINT = "tagMe.url";
  public static final String CFG_KEY_LANG = "tagMe.lang";

  public static final String TAGME_KEY = CFG.getString(CFG_KEY_TAGME_KEY);
  public static final String SPARQL_ENDPOINT = CFG.getString(CFG_KEY_SPARQL_ENDPOINT);
  public static final String RDFS_PREFIX = CFG.getString(CFG_KEY_RDFS_PREFIX);
  public static final String ENDPOINT = CFG.getString(CFG_KEY_ENDPOINT);
  public static final String RDF_PREFIX = CFG.getString(CFG_KEY_RDF_PREFIX);
  public static final double MIN_RHO = CFG.getDouble(CFG_KEY_MIN_WEIGHT);

  public static final String include_categories = "true";
  public static final String epsilon = "0.1";
  public static final String min_comm = "0.1";
  public static final String min_link = "0.1";

  protected Locale LANG;

  /*
   * protected ConcurrentMap<String, String> categoriesToTypeLRU = new
   * ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(cacheSize) .build();
   */

  String dbpediaURL;
  String dbpediaGraph;

  /**
   *
   * @param lang
   * @param dbpediaURL
   * @param dbpediaGraph
   */
  public TagMeCommon(final Locale lang, final String dbpediaURL, final String dbpediaGraph) {
    LOG.info("TagMeCommon ... ");
    LANG = lang;

    entityList = new ArrayList<>();
    this.dbpediaGraph = dbpediaGraph;
    this.dbpediaURL = dbpediaURL;
  }

  @Override
  public List<Entity> retrieve(final String input) {
    return retrieveSentences(getSentences(LANG, input));
  }

  protected List<Entity> retrieveSentences(final List<String> sentences) {

    // _sentences with string of 15k bytes max.
    final List<String> _sentences = new ArrayList<>();
    {
      int ii = 0;
      final Map<Integer, Integer> sentenceSize = new HashMap<>();
      for (final String sentence : sentences) {
        try {
          sentenceSize.put(ii++, sentence.getBytes("UTF-8").length);
        } catch (final UnsupportedEncodingException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
      StringBuilder sb = new StringBuilder();
      int sum = 0;
      for (final Entry<Integer, Integer> entry : sentenceSize.entrySet()) {
        sum += entry.getValue();
        if (sum < 15000) {
          sb.append(sentences.get(entry.getKey()));
        } else {
          _sentences.add(sb.toString());
          sb = new StringBuilder();
          sum = 0;
        }
      }
      _sentences.add(sb.toString());
    }

    final ExecutorService executorService = Executors.newFixedThreadPool(4);
    final CompletionService<List<Entity>> completionService =
        new ExecutorCompletionService<>(executorService);

    int n = 0;
    for (int i = 0; i < _sentences.size(); i++) {
      completionService.submit(new TagMeCall(_sentences.get(i), LANG, entityClasses));
      ++n;
    }
    executorService.shutdown();
    final Set<Entity> set = new HashSet<>();
    for (int i = 0; i < n; ++i) {
      try {
        final Future<List<Entity>> future = completionService.take();
        final List<Entity> result = future.get();

        if ((result != null) && !result.isEmpty()) {
          set.addAll(result);
        } else {
          LOG.warn("No entities found.");
        }
      } catch (InterruptedException | ExecutionException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    entityList.addAll(set);
    return entityList;
  }
}
