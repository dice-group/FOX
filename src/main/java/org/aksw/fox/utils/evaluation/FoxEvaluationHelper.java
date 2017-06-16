package org.aksw.fox.utils.evaluation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.fox.utils.FoxCfg;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import gnu.getopt.Getopt;

@Deprecated
/**
 *
 * Calculates measures for the evaluation.
 *
 * @author rspeck
 *
 */
public class FoxEvaluationHelper {

  public static final String CFG_KEY_CROSSVALIDATION_RUNS =
      CrossValidation.class.getName().concat(".runs");

  protected int folds = 10;
  protected int runs = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

  // columns
  protected int col_run = 0;
  protected int col_classifier = 1;
  protected int col_classs = 2;

  // row index (L,O,P,N)
  protected int row_a = col_classs + 1;
  protected int row_b = row_a + 1;
  protected int row_c = row_b + 1;
  protected int row_d = row_c + 1;

  // measures
  protected int row_recallIndex = row_d + 1;
  protected int row_precisionIndex = row_d + 2;
  protected int row_fscoreIndex = row_d + 3;

  // in folder
  protected String inputFolder = null;

  protected List<String> files = null;

  // out file
  public String outputFile = null;

  public List<List<String>> values = new ArrayList<>();
  public List<List<String>> meanValues = new ArrayList<>();

  /**
   *
   * @param args
   *        <p>
   *        -i for an input directory <br>
   *        (files in csv format with columns:<br>
   *        run, classifier, class, a, b, c, d) <br>
   *        <br>
   *
   *        -o for an output file<br>
   *        </p>
   *
   *        -m `true` for calculating mean values over all classes<br>
   *        `false` for calculation values for each class
   *
   * @throws IOException if something wrong
   */
  public static void main(final String[] args) throws IOException {

    final Getopt getopt = new Getopt("FoxEval", args, "i:x o:x m:x");

    final FoxEvaluationHelper foxEval = new FoxEvaluationHelper();
    boolean meanOn = false;

    int arg = -1;
    while ((arg = getopt.getopt()) != -1) {
      switch (arg) {
        case 'i':
          foxEval.setInputFolder(String.valueOf(getopt.getOptarg()));
          break;
        case 'o':
          foxEval.setOutputFile(String.valueOf(getopt.getOptarg()));
          break;
        case 'm':
          meanOn = (Boolean.valueOf(getopt.getOptarg()));
          break;
      }
    }

    foxEval.getFiles(foxEval.getInputFolder());

    // read data
    foxEval.read();

    // mean cm
    foxEval.meanTable();
    foxEval.values = foxEval.meanValues;

    // add measures
    foxEval.addValues();

    // mean
    if (meanOn) {
      foxEval.mean();
      foxEval.values = foxEval.meanValues;
    }

    // round all measures
    foxEval.round();

    // write
    foxEval.write();
  }

  protected void write() throws IOException {

    final CSVWriter writer =
        new CSVWriter(new FileWriter(outputFile), ',', CSVWriter.NO_QUOTE_CHARACTER);
    for (final List<String> row : values) {
      writer.writeNext(row.toArray(new String[row.size() - 1]));
    }

    writer.close();
  }

  protected void meanHelper(final List<String> list) {
    final List<String> cur = new ArrayList<>();
    cur.add(list.get(col_run));
    cur.add(list.get(col_classifier));
    cur.add(list.get(col_classs));

    for (int i = row_a; i < list.size(); i++) {
      final Double v = Double.valueOf(list.get(i)) / 3D;
      cur.add(v.toString());
    }
    meanValues.add(cur);
  }

  protected void meanTableHelper(final List<String> list) {
    final List<String> cur = new ArrayList<>();
    cur.add(list.get(col_run));
    cur.add(list.get(col_classifier));
    cur.add(list.get(col_classs));

    for (int i = row_a; i < list.size(); i++) {
      final double v = (Double.valueOf(list.get(i)) / runs);
      cur.add(String.valueOf(v));
    }
    meanValues.add(cur);
  }

