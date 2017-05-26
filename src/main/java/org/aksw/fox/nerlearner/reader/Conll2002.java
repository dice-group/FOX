package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FileUtil;

public class Conll2002 extends ANERReader {

  protected StringBuilder input = new StringBuilder();

  protected HashMap<String, String> entities = new HashMap<>();

  protected HashMap<String, Set<String>> disambEntities = new HashMap<>();

  Map<String, String> tagsMap = new HashMap<>();

  /**
   * Test.
   *
   * @param a
   * @throws IOException
   */
  public static void main(final String[] a) throws IOException {

    final String[] files = new String[3];
    files[0] = "input/conll2002/esp.train.gz";
    files[1] = "input/conll2002/esp.testa.gz";
    files[2] = "input/conll2002/esp.testb.gz";

    files[0] = "input/conll2002/ned.train.gz";
    files[1] = "input/conll2002/ned.testa.gz";
    files[2] = "input/conll2002/ned.testb.gz";

    final INERReader r = new Conll2002(files);

    LOG.info("input:\n" + r.getInput());

    LOG.info("entities:");
    r.getEntities().entrySet().forEach(LOG::info);
  }

  /**
   * Constructor for loading class.
   */
  public Conll2002() {}

  /**
   *
   * Constructor.
   *
   * @param inputPaths
   * @throws IOException
   */
  public Conll2002(final String[] inputPaths) throws IOException {
    // [I-MISC, B-LOC, I-PER, B-PER, I-LOC, B-MISC, I-ORG, B-ORG, O]
    tagsMap.put("B-PER", EntityClassMap.P);
    tagsMap.put("B-LOC", EntityClassMap.L);
    tagsMap.put("B-ORG", EntityClassMap.O);

    tagsMap.put("I-PER", EntityClassMap.P);
    tagsMap.put("I-LOC", EntityClassMap.L);
    tagsMap.put("I-ORG", EntityClassMap.O);

    tagsMap.put("O", EntityClassMap.N);
    tagsMap.put("I-MISC", EntityClassMap.N);
    tagsMap.put("B-MISC", EntityClassMap.N);

    initFiles(inputPaths);
  }

  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    super.initFiles(initFiles);
    parse();
  }

  public void parse() {

    final List<String> lines = new ArrayList<>();
    for (int i = 0; i < inputFiles.length; i++) {
      final List<String> l = FileUtil.gunzipItToList(inputFiles[i].getAbsolutePath());
      if (!l.isEmpty()) {
        if (!lines.isEmpty()) {
          lines.add("");
        }
        lines.addAll(l);
      }
    }

    //
    // all lines
    String lastClass = "";
    final StringBuffer word = new StringBuffer();
    for (final String line : lines) {
      final String[] split = line.split(" ");

      if (split.length < 2) {
        input.append("\n");
      }
      if (split.length > 1) {

        final String currentToken = split[0];
        final String currentClass = tagsMap.get(split[split.length - 1].trim());

        if (currentClass == null) {
          LOG.info("currentClass is NULL for the given line: " + line);
        }
        input.append(currentToken).append(" ");

        if (lastClass.isEmpty()) {
          lastClass = currentClass;
        }

        if (lastClass.equals(currentClass)) {
          if (word.length() != 0) {
            word.append(" ");
          }
          word.append(currentToken);
        } else {
          if (!(lastClass).equals(EntityClassMap.getNullCategory())) {
            addE(word.toString().trim(), lastClass);
          }

          word.delete(0, word.length());
          word.append(currentToken);
          lastClass = currentClass;
        }
      }
    }

    LOG.info("All entities: " + disambEntities.size());
    disambEntities = (HashMap<String, Set<String>>) disambEntities.entrySet().stream()
        .filter(e -> e.getValue().size() == 1)
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    LOG.info("used entities: " + disambEntities.size());

    disambEntities.entrySet()
        .forEach(e -> entities.put(e.getKey(), e.getValue().iterator().next()));

  }

  protected void addE(final String word, final String classs) {
    if (disambEntities.get(word) == null) {
      disambEntities.put(word, new HashSet<>());
    }
    disambEntities.get(word).add(classs);
  }

  @Override
  public String getInput() {
    return input.toString().trim();
  }

  @Override
  public Map<String, String> getEntities() {
    return entities;
  }
}
