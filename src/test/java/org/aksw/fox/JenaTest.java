package org.aksw.fox;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.Relation;
import org.aksw.fox.output.FoxJena;
import org.aksw.fox.output.IFoxJena;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class JenaTest {
  public final static Logger LOG = LogManager.getLogger(JenaTest.class);

  // TODO: implement as a test
  public static void main(final String args[]) {

    final IFoxJena foxJena = new FoxJena();

    final DataTestFactory dtf = new DataTestFactory();
    final List<Entity> entities = new ArrayList<>(dtf.getTestEntities().values().iterator().next());
    final Set<Relation> relations = dtf.getTestRelations().entrySet().iterator().next().getValue();

    final String input = dtf.getTestEntities().entrySet().iterator().next().getKey();
    final String end = DatatypeConverter.printDateTime(new GregorianCalendar());
    final String start = DatatypeConverter.printDateTime(new GregorianCalendar(2019, 1, 1, 12, 0));

    foxJena.addInput(input, "");
    foxJena.addEntities(entities, start, end, "na", "na");
    foxJena.addRelations(relations, start, end, "na", "na");

    LOG.info("Jena model: \n\n" + foxJena.print());
  }
}
