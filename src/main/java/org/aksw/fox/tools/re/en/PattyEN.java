package org.aksw.fox.tools.re.en;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.tools.re.AbstractRE;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;

import edu.stanford.nlp.ling.CoreLabel;

/**
 * TODO: type check (NER types are not checked against the relations domain and range).
 *
 *
 * Uses the patty pattern in 'dbpedia-relation-paraphrases.txt' to extract relations.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class PattyEN extends AbstractRE {

  protected final StanfordPipe stanford = StanfordPipe.getStanfordPipe();

  // FIXME: move to config.
  public static String dbpediaOntology = "http://dbpedia.org/ontology/";

  protected Map<String, Set<String>> paraphrases = null;
  // pattern to relation,e.g. married; -> spouse,birthplace
  protected Map<String, Set<String>> paraphrasesIndex = null;

  protected Map<String, String> ptbToUniPos = null;

  /**
   * Calls {@link #PattyEN(String, String)} with "data/patty/dbpedia-relation-paraphrases.txt" and
   * "data/patty/en-ptb.map".
   */
  public PattyEN() {
    // FIXME: add to resources
    this(//
        "data/patty/dbpedia-relation-paraphrases.txt", //
        "data/patty/en-ptb.map"//
    );
  }

  /**
   *
   * Reads data from files.
   *
   * @param paraphrasesFile "data/patty/dbpedia-relation-paraphrases.txt"
   * @param posTagMapFile "data/patty/en-ptb.map"
   */
  public PattyEN(final String paraphrasesFile, final String posTagMapFile) {
    paraphrases = readDBpediaRelationParaphrases(paraphrasesFile);
    ptbToUniPos = readPosTagMapFile(posTagMapFile);
  }

  /**
   *
   * @return DBpediaRelationParaphrases
   */
  public Map<String, Set<String>> getDBpediaRelationParaphrases() {
    return paraphrases;
  }

  /**
   * Needs an update.
   *
   * @return
   */
  @Deprecated
  protected Map<String, Set<String>> readTop10Wikipedia() {
    final String top = "/home/rspeck/Downloads/patty/patty-eval/wikipedia-top100.txt";
    final Map<String, Set<String>> map = new ConcurrentHashMap<>();
    final Map<String, Set<String>> topMap = new ConcurrentHashMap<>();
    // read top
    try (Stream<String> stream = Files.lines(Paths.get(top))) {
      final List<String> list = stream//
          .filter(line -> !line.startsWith("Synset"))//
          .filter(line -> !line.startsWith("TrueORFalse"))//
          // .filter(line->line.isEmpty())//
          .collect(Collectors.toList());

      final Set<String> patternBlock = new TreeSet<>();
      boolean newBlock = false;
      for (final String pattern : list) {

        // read block
        if (!pattern.trim().isEmpty()) {
          patternBlock.add(pattern);
        } else {
          newBlock = true;
        }

        // process block
        if (newBlock) {
          String p = "";
          for (final Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().containsAll(patternBlock)) {
              p = entry.getKey();
              if (topMap.containsKey(p)) {
                topMap.get(p).addAll(patternBlock);
              } else {
                topMap.put(p, new HashSet<>(patternBlock));
              }
              break;
            }
          } // end for
          if (p.isEmpty()) {
            LOG.error("not found " + patternBlock);
          }

          newBlock = false;
          patternBlock.clear();
        } // end block
      }
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    topMap.entrySet().forEach(LOG::info);
    return topMap;
  }

  private Map<String, String> readPosTagMapFile(final String posTagMapFile) {
    final Map<String, String> ptbToUniPos = new HashMap<>();
    try (final Stream<String> stream = Files.lines(Paths.get(posTagMapFile))) {
      stream//
          .collect(Collectors.toList())//
          .stream().map(line -> line.split("\t"))//
          .forEach(split -> {
            if (split.length > 1) {
              ptbToUniPos.put(split[0], split[1]);
            }
          });
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return ptbToUniPos;
  }

  /**
   * Reads the content of {@link #paraphrasesFile} and returns the content.
   *
   * @return property to patterns, e.g. spouse -> {"married";"[[num]] married in","is married"}
   *
   */
  private Map<String, Set<String>> readDBpediaRelationParaphrases(final String paraphrasesFile) {

    if (paraphrases == null) {
      paraphrases = new ConcurrentHashMap<String, Set<String>>();
      paraphrasesIndex = new ConcurrentHashMap<String, Set<String>>();
      try (final Stream<String> stream = Files.lines(Paths.get(paraphrasesFile))) {

        // all lines in the file
        final List<String> list = stream//
            .skip(1)//
            .collect(Collectors.toList());

        // add pattern to data
        list.stream().map(line -> line.split("\t"))//
            .forEach(split -> {
              String pattern = split[1];
              if (pattern.endsWith(";")) {
                pattern = pattern.substring(0, pattern.length() - 1);
              }

              paraphrases.computeIfAbsent(split[0], k -> new HashSet<String>());
              paraphrasesIndex.computeIfAbsent(pattern, k -> new HashSet<String>());

              paraphrases.get(split[0]).add(pattern);
              paraphrasesIndex.get(pattern).add(split[0]);

            });

      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return paraphrases;
  }

  // FIXME: check domain and range of the relations to the entities. so that e.g. 2 persons are not
  // a member of a band.
  @Override
  protected Set<Relation> _extract(final String text, final List<Entity> entities) {

    // keep the original entities with IDs
    final Map<Integer, Entity> idMap = setEntityIDs(entities);

    // copies all entities since we change the index of those
    final List<Entity> copiedEntities = new ArrayList<>();
    entities.forEach(entity -> copiedEntities.add(new Entity(entity)));

    // sentences in the text
    final Map<Integer, String> sentences = stanford.getSentenceIndex(text);
    // find entities for sentence

    final Map<Integer, List<Entity>> sentenceToEntities;
    sentenceToEntities = sentenceToEntities(sentences, copiedEntities);

    // each sentence
    for (final Entry<Integer, String> entry : sentences.entrySet()) {
      final int sentenceID = entry.getKey();
      final String stentence = entry.getValue();

      final Map<Integer, Entity> index = Entity.indexToEntity(sentenceToEntities.get(sentenceID));
      final List<Integer> sorted = new ArrayList<>(new TreeSet<Integer>(index.keySet()));

      final List<CoreLabel> labels = stanford.getLabels(stentence);
      // for all words between two entities
      for (int i = 0; i < (sorted.size() - 1); i++) {
        final Entity s = index.get(sorted.get(i));
        final Entity o = index.get(sorted.get(i + 1));

        final int sStart = Entity.getIndex(s);
        final int oStart = Entity.getIndex(o);

        boolean found = false;
        final List<CoreLabel> pattern = new ArrayList<>();
        for (final CoreLabel coreLabel : labels) {
          if (found && (coreLabel.beginPosition() == oStart)) {
            found = false;
          }
          if (found) {
            pattern.add(coreLabel);
          }
          if (coreLabel.beginPosition() == sStart) {
            found = true;
          }
        } // end for
        final Set<String> relationLabel = new HashSet<String>(checkPattern(pattern));
        for (final String label : relationLabel) {
          try {

            final Relation relation = addRelation(//
                idMap.get(new Integer(s.id)), idMap.get(new Integer(o.id)), label,
                Arrays.asList(new URI(dbpediaOntology.concat(label)))//
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

  private Relation addRelation(//
      final Entity s, final Entity o, final String relationLabel, final List<URI> relation) {

    final float relevance = 0f;
    final String relationByTool = relationLabel;

    return new Relation(s, relationLabel, relationByTool, o, relation, getToolName(), relevance);
  }

  protected List<String> checkPattern(final List<CoreLabel> sentencePath) {
    final List<String> _relations = new ArrayList<>();
    // each pattern
    for (final String pattern : paraphrasesIndex.keySet()) {
      // pattern to token
      final List<String> pattyPatternToken = new ArrayList<>();
      pattyPatternToken.addAll(Arrays.asList(pattern.split(" ")));

      int index = 0;
      int counter = 0;
      for (final String pattyToken : pattyPatternToken) {
        if (index < sentencePath.size()) {
          final CoreLabel coreLabel = sentencePath.get(index);

          if (pattyToken.startsWith("[")) {
            if (pattyToken.contains(ptbToUniPos.get(coreLabel.tag()).toLowerCase())) {
              counter++;
            }
          } else if (pattyToken.equals(coreLabel.originalText())) {
            counter++;
          } else {
            // no match
            break;
          }

          if ((counter == sentencePath.size()) && (counter == pattyPatternToken.size())) {
            _relations.addAll(paraphrasesIndex.get(pattern));
          }
          index++;
          if (index >= sentencePath.size()) {
            break;
          }
        } // end for
      }
    }
    return _relations;
  }
}
