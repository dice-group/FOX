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
import org.aksw.simba.knowledgeextraction.commons.dbpedia.DBpedia;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;

import edu.stanford.nlp.ling.CoreLabel;

/**
 * Uses the patty patterns in 'dbpedia-relation-paraphrases.txt' to extract relations.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class PattyEN extends AbstractRE {

  protected final StanfordPipe stanford = StanfordPipe.getStanfordPipe();

  protected Map<String, Set<String>> paraphrases = null;
  // pattern to relation,e.g. married; -> spouse,birthplace
  protected Map<String, Set<String>> paraphrasesIndex = null;

  protected Map<String, String> ptbToUniPos = null;

  public static void main(final String[] a) {
    final String paraphrases = "data/patty/dbpedia-relation-paraphrases.txt";
    final String posTagMap = "data/patty/en-ptb.map";
    new PattyEN(paraphrases, posTagMap);
  }

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
   *
   * @param posTagMapFile
   * @return
   */
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
   */
  private Map<String, Set<String>> readDBpediaRelationParaphrases(final String paraphrasesFile) {

    if (paraphrases == null) {
      paraphrases = new ConcurrentHashMap<>();
      paraphrasesIndex = new ConcurrentHashMap<>();
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

              paraphrases.computeIfAbsent(split[0], k -> new HashSet<>());
              paraphrasesIndex.computeIfAbsent(pattern, k -> new HashSet<>());

              paraphrases.get(split[0]).add(pattern);
              paraphrasesIndex.get(pattern).add(split[0]);

            });

      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return paraphrases;
  }

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
      final List<Integer> sorted = new ArrayList<>(new TreeSet<>(index.keySet()));

      final List<CoreLabel> labels = stanford.getLabels(stentence);
      // for all words between two entities
      for (int i = 0; (i + 1) < sorted.size(); i++) {
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
          if (coreLabel.endPosition() == (sStart + s.getText().length())) {
            found = true;
          }
        } // end for

        final Set<String> relationLabel = new HashSet<>(checkPattern(pattern));
        for (final String label : relationLabel) {

          // check if domain and range match the relation
          final Entity sEntity = idMap.get(s.id);
          final Entity oEntity = idMap.get(o.id);

          final String sType = mapFoxTypesToDBpediaTypes(sEntity.getType());
          final String p = DBpedia.ns_dbpedia_ontology.concat(label);
          final String oType = mapFoxTypesToDBpediaTypes(oEntity.getType());

          if (checkDomainRange(sType, p, oType)) {
            try {
              final Relation relation = addRelation(//
                  sEntity, oEntity, label, Arrays.asList(new URI(p))//
              );

              relations.add(relation);
            } catch (final URISyntaxException e) {
            }
          }
        }
      }
    }
    return relations;
  }

  /**
   * Helper class to create a Relation instance.
   *
   * @param s
   * @param o
   * @param relationLabel
   * @param relation
   * @return Relation instance
   */
  private Relation addRelation(//
      final Entity s, final Entity o, final String relationLabel, final List<URI> relation) {

    final float relevance = 0f;
    final String relationByTool = relationLabel;

    return new Relation(s, relationLabel, relationByTool, o, relation, getToolName(), relevance);
  }

  /**
   * Compares the patty pattern with the CoreLabels that come from Stanfords NLP Tool. In case it is
   * a match, the method returns the relation that is associated by Patty.
   *
   * @param sentencePath
   * @return e.g. {spouse, relative,...}
   */
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

  /**
   * Needs an update.
   *
   * @return
   */
  /**
   * <code>
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
   }</code>
   */

}
