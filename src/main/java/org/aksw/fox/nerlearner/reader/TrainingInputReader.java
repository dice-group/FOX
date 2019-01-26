package org.aksw.fox.nerlearner.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.fox.data.BILOUEncoding;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.nerlearner.TokenManager;
import org.aksw.fox.utils.FoxTextUtil;

/**
 * Reads input training data with ORGANIZATION, LOCATION and PERSON entities.
 *
 * @author rspeck
 *
 */
public class TrainingInputReader extends ANERReader {

  private Map<String, String> entityClassesOracel = null;

  protected StringBuffer taggedInput = new StringBuffer();
  protected String input = "";
  protected HashMap<String, String> entities = new HashMap<>();

  /**
  *
  */
  public static void main(final String[] aa) throws Exception {

    final TrainingInputReader trainingInputReader = new TrainingInputReader();
    trainingInputReader.initFiles("input/4");

    LOG.info("input: \n" + trainingInputReader.getInput());
    LOG.info("oracle: ");
    trainingInputReader.getEntities().entrySet().forEach(LOG::info);
  }

  /**
   * Empty constructor to create class with Reflections.
   */
  public TrainingInputReader() {}

  /**
   * Calls super constructor with inputPaths.
   *
   * @param inputPaths
   * @throws IOException
   */

  public TrainingInputReader(final String[] inputPaths) throws IOException {
    super(inputPaths);
  }

  @Override
  public void initFiles(final String[] initFiles) throws IOException {
    super.initFiles(initFiles);

    readInputFromFiles();
    parse();
  }

  public void initFiles(final String folder) throws IOException {
    final List<String> files = new ArrayList<>();

    final File file = new File(folder);
    if (!file.exists()) {
      throw new IOException("Can't find directory.");
    } else {
      if (file.isDirectory()) {
        // read all files in a directory
        for (final File fileEntry : file.listFiles()) {
          if (fileEntry.isFile() && !fileEntry.isHidden()) {
            files.add(fileEntry.getAbsolutePath());
          }
        }
      } else {
        throw new IOException("Input isn't a valid directory.");
      }
    }

    initFiles(files.toArray(new String[files.size()]));
  }

  /**
   *
   * @return
   * @throws IOException
   */
  @Override
  public String getInput() {
    return input;
  }

  @Override
  public HashMap<String, String> getEntities() {
    // remove oracle entities aren't in input
    final Set<Entity> set = new HashSet<>();

    for (final Entry<String, String> oracleEntry : entities.entrySet()) {
      set.add(new Entity(oracleEntry.getKey(), oracleEntry.getValue()));
    }

    // repair entities (use fox token)
    new TokenManager(input).repairEntities(set);

    // use
    entities.clear();
    for (final Entity e : set) {
      entities.put(e.getText(), e.getType());
    }
    return entities;
  }

  /**
   * Reads PREAMBLE or TEXT tag content to taggedInput.
   *
   **/
  protected void readInputFromFiles() throws IOException {

    for (final File file : inputFiles) {
      final BufferedReader br = new BufferedReader(new FileReader(file));
      String line = "";
      boolean includeLine = false;
      while ((line = br.readLine()) != null) {
        // open
        if (line.contains("<PREAMBLE>")) {
          includeLine = true;
          line = line.substring(line.indexOf("<PREAMBLE>") + "<PREAMBLE>".length());
        } else if (line.contains("<TEXT>")) {
          includeLine = true;
          line = line.substring(line.indexOf("<TEXT>") + "<TEXT>".length());
        }
        // close
        if (includeLine) {
          if (line.contains("</PREAMBLE>")) {
            includeLine = false;
            if (line.indexOf("</PREAMBLE>") > 0) {
              taggedInput.append(line.substring(0, line.indexOf("</PREAMBLE>")) + "\n");
            }

          } else if (line.contains("</TEXT>")) {
            includeLine = false;
            if (line.indexOf("</TEXT>") > 0) {
              taggedInput.append(line.substring(0, line.indexOf("</TEXT>")) + "\n");
            }

          } else {
            taggedInput.append(line + "\n");
          }
        }
      }
      br.close();
    }
  }

