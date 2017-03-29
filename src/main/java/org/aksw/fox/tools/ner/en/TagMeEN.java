package org.aksw.fox.tools.ner.en;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.common.TagMeCommon;
import org.aksw.fox.tools.ner.common.WikipediaCategories;
import org.aksw.fox.utils.FoxConst;

import de.renespeck.swissknife.io.SerializationUtil;

public class TagMeEN extends TagMeCommon {
  final String file = "dataEN.bin";

  public TagMeEN() {
    super(Locale.ENGLISH, "http://dbpedia.org/sparql", "http://dbpedia.org");

    final Map<String, String> fileToType = new HashMap<>();
    fileToType.put("catsOrganisationEN.bin", EntityClassMap.O);
    fileToType.put("catsLocationEN.bin", EntityClassMap.L);
    fileToType.put("catsPersonEN.bin", EntityClassMap.P);

    createEnWikipediaCategories().forEach((k, set) -> //
    set.forEach(v -> entityClasses.put(v.replace("http://dbpedia.org/resource/Category:", ""),
        fileToType.get(k))));

  }

  @SuppressWarnings("unchecked")
  public Map<String, Set<String>> createEnWikipediaCategories() {

    Map<String, Set<String>> cats = null;
    cats = SerializationUtil.deserialize(file, new HashMap<String, Set<String>>().getClass());
    if (cats == null) {
      cats = _createEnWikipediaCategories();
      try {
        if (cats != null) {
          SerializationUtil.serialize(file, cats);
        }
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return cats;
  }

  private Map<String, Set<String>> _createEnWikipediaCategories() {

    final WikipediaCategories wikipediaCategories = new WikipediaCategories();
    // load data into a map
    final Map<String, Set<String>> fileToSet = new HashMap<>();
    fileToSet.put("catsOrganisationEN.bin", //
        wikipediaCategories.queryExecAndSerialize("catsOrganisationEN.bin", "dbo:Organisation"));
    fileToSet.put("catsPersonEN.bin", //
        wikipediaCategories.queryExecAndSerialize("catsPersonEN.bin", "dbo:Person"));
    fileToSet.put("catsLocationEN.bin", //
        wikipediaCategories.queryExecAndSerialize("catsLocationEN.bin", "dbo:Place"));

    // makes uris unique in each set
    final Map<String, Set<String>> fileToSetClean = new HashMap<>();
    for (final Entry<String, Set<String>> entry : fileToSet.entrySet()) {
      final String key1 = entry.getKey();
      final Set<String> value1 = new HashSet<>(entry.getValue());
      for (final Entry<String, Set<String>> entry2 : fileToSet.entrySet()) {
        if (!key1.equals(entry2.getKey())) {
          value1.removeAll(entry2.getValue());
        }
      }
      fileToSetClean.put(key1, value1);
    }
    return fileToSetClean;
  }

  public static void main(final String[] a) throws IOException {
    new TagMeEN().retrieve(FoxConst.NER_EN_EXAMPLE_1).forEach(LOG::info);
  }
}
