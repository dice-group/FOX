package org.aksw.fox.webservice.oke;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.Relation;
import org.aksw.fox.output.FoxJena;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.Pair;
import spark.Request;
import spark.Response;

public class Oke {

  public static Logger LOG = LogManager.getLogger(Oke.class);

  final String turtleContentType = "application/x-turtle";

  final OKEDataReader reader = new OKEDataReader();
  final DBpediaQuery db = new DBpediaQuery();

  /**
   *
   * Constructor.
   *
   */
  public Oke() {

  }

  protected List<Entity> getEntities(final Document doc) {
    final List<Marking> markings = doc.getMarkings();
    final String text = doc.getText();

    final List<Entity> entities = new ArrayList<>();

    for (final Marking marking : markings) {
      if (marking instanceof TypedNamedEntity) {

        final TypedNamedEntity ee = (TypedNamedEntity) marking;

        String type = "";
        Set<String> types = ee.getTypes();
        types = types.stream().map(uri -> uri.toLowerCase().substring(uri.lastIndexOf('/') + 1))
            .collect(Collectors.toSet());

        if (types.contains("location") || types.contains("place")) {
          type = EntityTypes.L;
        } else if (types.contains("organization") || types.contains("organisation")) {
          type = EntityTypes.O;
        } else if (types.contains("person")) {
          type = EntityTypes.P;
        } else {
          LOG.warn("Somthing went wrong!");
          LOG.warn(ee.toString());
        }

        final String surface =
            text.substring(ee.getStartPosition(), ee.getStartPosition() + ee.getLength());

        final Entity e = new Entity(surface, type);
        e.addIndicies(ee.getStartPosition());

        String uri = "";
        for (final String u : ee.getUris()) {
          if (u.startsWith("http://dbpedia.org/resource/")) {
            uri = u;
            break;
          }
        }
        e.setUri(uri);
        entities.add(e);
      }

    }
    LOG.info("Entities in doc: ");
    entities.forEach(LOG::info);
    return entities;
  }

  public String reTask(final Request req, final Response res) {

    final String turtleDoc = req.body();

    return reTask(turtleDoc);

  }

  public String reTask(final String turtleDoc) {

    try {
      final List<Document> docs = reader.parseInput(turtleDoc);

      final InputStream stream =
          new ByteArrayInputStream(turtleDoc.getBytes(StandardCharsets.UTF_8));
      Model graph = ModelFactory.createDefaultModel();
      graph = graph.read(stream, "", "Turtle");
      final FoxJena jena = new FoxJena(graph);

      // each doc
      for (int ii = 0; ii < docs.size(); ii++) {

        final Set<Relation> relations = new HashSet<>();
        final Document doc = docs.get(ii);
        //
        final List<Entity> entities = getEntities(doc);
        final Map<Integer, Entity> index = Entity.indexToEntity(entities);
        final List<Integer> sorted = new ArrayList<>(new TreeSet<>(index.keySet()));

        for (int i = 0; i < sorted.size() - 1; i++) {
          final Entity s = index.get(sorted.get(i));
          final Entity o = index.get(sorted.get(i + 1));

          final Pair<Set<String>, Set<String>> pair = db.query(s.getUri(), o.getUri());
          if (pair.first != null) {
            for (final String r : pair.first) {
              if (reader.relations.contains(r)) {

                final List<URI> relation = new ArrayList<>();
                try {
                  relation.add(new URI("http://dbpedia.org/ontology/" + r));
                  final Relation foxRelation = addRelation(s, o, r, relation);

                  relations.add(foxRelation);
                } catch (final URISyntaxException e) {
                  LOG.error(e.getMessage(), e);
                }
              }
            } // end inner for
          }
        }
        jena._addRelations(relations, graph);
      } // end each doc
      return jena.print();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return "";
  }

  private Relation addRelation(//
      final Entity s, final Entity o, final String relationLabel, final List<URI> relation) {

    final float relevance = 0f;
    final String tool = "baseline";
    final String relationByTool = relationLabel;

    return new Relation(s, relationLabel, relationByTool, o, relation, tool, relevance);
  }
}
