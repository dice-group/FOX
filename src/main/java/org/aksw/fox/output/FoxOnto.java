package org.aksw.fox.output;

import java.nio.file.Paths;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * <code>  http://owlapi.sourceforge.net/owled2011_tutorial.pdf

    https://github.com/phillord/owl-api/blob/master/contract/src/test/java/org/coode/owlapi/examples/Examples.java
    </code>
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class FoxOnto {

  public static Logger LOG = LogManager.getLogger(FoxOnto.class);

  String name = "http://ns.aksw.org/fox/ontology";
  public static final IRI foxIRI = IRI.create("http://ns.aksw.org/fox/fox.owl");

  public static void main(final String[] a) {
    final FoxOnto foxOnto = new FoxOnto();
    foxOnto.hashCode();

  }

  public FoxOnto() {

    final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    final OWLDataFactory df = OWLManager.getOWLDataFactory();
    OWLOntology o = null;
    try {
      o = m.createOntology(foxIRI);
    } catch (final OWLOntologyCreationException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    final OWLClass clsT = df.getOWLClass(IRI.create(name + "#Thing"));
    final OWLClass clsP = df.getOWLClass(IRI.create(name + "#Person"));
    final OWLSubClassOfAxiom axiom = df.getOWLSubClassOfAxiom(clsP, clsT);
    m.applyChange(new AddAxiom(o, axiom));

    try {

      m.saveOntology(o,
          IRI.create("file:".concat(Paths.get("./fox.owl").toFile().getAbsolutePath().toString())));
    } catch (final OWLOntologyStorageException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
