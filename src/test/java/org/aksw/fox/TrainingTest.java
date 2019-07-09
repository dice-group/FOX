package org.aksw.fox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.FoxParameter;
import org.aksw.fox.nerlearner.FoxClassifier;
import org.aksw.fox.nerlearner.FoxClassifierFactory;
import org.aksw.fox.nerlearner.reader.INERReader;
import org.aksw.fox.tools.NERTools;
import org.aksw.fox.tools.ToolsGenerator;
import org.aksw.fox.ui.FoxCLI;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

class TrainingTestReader implements INERReader {

  final String bob = "Bob Right";
  final String alice = "Alice Right";
  final String karl = "Karl Mueller";
  final String paula = "Paula Petersen";

  final String sentenceA = bob.concat(" married ").concat(alice).concat(".");
  final String sentenceB = karl.concat(" and ").concat(paula).concat(" are married.");

  final String text = sentenceA.concat(" ").concat(sentenceB);

  Map<String, String> entities = new HashMap<>();

  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String input() {
    return text;
  }

  private Entity newEntity(final String text, final String type, final int index) {
    return new Entity(text, type, 0, "tool", index);
  }

  @Override
  public Map<String, String> getEntities() {
    if (entities.isEmpty()) {

      final Entity eBob = newEntity(bob, EntityTypes.P, text.indexOf(bob));
      final Entity eAlice = newEntity(alice, EntityTypes.P, text.indexOf(alice));

      final Entity eKarl = newEntity(karl, EntityTypes.P, text.indexOf(karl));
      final Entity ePaula = newEntity(paula, EntityTypes.P, text.indexOf(paula));

      final List<Entity> e = new ArrayList<>();
      e.add(eBob);
      e.add(eAlice);
      e.add(eKarl);
      e.add(ePaula);

      entities.put(karl, EntityTypes.L);
      entities.put(alice, EntityTypes.L);
      entities.put(paula, EntityTypes.L);
      entities.put(bob, EntityTypes.L);
    }
    return entities;
  }

  @Override
  public List<Entity> entities() {
    // TODO Auto-generated method stub
    return null;
  }
}


public class TrainingTest {

  static {
    PropertyConfigurator.configure("data/fox/log4j.properties");
    PropertiesLoader.setPropertiesFile("fox.properties");
  }
  public static Logger LOG = LogManager.getLogger(TrainingTest.class);

  final static String lang = "en";

  public static void main(final String[] a) {
    TrainingTest.retrieve();
    // TrainingTest.training();
  }

  public static void training() {

    final ToolsGenerator toolsGenerator = new ToolsGenerator();
    final NERTools foxNERTools = toolsGenerator.getNERTools(lang);

    final FoxClassifier foxClassifier = new FoxClassifier();

    final Set<String> toolNames = foxNERTools.getToolResult().keySet();
    final String[] prefix = toolNames.toArray(new String[toolNames.size()]);

    LOG.info("tools used: " + toolNames);
    try {
      setClassifier(foxClassifier, prefix);
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // read training data
    final INERReader reader = new TrainingTestReader();

    LOG.info(reader.input());
    LOG.info(reader.getEntities());

    final String input = reader.input();
    final Map<String, String> oracle = reader.getEntities();

    // retrieve entities (tool results)
    foxNERTools.setTraining(true);
    foxNERTools.getEntities(input);

    try {
      foxClassifier.training(input, foxNERTools.getToolResult(), oracle);

      final String classifierFile = FoxCLI.getName(lang);

      foxClassifier.writeClassifier(classifierFile);
      foxClassifier.eva();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public static void retrieve() {

    final INERReader reader = new TrainingTestReader();

    final IFox fox = new Fox(lang);
    final Map<String, String> para = FoxParameter.getDefaultParameter();

    String input = reader.input();
    input = input + " " + input;

    para.put("input", input);
    fox.setParameter(para);
    fox.run();

    final String rdf = fox.getResultsAndClean();
    LOG.info(rdf);

  }

  private static void setClassifier(final FoxClassifier foxClassifier, final String[] prefix)
      throws IOException {
    switch (PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER).trim()) {
      case "result_vote": {
        foxClassifier.setIsTrained(true);
        foxClassifier.setClassifier(FoxClassifierFactory.getClassifierResultVote(prefix));
        break;
      }
      case "class_vote": {
        foxClassifier.setIsTrained(true);
        foxClassifier.setClassifier(FoxClassifierFactory.getClassifierClassVote(prefix));
        break;
      }
      default:
        foxClassifier.setClassifier(
            FoxClassifierFactory.get(PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER),
                PropertiesLoader.get(FoxClassifier.CFG_KEY_LEARNER_OPTIONS)));
    }
  }
}
