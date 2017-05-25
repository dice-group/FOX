package org.aksw.fox.nerlearner.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.fox.data.EntityClassMap;

public class GermanNERCorpus extends ANERReader {

  protected StringBuilder input = new StringBuilder();
  protected HashMap<String, String> entities = new HashMap<>();

  protected HashMap<String, Set<String>> disambEntities = new HashMap<>();

  Map<String, String> tagsMap = new HashMap<>();

  String sep = "\t";

  boolean debug = false;

  /**
   * Test.
   *
   * @param a
   * @throws IOException
   */
  public static void main(final String[] a) throws IOException {

    final String[] files = new String[1];
    files[0] = "input/GermanNER/full_train.tsv";

    final INERReader r = new GermanNERCorpus(files);

    LOG.info("input:");
    for (final String line : r.getInput().split("\n")) {
      LOG.info(line);
    }

    LOG.info("entities:");
    r.getEntities().entrySet().forEach(LOG::info);
  }

  /**
   * Constructor for loading class.
   */
  public GermanNERCorpus() {}

  /**
   *
   * Constructor.
   *
   * @param inputPaths
   * @throws IOException
   */
  public GermanNERCorpus(final String[] inputPaths) throws IOException {
    initFiles(inputPaths);
  }

  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    super.initFiles(initFiles);

    tagsMap.put("B-PER", EntityClassMap.P);
    tagsMap.put("B-LOC", EntityClassMap.L);
    tagsMap.put("B-ORG", EntityClassMap.O);

    tagsMap.put("I-PER", EntityClassMap.P);
    tagsMap.put("I-LOC", EntityClassMap.L);
    tagsMap.put("I-ORG", EntityClassMap.O);

    tagsMap.put("B-OTH", EntityClassMap.N);
    tagsMap.put("I-OTH", EntityClassMap.N);
    tagsMap.put("O", EntityClassMap.N);

    // all files
    for (int i = 0; i < inputFiles.length; i++) {
      final List<String> lines = Files.readAllLines(inputFiles[i].toPath());

      // all lines
      String lastClass = "";
      final StringBuffer entity = new StringBuffer();

      final int debuglines = 1000;
      int currentLine = 0;
      for (final String line : lines) {
        currentLine++;

        if (debug && (currentLine < debuglines)) {
          if (line.trim().isEmpty()) {
            // new sentence
            input.append(" \n");

          } else {
            final String[] split = line.split(sep);

            if (split.length != 2) {
              LOG.info("Line length wrong, should be 2, but it's: " + split.length);
            }
            if (split.length > 1) {

              final String currentToken = split[0].trim();
              final String currentClass = tagsMap.get(split[1]);

              input.append(currentToken).append(" ");

              if (lastClass.isEmpty()) {
                lastClass = currentClass;
              }

              if (lastClass.equals(currentClass)) {
                if (entity.length() != 0) {
                  entity.append(" ");
                }
                entity.append(currentToken);
              } else {
                if (!(lastClass).equals(EntityClassMap.getNullCategory())) {
                  addE(entity.toString().trim(), lastClass);
                }

                entity.delete(0, entity.length());
                entity.append(currentToken);
                lastClass = currentClass;
              }
            }
          }
        }
      }
    }
    LOG.info("All entities: " + disambEntities.size());
    disambEntities = (HashMap<String, Set<String>>) disambEntities.entrySet().stream()
        .filter(e -> e.getValue().size() == 1)
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    LOG.info("used entities: " + disambEntities.size());

    // disambEntities.entrySet().forEach(LOG::info);

    disambEntities.entrySet()
        .forEach(e -> entities.put(e.getKey(), (e.getValue().iterator().next())));

  }

  protected void addE(final String word, final String classs) {
    if (disambEntities.get(word) == null) {
      disambEntities.put(word, new HashSet<>());
    }
    disambEntities.get(word).add(classs);
  }

  @Override
  public String getInput() {
    return input.toString();
  }

  @Override
  public Map<String, String> getEntities() {
    return entities;
  }
}
