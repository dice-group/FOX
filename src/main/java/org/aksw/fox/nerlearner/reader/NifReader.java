package org.aksw.fox.nerlearner.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class NifReader extends ANERReader {

  protected String input = "";
  protected HashMap<String, String> entities = new HashMap<>();

  private final TurtleNIFParser turtleNIFParser = new TurtleNIFParser();
  private static final String per_file = "data/dbpedia/Person.ser";
  private static final String place_file = "data/dbpedia/Place.ser";
  private static final String org_file = "data/dbpedia/Organisation.ser";

  protected String dbpedia_instance_types_en = "data/dbpedia/instance_types_en.nt";
  static final String dbpediaO = "http://dbpedia.org/ontology/Organisation";
  static final String dbpediaP = "http://dbpedia.org/ontology/Person";
  static final String dbpediaL = "http://dbpedia.org/ontology/Place";
  static Map<String, String> typeMap = new HashMap<>();
  static {

    typeMap.put(dbpediaL, EntityClassMap.L);
    typeMap.put(dbpediaO, EntityClassMap.O);
    typeMap.put(dbpediaP, EntityClassMap.P);
  }

  /**
   *
   * @param aa
   * @throws Exception
   */
  public static void main(final String[] aa) throws Exception {

    final String file = "input/bengal/bengal_hybrid_10000.ttl";

    final String[] files = new String[1];
    files[0] = new File(file).getAbsolutePath();

    final NifReader bn = new NifReader(files);

    LOG.info(bn.getInput().substring(0, 1000));
    bn.getEntities().entrySet().stream().limit(10).forEach(LOG::info);

    /**
     * <code>
    final String[] a = files.toArray(new String[files.size()]);
    
    final INERReader trainingInputReader = new TrainingInputReader(a);
    ANERReader.LOG.info("input: ");
    ANERReader.LOG.info(trainingInputReader.getInput());
    ANERReader.LOG.info("oracle: ");
    for (final Entry<String, String> e : trainingInputReader.getEntities().entrySet()) {
      ANERReader.LOG.info(e.getValue() + "-" + e.getKey());
    }</code>
     *
     */
    LOG.info("done");
  }

  public NifReader(final String[] files) throws IOException {
    super(files);
  }

  public NifReader() {

  }

  @Override
  public String getInput() {
    if (input.isEmpty()) {
      readData();
    }
    return input;
  }

  @Override
  public Map<String, String> getEntities() {
    if (input.isEmpty()) {
      readData();
    }
    return entities;
  }

  /**
   * Deserialize data.
   *
   * @return List of sets with uris of a type (person=0,place=1,org=2).
   */
  protected List<Set<String>> deserialize() {
    Set<String> org = null, person = null, place = null;
    try {
      person = deserialize(per_file);
      place = deserialize(place_file);
      org = deserialize(org_file);
    } catch (final IOException | ClassNotFoundException e) {
      LOG.info(e.getLocalizedMessage(), e);
      return null;
    }
    return Arrays.asList(person, place, org);
  }

  protected void readData() {
    LOG.info("reads input files");
    final List<Document> nifdocs = new ArrayList<>();
    for (final File file : inputFiles) {
      List<String> lines = null;
      try {
        lines = Files.readAllLines(file.toPath());
      } catch (final IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
        LOG.error(file.toPath());
      }
      LOG.info("Bengal file with " + lines.size() + " lines.");
      nifdocs.addAll(turtleNIFParser.parseNIF(String.join(" ", lines)));
    }
    nifdocs.removeIf(p -> nifdocs.indexOf(p) > maxSentences);
    LOG.info("create data");
    final Map<String, String> word2URI = new HashMap<>();
    final Map<String, String> URI2Type = new HashMap<>();
    {
      for (final Document doc : nifdocs) {

        final String text = doc.getText();
        if ((text == null) || text.isEmpty()) {
          continue;
        }
        for (final Marking marking : doc.getMarkings()) {
          if (!(marking instanceof NamedEntity)) {
            continue;
          }
          final NamedEntity ne = (NamedEntity) marking;
          // dbpedia resources
          final Set<String> uris = ne.getUris();
          final String word =
              text.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
          // find type
          // test data
          if (uris.size() > 1) {
            LOG.info("Size > 1:  " + uris);
            LOG.warn("we toke only the first uri into account");
          }
          if (word2URI.get(word) == null) {
            word2URI.put(word, uris.iterator().next());
          } else {
            if (!word2URI.get(word).equals(uris.iterator().next())) {
              LOG.info("Disambiguation: " + word + " " + word2URI.get(word) + " / "
                  + uris.iterator().next());
              LOG.warn("we ignore disambiguation.");
              word2URI.put(word, "Disambiguation");
            }
          }
          word2URI.values().removeAll(Collections.singleton("Disambiguation"));
        } // markings
      }
    }
    LOG.info("number of markings: " + word2URI.size());
    word2URI.entrySet().stream().limit(10).forEach(LOG::info);
    // TODO: find out what kind of annotations we have? atm we are using NamedEntity
    LOG.info("reads type info files");
    // set URI2Type
    final Map<String, Set<String>> dbpediaTypes = dbpadiaUriToTypes(dbpedia_instance_types_en);
    dbpediaTypes.entrySet().stream().limit(10).forEach(LOG::info);
    final List<Set<String>> deserializetypes = deserialize();
    {
      for (final Entry<String, String> entry : word2URI.entrySet()) {
        final String uri = entry.getValue();
        String currentType = "";
        final Set<String> currentTypes = dbpediaTypes.get(uri);
        if (currentTypes != null) {
          if (currentTypes.contains(dbpediaP)) {
            currentType = dbpediaP;
          } else if (currentTypes.contains(dbpediaL)) {
            currentType = dbpediaL;
          } else if (currentTypes.contains(dbpediaO)) {
            currentType = dbpediaO;
          }
        } else {
          if (deserializetypes.get(0).contains(uri)) {
            currentType = dbpediaP;
          } else if (deserializetypes.get(1).contains(uri)) {
            currentType = dbpediaL;
          } else if (deserializetypes.get(2).contains(uri)) {
            currentType = dbpediaO;
          }
        }
        URI2Type.put(uri, currentType);
      }
      URI2Type.values().removeAll(Collections.singleton(""));
    }

    LOG.info("found " + URI2Type.size() + "/"
        + (word2URI.values().stream().collect(Collectors.toSet())).size());

    // read data
    final StringBuffer in = new StringBuffer();

    for (final Document doc : nifdocs) {

      final String text = doc.getText();
      if ((text == null) || text.isEmpty()) {
        continue;
      }
      in.append(text).append(" ");
      for (final Marking marking : doc.getMarkings()) {
        if (!(marking instanceof NamedEntity)) {
          continue;
        }
        final NamedEntity ne = (NamedEntity) marking;

        final Set<String> types = new HashSet<>();
        // dbpedia resources
        for (final String uri : ne.getUris()) {
          types.add(URI2Type.get(uri));
        }

        types.retainAll(typeMap.keySet());

        // LOG.info(types);
        if (types.size() == 1) {
          final String word =
              text.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
          String type = EntityClassMap.getNullCategory();
          // find type

          if (types.contains(dbpediaL)) {
            type = EntityClassMap.L;
          } else if (types.contains(dbpediaO)) {
            type = EntityClassMap.O;
          } else if (types.contains(dbpediaP)) {
            type = EntityClassMap.P;
          }
          // check and add data
          if (type.equals(EntityClassMap.getNullCategory())) {
            continue;
          }
          if (entities.get(word) == null) {
            entities.put(word, type);
          } else {
            if (!entities.get(word).equals(type)) {
              // LOG.info(word + " " + type + " and " + entities.get(word));
            }
          }
        }
      } // markings
    }
    input = in.toString();
  }

  /**
   * <code>
    protected void _readData() {

      int notfound = 0;
      final Set<String> notFoundUris = new HashSet<>();
      for (final Document doc : nifdocs) {

        final String text = doc.getText();
        if ((text != null) && !text.isEmpty()) {
          in.append(text).append(" ");

          final List<Marking> markings = doc.getMarkings();

          for (final Marking m : markings) {
            if (m instanceof NamedEntity) {
              String type = "";

              // dbpedia types
              final Set<String> uris = ((NamedEntity) m).getUris();

              for (final String uri : uris) {
                final Set<String> types = uriToTypes.get(uri);
                if ((types != null) && !types.isEmpty()) {

                  if (types.contains("http://dbpedia.org/ontology/Organisation")) {
                    type = EntityClassMap.O;
                  } else

                  if (types.contains("http://dbpedia.org/ontology/Person")) {
                    type = EntityClassMap.P;
                  } else

                  if (types.contains("http://dbpedia.org/ontology/Place")) {
                    type = EntityClassMap.L;
                  }
                }
              }

              if (type.isEmpty()) {
                //
                boolean found = checkType(((NamedEntity) m), person);;
                if (found) {
                  type = EntityClassMap.P;
                } else {
                  found = checkType(((NamedEntity) m), place);
                }
                if (found) {
                  type = EntityClassMap.L;
                } else {
                  found = checkType(((NamedEntity) m), org);
                }

                if (found) {
                  type = EntityClassMap.O;
                } else {
                  type = EntityClassMap.getNullCategory();
                }
              }

              if (uris.isEmpty()) {
                LOG.info(m);
              } else {
                LOG.info(uris);
              }

              final String word = text.substring(((NamedEntity) m).getStartPosition(),
                  ((NamedEntity) m).getStartPosition() + ((NamedEntity) m).getLength());

              if (type.equals(EntityClassMap.getNullCategory())) {
                if (((NamedEntity) m).getUris().size() == 0) {
                  LOG.info(doc.getDocumentURI());
                  LOG.warn(word + " not found. . . " + ((NamedEntity) m).getUris().size());
                  notFoundUris.addAll(((NamedEntity) m).getUris());
                  notfound++;
                }
              } else {

              }

            } else {
              LOG.error("Entity type in file has changed from `NamedEntity`.  ");
            }
          }
        }
      }
      // entities.entrySet().forEach(LOG::info);

      LOG.info("found: " + entities.entrySet().size() + " notFound: " + notfound + " uris: "
          + notFoundUris.size());

    }
  </code>
   */
  /**
   * Deserializes sets of strings with uris of resources with the same type.
   *
   * @param file path to file
   * @return set of strings containing uris of resources with the same type.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  protected Set<String> deserialize(final String file) throws IOException, ClassNotFoundException {
    final FileInputStream door = new FileInputStream(file);
    final ObjectInputStream reader = new ObjectInputStream(door);
    @SuppressWarnings("unchecked")
    final Set<String> types = ((HashSet<String>) reader.readObject());
    reader.close();
    return types;
  }

  /**
   * Triples separated with a space in the input file.
   *
   * @param dbpedia_instance_types_en triples separated with a space in the input file
   * @return URI to types map
   */
  protected Map<String, Set<String>> dbpadiaUriToTypes(final String file) {

    // read dbpedia
    List<String> dbpediaTypes = null;
    final Map<String, Set<String>> uriToTypes = new HashMap<>();
    try {
      dbpediaTypes = Files.readAllLines(Paths.get(file));
    } catch (final IOException ee) {
      LOG.info(ee.getLocalizedMessage(), ee);
    }

    if (dbpediaTypes != null) {
      dbpediaTypes.forEach(line -> {
        if (!line.startsWith("#")) {
          final String[] split = line.split(" ");
          if (split.length > 2) {
            final String uri = split[0].replaceAll("<", "").replaceAll(">", "").trim();
            final String type = split[2].replaceAll("<", "").replaceAll(">", "").trim();
            if (uriToTypes.get(uri) == null) {
              uriToTypes.put(uri, new HashSet<>());
            }
            uriToTypes.get(uri).add(type);
          } else {
            LOG.warn("Split on space to less in line: " + line);
          }
        } else {
          LOG.warn("Line starts with #: " + line);
        }
      });
    }
    return uriToTypes;
  }
}
