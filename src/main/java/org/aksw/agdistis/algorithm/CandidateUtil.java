package org.aksw.agdistis.algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.aksw.agdistis.datatypes.Document;
import org.aksw.agdistis.datatypes.NamedEntitiesInText;
import org.aksw.agdistis.datatypes.NamedEntityInText;
import org.aksw.agdistis.graph.Node;
import org.aksw.agdistis.util.PreprocessingNLP;
import org.aksw.agdistis.util.Stemming;
import org.aksw.agdistis.util.Triple;
import org.aksw.agdistis.util.TripleIndex;
import org.aksw.agdistis.util.TripleIndexContext;
import org.apache.lucene.search.spell.NGramDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class CandidateUtil {

  private static Logger log = LoggerFactory.getLogger(CandidateUtil.class);
  private final String nodeType;
  private final TripleIndex index;
  private TripleIndexContext index2;
  private final NGramDistance nGramDistance;
  private final CorporationAffixCleaner corporationAffixCleaner;
  private final DomainWhiteLister domainWhiteLister;
  private final boolean popularity;
  private final boolean context;
  private final boolean acronym;
  private final boolean commonEntities;
  private final String algorithm;

  public CandidateUtil(final String file) throws IOException {
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    final InputStream is = loader.getResourceAsStream(file);

    final Properties prop = new Properties();
    prop.load(is);

    nodeType = prop.getProperty("nodeType");
    nGramDistance = new NGramDistance(Integer.valueOf(prop.getProperty("ngramDistance")));
    index = new TripleIndex(file);
    context = Boolean.valueOf(prop.getProperty("context"));
    if (context == true) { // in case the index by context exist
      index2 = new TripleIndexContext();
    }
    corporationAffixCleaner = new CorporationAffixCleaner();
    domainWhiteLister = new DomainWhiteLister(index);
    popularity = Boolean.valueOf(prop.getProperty("popularity"));
    acronym = Boolean.valueOf(prop.getProperty("acronym"));
    commonEntities = Boolean.valueOf(prop.getProperty("commonEntities"));
    algorithm = prop.getProperty("algorithm");
  }

  public void insertCandidatesIntoText(final DirectedSparseGraph<Node, String> graph,
      final Document document, final double threshholdTrigram, final Boolean heuristicExpansionOn)
      throws IOException {
    final NamedEntitiesInText namedEntities = document.getNamedEntitiesInText();
    final String text = document.DocumentText().getText();
    final HashMap<String, Node> nodes = new HashMap<String, Node>();

    // used for heuristic label expansion start with longest Named Entities
    Collections.sort(namedEntities.getNamedEntities(), new NamedEntityLengthComparator());
    Collections.reverse(namedEntities.getNamedEntities());
    String entities = "";
    for (final NamedEntityInText namedEntity : namedEntities) {
      entities = entities.concat(" ".concat(namedEntity.getLabel()));
    }
    log.info("entities" + entities);
    final HashSet<String> heuristicExpansion = new HashSet<String>();
    for (final NamedEntityInText entity : namedEntities) {
      String label = text.substring(entity.getStartPos(), entity.getEndPos());

      log.info("\tLabel: " + label);
      final long start = System.currentTimeMillis();

      if (heuristicExpansionOn) {
        label = heuristicExpansion(heuristicExpansion, label);
      }
      checkLabelCandidates(graph, threshholdTrigram, nodes, entity, label, false, entities);

      log.info("\tGraph size: " + graph.getVertexCount() + " took: "
          + (System.currentTimeMillis() - start) + " ms");
    }
  }

  private String heuristicExpansion(final HashSet<String> heuristicExpansion, String label) {
    String tmp = label;
    boolean expansion = false;
    for (final String key : heuristicExpansion) {
      if (key.contains(label)) {
        // take the shortest possible expansion
        if ((tmp.length() > key.length()) && (tmp != label)) {
          tmp = key;
          expansion = true;
          log.debug("Heuristic expansion: " + label + "-->" + key);
        }
        if ((tmp.length() < key.length()) && (tmp == label)) {
          tmp = key;
          expansion = true;
          log.debug("Heuristic expansion: " + label + "-->" + key);
        }
      }
    }
    label = tmp;
    if (!expansion) {
      heuristicExpansion.add(label);
    }
    return label;
  }

  public void addNodeToGraph(final DirectedSparseGraph<Node, String> graph,
      final HashMap<String, Node> nodes, final NamedEntityInText entity, final Triple c,
      final String candidateURL) throws IOException {
    final Node currentNode = new Node(candidateURL, 0, 0, algorithm);
    log.debug("CandidateURL: " + candidateURL);
    // candidates are connected to a specific label in the text via their
    // start position
    if (!graph.addVertex(currentNode)) {
      final int st = entity.getStartPos();
      if (nodes.get(candidateURL) != null) {
        nodes.get(candidateURL).addId(st);
      } else {
        log.error("This vertex couldn't be added because of an bug in Jung: " + candidateURL);
      }
    } else {
      currentNode.addId(entity.getStartPos());
      nodes.put(candidateURL, currentNode);
    }
  }

  private void checkLabelCandidates(final DirectedSparseGraph<Node, String> graph,
      final double threshholdTrigram, final HashMap<String, Node> nodes,
      final NamedEntityInText entity, String label, final boolean searchInSurfaceForms,
      final String entities) throws IOException {
    List<Triple> candidates = new ArrayList<Triple>();
    List<Triple> acronymCandidatesTemp = new ArrayList<Triple>();
    List<Triple> acronymCandidatesTemp2 = new ArrayList<Triple>();
    List<Triple> candidatesContext = new ArrayList<Triple>();
    final List<Triple> candidatesContextbyLabel = new ArrayList<Triple>();
    final List<Triple> linkedsbyContext = new ArrayList<Triple>();
    int countFinalCandidates = 0;

    final PreprocessingNLP nlp = new PreprocessingNLP();
    // Label treatment
    label = corporationAffixCleaner.cleanLabelsfromCorporationIdentifier(label);
    log.info("Label:" + label);
    label = nlp.Preprocessing(label);
    // label treatment finished ->
    // searchByAcronym
    if (acronym == true) {
      if (label.equals(label.toUpperCase()) && (label.length() <= 4)) {
        acronymCandidatesTemp = searchbyAcronym(label, searchInSurfaceForms, entity.getType());
        for (final Triple triple : acronymCandidatesTemp) {
          acronymCandidatesTemp2 =
              searchAcronymByLabel(triple.getSubject(), searchInSurfaceForms, entity.getType());
          for (final Triple triple2 : acronymCandidatesTemp2) {
            if (nGramDistance.getDistance(triple.getSubject(),
                triple2.getObject()) > threshholdTrigram) {
              // follow redirect
              triple2.setSubject(redirect(triple2.getSubject()));
              // iff it is a disambiguation resource, skip it
              if (isDisambiguationResource(triple2.getSubject())) {
                continue;
              }
              if (commonEntities == true) {
                addNodeToGraph(graph, nodes, entity, triple2, triple2.getSubject());
                countFinalCandidates++;
              } else {
                if (domainWhiteLister.fitsIntoDomain(triple2.getSubject())) {
                  addNodeToGraph(graph, nodes, entity, triple2, triple2.getSubject());
                  countFinalCandidates++;
                }
              }
            }
          }
          acronymCandidatesTemp2.clear();
        }
        log.info("\t\tnumber of candidates by acronym: " + countFinalCandidates);
      }
    }
    // searchByAcronymFinished

    if (countFinalCandidates == 0) {
      candidates = searchCandidatesByLabel(label, searchInSurfaceForms, "", popularity);
      if (searchInSurfaceForms) {
        log.info("\t\tnumber of candidates by SF label: " + candidates.size());
      } else {
        log.info("\t\tnumber of candidates by main label: " + candidates.size());
      }

      if (candidates.size() == 0) {
        log.info("\t\t\tNo candidates for: " + label);
        if (label.endsWith("'s")) {
          // removing plural s
          label = label.substring(0, label.lastIndexOf("'s"));
          candidates = searchCandidatesByLabel(label, searchInSurfaceForms, "", popularity);
          log.info("\t\t\tEven not with expansion");
        } else if (label.endsWith("s")) {
          // removing genitiv s
          label = label.substring(0, label.lastIndexOf("s"));
          candidates = searchCandidatesByLabel(label, searchInSurfaceForms, "", popularity);
          log.info("\t\t\tEven not with expansion");
        }
      }
      // If the set of candidates is still empty, here we apply stemming
      // technique
      if (candidates.isEmpty()) {
        final Stemming stemmer = new Stemming();
        final String temp = stemmer.stemming(label);
        candidates = searchCandidatesByLabel(temp, searchInSurfaceForms, "", popularity);
        log.info("\t\tnumber of all candidates by stemming: " + candidates.size());
      }
      // Here starts the similarity by trigram
      boolean added = false;
      for (final Triple c : candidates) {
        log.info("Candidate triple to check: " + c);
        String candidateURL = c.getSubject();
        String surfaceForm = c.getObject();
        surfaceForm = nlp.Preprocessing(surfaceForm);
        // rule of thumb: no year numbers in candidates
        if (candidateURL.startsWith(nodeType)) {
          // if it is a disambiguation resource, skip it
          // trigram similarity
          if (c.getPredicate().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
            if (nGramDistance.getDistance(surfaceForm, label) < 1.0) {// Here
                                                                      // we
                                                                      // set
                                                                      // the
                                                                      // similarity
                                                                      // as
                                                                      // maximum
                                                                      // because
                                                                      // rfds:label
                                                                      // refers
                                                                      // to
                                                                      // the
                                                                      // main
                                                                      // reference
                                                                      // of
                                                                      // a
                                                                      // given
                                                                      // resource
              continue;
            }
          } else if (!c.getPredicate().equals("http://www.w3.org/2000/01/rdf-schema#label")) { // Here
                                                                                               // the
                                                                                               // similarity
                                                                                               // is
                                                                                               // in
                                                                                               // accordance
                                                                                               // with
                                                                                               // the
                                                                                               // user's
                                                                                               // choice.
            if (nGramDistance.getDistance(surfaceForm, label) < threshholdTrigram) {
              continue;
            }
          }
          // follow redirect
          candidateURL = redirect(candidateURL);
          if (isDisambiguationResource(candidateURL)) {
            log.info("CandidateURL" + candidateURL);
            continue;
          }
          if (commonEntities == true) { // Being able to get all kinds
                                        // of resource not only
                                        // Person, Organization,
                                        // Location
            addNodeToGraph(graph, nodes, entity, c, candidateURL);
            added = true;
            countFinalCandidates++;
          } else {
            if (domainWhiteLister.fitsIntoDomain(candidateURL)) {
              addNodeToGraph(graph, nodes, entity, c, candidateURL);
              added = true;
              countFinalCandidates++;
            }
          }
        }
      }
      // Looking by context starts here.
      if (!added && !searchInSurfaceForms && (context == true)) {
        log.info("searchByContext");
        candidatesContext = searchCandidatesByContext(entities, label); // looking
                                                                        // for
                                                                        // all
                                                                        // entities
                                                                        // together
        log.info("\t\tnumber of candidates by context: " + candidatesContext.size());

        // taking all possibles SF for each resource found.
        if (candidatesContext != null) {
          for (final Triple triple : candidatesContext) {
            final String url = nodeType + triple.getPredicate();
            candidatesContextbyLabel.addAll(searchCandidatesByUrl(url, searchInSurfaceForms));
          }
        }
        // Here, we apply two filters for increasing the quality of
        // possible candidates
        for (final Triple c : candidatesContextbyLabel) {
          log.debug("Candidate triple to check: " + c);
          String candidateURL = c.getSubject();
          String cleanCandidateURL = candidateURL.replace(nodeType, "");
          cleanCandidateURL = nlp.Preprocessing(cleanCandidateURL);
          if (candidateURL.startsWith(nodeType)) {
            // trigram similarity over the URIS
            if (nGramDistance.getDistance(cleanCandidateURL, label) < 0.3) {
              continue;
            }
            // finding direct connections
            for (final Triple temp : candidatesContext) {
              final String candidateTemp = nodeType + temp.getPredicate();
              linkedsbyContext.addAll(searchbyConnections(candidateURL, candidateTemp));
            }
            // Only resources which have connections with others are
            // treated as possible candidates.
            if (linkedsbyContext.size() < 1) {
              continue;
            }
            // follow redirect
            candidateURL = redirect(candidateURL);

            // if it is a disambiguation resource, skip it
            if (isDisambiguationResource(candidateURL)) {
              continue;
            }
            // Enabling more types of entities as the previous step.
            if (commonEntities == true) {
              addNodeToGraph(graph, nodes, entity, c, candidateURL);
              added = true;
              countFinalCandidates++;
            } else {
              if (domainWhiteLister.fitsIntoDomain(candidateURL)) {
                addNodeToGraph(graph, nodes, entity, c, candidateURL);
                added = true;
                countFinalCandidates++;
              }
            }
          }
          linkedsbyContext.clear();
        }
      }
      // Looking for the given label among the set of surface forms.
      if (!added && !searchInSurfaceForms) {
        log.info("Search using SF from disambiguation, redirects and from anchors web pages");
        checkLabelCandidates(graph, threshholdTrigram, nodes, entity, label, true, entities);
      }

    }
    log.info("\t\tnumber of final candidates " + countFinalCandidates);
  }

  private ArrayList<Triple> searchCandidatesByLabel(final String label,
      final boolean searchInSurfaceFormsToo, final String type, final boolean popularity) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    final ArrayList<Triple> tmp2 = new ArrayList<Triple>();
    final ArrayList<Triple> finalTmp = new ArrayList<Triple>();
    ArrayList<Triple> candidatesScore = new ArrayList<Triple>();

    if (popularity) { // Frequency of entities.
      tmp.addAll(index.search(null, "http://www.w3.org/2000/01/rdf-schema#label", label, 500));
      if (searchInSurfaceFormsToo) {
        tmp.clear();
        tmp.addAll(index.search(null, "http://www.w3.org/2004/02/skos/core#altLabel", label, 500));
      }

      for (final Triple c : tmp) {
        tmp2.add(new Triple(c.getSubject(), c.getPredicate(), c.getObject()));
        final String uri = c.getSubject().replace(nodeType, "");
        candidatesScore = searchCandidatesByScore(uri);
        c.setPredicate(c.getObject());
        if (candidatesScore.isEmpty()) {
          c.setObject("1");
        } else {
          c.setObject(candidatesScore.get(0).getObject());
        }
      }

      Collections.sort(tmp);

      if (tmp.size() < 100) {
        for (final Triple triple : tmp.subList(0, tmp.size())) {
          for (final Triple triple2 : tmp2) {
            if (triple.getSubject().equals(triple2.getSubject())
                && triple.getPredicate().equals(triple2.getObject())) {
              finalTmp.add(triple2);
              continue;
            }

          }
        }

      } else if (tmp.size() >= 100) {
        for (final Triple triple : tmp.subList(0, 100)) {
          for (final Triple triple2 : tmp2) {
            if (triple.getSubject().equals(triple2.getSubject())
                && triple.getPredicate().equals(triple2.getObject())) {
              finalTmp.add(triple2);
              continue;
            }

          }
        }

      }
      return finalTmp;
    } else {
      tmp.addAll(index.search(null, "http://www.w3.org/2000/01/rdf-schema#label", label));
      if (searchInSurfaceFormsToo) {
        tmp.clear();
        tmp.addAll(index.search(null, "http://www.w3.org/2004/02/skos/core#altLabel", label));
      }
      return tmp;
    }
  }

  public ArrayList<Triple> searchbyAcronym(final String label,
      final boolean searchInSurfaceFormsToo, final String type) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    tmp.addAll(index.search(null, "http://dbpedia.org/property/acronym", label, 100));
    return tmp;
  }

  public ArrayList<Triple> searchAcronymByLabel(final String label,
      final boolean searchInSurfaceFormsToo, final String type) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    tmp.addAll(index.search(null, "http://www.w3.org/2000/01/rdf-schema#label", label, 100));
    return tmp;
  }

  ArrayList<Triple> searchCandidatesByContext(final String entities, final String label) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    tmp.addAll(index2.search(entities, label, null, 100));

    return tmp;
  }

  ArrayList<Triple> searchCandidatesByScore(final String label) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    tmp.addAll(index2.search(null, label, null));

    return tmp;
  }

  ArrayList<Triple> searchbyConnections(final String uri, final String uri2) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    tmp.addAll(index.search(uri, null, uri2));

    return tmp;
  }

  ArrayList<Triple> searchCandidatesByUrl(final String url, final boolean searchInSurfaceFormsToo) {
    final ArrayList<Triple> tmp = new ArrayList<Triple>();
    final ArrayList<Triple> tmp2 = new ArrayList<Triple>();
    final ArrayList<Triple> finalTmp = new ArrayList<Triple>();
    ArrayList<Triple> candidatesScore = new ArrayList<Triple>();

    if (popularity) {
      tmp.addAll(index.search(url, "http://www.w3.org/2000/01/rdf-schema#label", null, 500));

      for (final Triple c : tmp) {
        tmp2.add(new Triple(c.getSubject(), c.getPredicate(), c.getObject()));
        final String uri = c.getSubject().replace(nodeType, "");
        candidatesScore = searchCandidatesByScore(uri);
        c.setPredicate(c.getObject());
        if (candidatesScore.isEmpty()) {
          c.setObject("1");
        } else {
          c.setObject(candidatesScore.get(0).getObject());
        }
      }

      Collections.sort(tmp);

      if (tmp.size() < 100) {
        for (final Triple triple : tmp.subList(0, tmp.size())) {
          for (final Triple triple2 : tmp2) {
            if (triple.getSubject().equals(triple2.getSubject())
                && triple.getPredicate().equals(triple2.getObject())) {
              finalTmp.add(triple2);
              continue;
            }

          }
        }

      } else if (tmp.size() >= 100) {
        for (final Triple triple : tmp.subList(0, 100)) {
          for (final Triple triple2 : tmp2) {
            if (triple.getSubject().equals(triple2.getSubject())
                && triple.getPredicate().equals(triple2.getObject())) {
              finalTmp.add(triple2);
              continue;
            }

          }
        }

      }
      return finalTmp;
    } else {
      tmp.addAll(index.search(url, "http://www.w3.org/2000/01/rdf-schema#label", null));
      return tmp;
    }
  }

  private boolean isDisambiguationResource(final String candidateURL) {
    final List<Triple> tmp =
        index.search(candidateURL, "http://dbpedia.org/ontology/wikiPageDisambiguates", null);
    if (tmp.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  private String redirect(final String candidateURL) {
    if (candidateURL == null) {
      return candidateURL;
    }
    final List<Triple> redirect =
        index.search(candidateURL, "http://dbpedia.org/ontology/wikiPageRedirects", null);
    if (redirect.size() == 1) {
      return redirect.get(0).getObject();
    } else if (redirect.size() > 1) {
      log.error("Several redirects detected for :" + candidateURL);
      return candidateURL;
    } else {
      return candidateURL;
    }
  }

  public void close() throws IOException {
    index.close();
  }

  public TripleIndex getIndex() {
    return index;
  }

}