  // mean over classes except null
  public void mean() {
    meanValues = new ArrayList<>();
    String alg = "";
    final List<String> current = new ArrayList<>();
    for (final List<String> value : values) {
      // header
      if (values.get(0) == value) {
        meanValues.add(values.get(0));
      } else {

        if (!value.get(col_classifier).equals(alg)) {
          alg = value.get(col_classifier);
          // next
          if (current.size() > 0) {
            meanHelper(current);
            current.clear();
          }
        }

        // mean
        final List<String> row = new ArrayList<>();
        row.add(value.get(col_run));
        row.add(value.get(col_classifier));
        row.add(value.get(col_classs));

        // ignore NULL category
        if (!value.get(col_classs).equals(EntityClassMap.N)) {

          for (int i = row_a; i < value.size(); i++) {
            final Double v = Double.valueOf(value.get(i));
            Double vm = 0D;
            if (current.size() > 0) {
              vm = Double.valueOf(current.get(i) == null ? "0" : current.get(i));
            }

            row.add(String.valueOf(v + vm));
          }
          current.clear();
          current.addAll(row);
        }
      }
    }
    meanHelper(current);
    current.clear();
  }

  public void round() {

    final List<List<String>> rounded = new ArrayList<>();
    for (final List<String> value : values) {
      // header
      if (values.get(0) == value) {
        rounded.add(values.get(0));
      } else {
        final List<String> l = new ArrayList<>();
        for (int i = 0; i < row_recallIndex; i++) {
          l.add(value.get(i));
        }
        for (int i = row_recallIndex; i < (value.size() - 2); i++) {
          l.add(String.valueOf(Math.round(Double.valueOf(value.get(i)) * 10000) / 100D));
        }
        l.add(
            String.valueOf(Math.round(Double.valueOf(value.get(value.size() - 2)) * 10000) / 100D));
        l.add(
            String.valueOf(Math.round(Double.valueOf(value.get(value.size() - 1)) * 1000) / 1000D));
        rounded.add(l);
      }
    }
    values = rounded;
  }

  public void meanTable() {
    final List<String> loc = new ArrayList<>();
    final List<String> org = new ArrayList<>();
    final List<String> per = new ArrayList<>();
    final List<String> no = new ArrayList<>();

    String alg = "";
    for (final List<String> value : values) {
      // header
      if (values.get(0) == value) {
        meanValues.add(values.get(0));
      } else {
        if (!value.get(col_classifier).equals(alg)) {
          alg = value.get(col_classifier);

          if (!loc.isEmpty()) {

            meanTableHelper(loc);
            meanTableHelper(org);
            meanTableHelper(per);
            meanTableHelper(no);

            loc.clear();
            org.clear();
            per.clear();
            no.clear();
          }
        }
        List<String> current = null;
        if (value.get(col_classs).equals(EntityClassMap.L)) {
          current = loc;
        }
        if (value.get(col_classs).equals(EntityClassMap.O)) {
          current = org;
        }
        if (value.get(col_classs).equals(EntityClassMap.P)) {
          current = per;
        }
        if (value.get(col_classs).equals(EntityClassMap.N)) {
          current = no;
        }

        if (current.isEmpty()) {
          current.addAll(value);
        } else {
          final List<String> cur = new ArrayList<>();
          cur.add(value.get(col_run));
          cur.add(value.get(col_classifier));
          cur.add(value.get(col_classs));

          for (int i = row_a; i < value.size(); i++) {
            final Double v = Double.valueOf(value.get(i));
            final Double vm = Double.valueOf(current.get(i));
            cur.add(String.valueOf(v + vm));
          }

          current.clear();
          current.addAll(cur);
        }
      }
    }
    meanTableHelper(loc);
    meanTableHelper(org);
    meanTableHelper(per);
    meanTableHelper(no);
  }

  protected void addValues() {
    addRecall();
    addPrecision();
    addFscore();
    // addAccuracy();
    addError();
    addMcc();
  }

  protected double getFP(final List<String> row) {
    final int i = values.indexOf(row);
    if (row.get(col_classs).equals(EntityClassMap.L)) {
      return Double.valueOf(values.get(i + 1).get(row_a))
          + Double.valueOf(values.get(i + 2).get(row_a))
          + Double.valueOf(values.get(i + 3).get(row_a));
    }
    if (row.get(col_classs).equals(EntityClassMap.O)) {
      return Double.valueOf(values.get(i - 1).get(row_b))
          + Double.valueOf(values.get(i + 1).get(row_b))
          + Double.valueOf(values.get(i + 2).get(row_b));
    }
    if (row.get(col_classs).equals(EntityClassMap.P)) {
      return Double.valueOf(values.get(i - 2).get(row_c))
          + Double.valueOf(values.get(i - 1).get(row_c))
          + Double.valueOf(values.get(i + 1).get(row_c));
    }
    if (row.get(col_classs).equals(EntityClassMap.N)) {
      return Double.valueOf(values.get(i - 3).get(row_d))
          + Double.valueOf(values.get(i - 2).get(row_d))
          + Double.valueOf(values.get(i - 1).get(row_d));
    }
    return 0;
  }