  /**
   * Gets the entity class for a oracel entity type/class.
   */
  public String oracel(final String tag) {
    if (entityClassesOracel == null) {
      entityClassesOracel = new HashMap<>();
      entityClassesOracel.put("ORGANIZATION", EntityTypes.O);
      entityClassesOracel.put("LOCATION", EntityTypes.L);
      entityClassesOracel.put("PERSON", EntityTypes.P);
    }
    final String t = entityClassesOracel.get(tag);
    return t == null ? BILOUEncoding.O : t;
  }

  /**
   * Reads entities in taggedInput.
   *
   * @return
   */
  protected String parse() {
    input = taggedInput.toString().replaceAll("<p>|</p>", "");

    while (true) {
      final int openTagStartIndex = input.indexOf("<ENAMEX");
      if (openTagStartIndex == -1) {
        break;

      } else {
        final int openTagCloseIndex = input.indexOf(">", openTagStartIndex);
        final int closeTagIndex = input.indexOf("</ENAMEX>");

        try {
          final String taggedWords = input.substring(openTagCloseIndex + 1, closeTagIndex);
          final String categoriesString = input.substring(
              openTagStartIndex + "<ENAMEX TYPE=\"".length(), openTagCloseIndex - "\"".length());

          final String[] types = categoriesString.split("\\|");
          for (final String type : types) {

            if (!oracel(type).equals(BILOUEncoding.O)) {
              final String[] tokens = FoxTextUtil.getSentenceToken(taggedWords + ".");

              String word = "";
              for (final String token : tokens) {
                if (!word.isEmpty() && token.isEmpty()) {
                  put(word, type);
                  word = "";
                } else {
                  word += token + " ";
                }
              } // end each token

              if (!word.isEmpty()) {
                put(word, type);
              }
            }
          } // end categories

          String escapedCategoriesString = "";
          for (final String type : types) {
            escapedCategoriesString += type + "\\|";
          }

          escapedCategoriesString =
              escapedCategoriesString.substring(0, escapedCategoriesString.length() - 1);

          input = input.replaceFirst("<ENAMEX TYPE=\"" + escapedCategoriesString + "\">", "");
          input = input.replaceFirst("</ENAMEX>", "");

        } catch (final Exception e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }

    while (true) {
      final int openTagStartIndex = input.indexOf("<TIMEX");
      if (openTagStartIndex == -1) {
        break;
      } else {
        final int openTagCloseIndex = input.indexOf(">", openTagStartIndex);
        final String category =
            input.substring(openTagStartIndex + "<TIMEX TYPE=\"".length(), openTagCloseIndex - 1);
        input = input.replaceFirst("<TIMEX TYPE=\"" + category + "\">", "");
        input = input.replaceFirst("</TIMEX>", "");
      }
    }

    input = input.trim();
    // input = input.replaceAll("``|''", "");
    // input = input.replaceAll("\\p{Blank}+", " ");
    // input = input.replaceAll("\n ", "\n");
    // input = input.replaceAll("\n+", "\n");
    // input = input.replaceAll("[.]+", ".");
    return input;
  }

  protected void put(String word, final String classs) {
    word = word.trim();
    if (!word.isEmpty()) {
      if (entities.get(word) != null) {
        if (!entities.get(word).equals(classs) && !entities.get(word).equals(BILOUEncoding.O)) {

          LOG.info("Oracle with a token with diff. annos. No disamb. for now. Ignore token.");
          LOG.info(word + " : " + classs + " | " + entities.get(word));

          entities.put(word, BILOUEncoding.O);
        }
      } else {
        entities.put(word, classs);
      }
    }
  }
}