  protected double getTP(final List<String> row) {
    if (row.get(col_classs).equals(EntityClassMap.L)) {
      return Double.valueOf(row.get(row_a));
    }
    if (row.get(col_classs).equals(EntityClassMap.O)) {
      return Double.valueOf(row.get(row_b));
    }
    if (row.get(col_classs).equals(EntityClassMap.P)) {
      return Double.valueOf(row.get(row_c));
    }
    if (row.get(col_classs).equals(EntityClassMap.N)) {
      return Double.valueOf(row.get(row_d));
    }
    return 0;
  }

  protected double getFN(final List<String> row) {
    if (row.get(col_classs).equals(EntityClassMap.L)) {
      return Double.valueOf(row.get(row_b)) + Double.valueOf(row.get(row_c))
          + Double.valueOf(row.get(row_d));
    }
    if (row.get(col_classs).equals(EntityClassMap.O)) {
      return Double.valueOf(row.get(row_a)) + Double.valueOf(row.get(row_c))
          + Double.valueOf(row.get(row_d));

    }
    if (row.get(col_classs).equals(EntityClassMap.P)) {
      return Double.valueOf(row.get(row_a)) + Double.valueOf(row.get(row_b))
          + Double.valueOf(row.get(row_d));

    }
    if (row.get(col_classs).equals(EntityClassMap.N)) {
      return Double.valueOf(row.get(row_a)) + Double.valueOf(row.get(row_b))
          + Double.valueOf(row.get(row_c));
    }
    return 0;
  }

  protected double getTN(final List<String> row) {
    final int i = values.indexOf(row);
    if (row.get(col_classs).equals(EntityClassMap.L)) {
      return Double.valueOf(values.get(i + 1).get(row_b))
          + Double.valueOf(values.get(i + 1).get(row_c))
          + Double.valueOf(values.get(i + 1).get(row_d))
          + Double.valueOf(values.get(i + 2).get(row_b))
          + Double.valueOf(values.get(i + 2).get(row_c))
          + Double.valueOf(values.get(i + 2).get(row_d))
          + Double.valueOf(values.get(i + 3).get(row_b))
          + Double.valueOf(values.get(i + 3).get(row_c))
          + Double.valueOf(values.get(i + 3).get(row_d));
    }
    if (row.get(col_classs).equals(EntityClassMap.O)) {
      return Double.valueOf(values.get(i - 1).get(row_a))
          + Double.valueOf(values.get(i - 1).get(row_c))
          + Double.valueOf(values.get(i - 1).get(row_d))
          + Double.valueOf(values.get(i + 1).get(row_a))
          + Double.valueOf(values.get(i + 1).get(row_c))
          + Double.valueOf(values.get(i + 1).get(row_d))
          + Double.valueOf(values.get(i + 2).get(row_a))
          + Double.valueOf(values.get(i + 2).get(row_c))
          + Double.valueOf(values.get(i + 2).get(row_d));
    }
    if (row.get(col_classs).equals(EntityClassMap.P)) {
      return Double.valueOf(values.get(i - 2).get(row_a))
          + Double.valueOf(values.get(i - 2).get(row_b))
          + Double.valueOf(values.get(i - 2).get(row_d))
          + Double.valueOf(values.get(i - 1).get(row_a))
          + Double.valueOf(values.get(i - 1).get(row_b))
          + Double.valueOf(values.get(i - 1).get(row_d))
          + Double.valueOf(values.get(i + 1).get(row_a))
          + Double.valueOf(values.get(i + 1).get(row_b))
          + Double.valueOf(values.get(i + 1).get(row_d));
    }
    if (row.get(col_classs).equals(EntityClassMap.N)) {
      return Double.valueOf(values.get(i - 3).get(row_a))
          + Double.valueOf(values.get(i - 3).get(row_b))
          + Double.valueOf(values.get(i - 3).get(row_c))
          + Double.valueOf(values.get(i - 2).get(row_a))
          + Double.valueOf(values.get(i - 2).get(row_b))
          + Double.valueOf(values.get(i - 2).get(row_c))
          + Double.valueOf(values.get(i - 1).get(row_a))
          + Double.valueOf(values.get(i - 1).get(row_b))
          + Double.valueOf(values.get(i - 1).get(row_c));
    }
    return 0;
  }

  // Matthews correlation coefficient
  protected void addMcc() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("mcc");
        continue;
      }

      final Double tp = Double.valueOf(getTP(row)), fp = Double.valueOf(getFP(row)),
          fn = Double.valueOf(getFN(row)), tn = Double.valueOf(getTN(row));
      final Double d = (tp + fp) * (tp + fn) * (tn + fp) * (tn + fn);

      Double mcc = 0D;
      if (d > 0) {
        mcc = ((tp * tn) - (fp * fn)) / Math.sqrt(d);
      }

      row.add(mcc.toString());
    }
  }

  protected void addError() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("error");
        continue;
      }

      final Double tp = getTP(row), fp = getFP(row), fn = getFN(row), tn = getTN(row);
      Double accuracy = 0D;
      if ((tp + fn + fp + tn) > 0) {
        accuracy = 1D - (Double.valueOf(tp + tn) / Double.valueOf(tp + fn + fp + tn));
      }

      row.add(accuracy.toString());
    }
  }

  // (tp+tn)/(tp+fn+fp+tn)
  protected void addAccuracy() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("accuracy");
        continue;
      }

      final Double tp = getTP(row), fp = getFP(row), fn = getFN(row), tn = getTN(row);
      Double accuracy = 0D;
      if ((tp + fn + fp + tn) > 0) {
        accuracy = Double.valueOf(tp + tn) / Double.valueOf(tp + fn + fp + tn);
      }

      row.add(accuracy.toString());
    }
  }

  // 2*((pre*recall)/(pre+recall))
  protected void addFscore() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("fscore");
        continue;
      }
      final Double precision = Double.valueOf(row.get(row_precisionIndex));
      final Double recall = Double.valueOf(row.get(row_recallIndex));
      if ((precision + recall) > 0) {
        row.add(String.valueOf(
            2 * (Double.valueOf(precision * recall) / Double.valueOf(precision + recall))));
      } else {
        row.add("0");
      }
    }
  }

  // tp/(tp+fp)
  protected void addPrecision() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("precision");
        continue;
      }
      final Double tp = getTP(row), fp = getFP(row);
      Double precision = 0D;
      if (tp > 0) {
        precision = Double.valueOf(tp) / Double.valueOf(tp + fp);
      }

      row.add(precision.toString());
    }
  }

  // tp/(tp+fn)
  protected void addRecall() {
    for (final List<String> row : values) {
      // header
      if (values.get(0) == row) {
        row.add("recall");
        continue;
      }
      final Double tp = getTP(row), fn = getFN(row);

      Double recall = 0D;
      if (tp > 0) {
        recall = tp / Double.valueOf(tp + fn);
      }

      row.add(recall.toString());
    }
  }

  /** read folder to files */
  public List<String> getFiles(final String folder) throws IOException {
    files = new ArrayList<>();
    final File file = new File(folder);

    if (!file.exists()) {
      throw new IOException("Can't find file or directory.");
    } else {
      if (file.isDirectory()) {
        for (final File fileEntry : file.listFiles()) {
          if (fileEntry.isFile() && !fileEntry.isHidden()) {
            files.add(fileEntry.getAbsolutePath());
          }
        }
      } else if (file.isFile()) {
        files.add(file.getAbsolutePath());
      } else {
        throw new IOException("Input isn't a valid file or directory.");
      }
    }
    return files;
  }

  // Reads all files to values
  public void read() throws IOException {

    boolean headeradded = false;
    for (final String file : files) {
      final CSVReader csvReader = new CSVReader(new FileReader(file));
      final String[] header = csvReader.readNext();
      String[] row = null;
      while ((row = csvReader.readNext()) != null) {
        if (!headeradded) {
          values.add(new ArrayList<String>(Arrays.asList(header)));
          headeradded = true;
        }
        if (row.length == header.length) {
          values.add(new ArrayList<String>(Arrays.asList(row)));
        }
      }
      csvReader.close();
    }
  }

  //
  // setter and getter
  //

  /**
   *
   * @return
   */
  public String getOutputFile() {
    return outputFile;
  }

  /**
   *
   * @param outputFile
   */
  public void setOutputFile(final String outputFile) {
    this.outputFile = outputFile;
  }

  /**
   *
   * @return
   */
  public String getInputFolder() {
    return inputFolder;
  }

  /**
   *
   * @param inputFolder
   */
  public void setInputFolder(final String inputFolder) {
    this.inputFolder = inputFolder;
  }

}
