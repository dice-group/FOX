package org.aksw.fox.utils.evaluation.entitiyBased;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.data.BILOUEncoding;
import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;

import au.com.bytecode.opencsv.CSVWriter;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

// TODO: clean up this quick n dirty code
@Deprecated
// a,b,c,d
// LOCATION
// ORGANIZATION
// PERSON
// NULL
public class DetailCSV {

  // need to be set
  // private static final int classindex = 4;
  public static int classindex = 16;
  // need to be set
  public static String alg = "Vote";
  // need to be set
  public static String folder = "evaluation/entityBased/1/" + alg + "/tmp";

  // need to be set
  public static final String CFG_KEY_CROSSVALIDATION_RUNS =
      CrossValidation.class.getName().concat(".runs");
  protected static int runs = Integer.valueOf(PropertiesLoader.get(CFG_KEY_CROSSVALIDATION_RUNS));

  //
  static int currentRow = 0;

  static Map<String, Integer> defaultmap = new HashMap<>();
  static Map<String, Map<String, Integer>> matrix = new HashMap<>();
  static {
    defaultmap.put(EntityTypes.L, 0); // a
    defaultmap.put(EntityTypes.O, 0); // b
    defaultmap.put(EntityTypes.P, 0); // c
    defaultmap.put(BILOUEncoding.O, 0); // d
  }

  static List<String> classifiedClasses = new ArrayList<>();

  static StringBuffer csv_out = null;

  static int nullRows = 0; // just
                           // debugging

  public static void init() {
    matrix.put(EntityTypes.L, new HashMap<>(defaultmap));
    matrix.put(EntityTypes.O, new HashMap<>(defaultmap));
    matrix.put(EntityTypes.P, new HashMap<>(defaultmap));
    matrix.put(BILOUEncoding.O, new HashMap<>(defaultmap));

    currentRow = 0;
    nullRows = 0;
  }

  public static void main(final String[] args) throws Exception {

    String lastOracleClass = "";
    String lastRun = "";
    String lastFold = "";

    final Instances oracel = new DataSource(folder + "/training.arff").getDataSet();
    for (int run = 1; run <= runs; run++) {
      init();
      lastRun = run + "";

      for (int i = 1; i <= 10; i++) {
        final Instances part =
            new DataSource(folder + "/classified_" + run + "_" + i + ".arff").getDataSet();
        lastFold = i + "";
        // all instances for one fold
        for (int ii = 0; ii < part.numInstances(); ii++) {
          final String classifiedClass = part.instance(ii).stringValue(classindex);
          final String oracelclasss = oracel.instance(currentRow).stringValue(classindex);
          if (oracelclasss.equals(BILOUEncoding.O)) {
            nullRows++;
          }
          currentRow++;

          //
          if (lastOracleClass.equals(oracelclasss) && !oracelclasss.equals(BILOUEncoding.O)
              && ii < part.numInstances() - 1) {
            classifiedClasses.add(classifiedClass);
          } else {

            boolean correct = true;
            for (final String c : classifiedClasses) {
              if (!c.equals(lastOracleClass)) {
                correct = false;
                break;
              }
            }
            classifiedClasses.clear();

            //
            if (correct) {
              Integer count = matrix.get(oracelclasss).get(classifiedClass);
              count++;
              matrix.get(oracelclasss).put(classifiedClass, count);
            } else {
              Integer count = matrix.get(oracelclasss).get(BILOUEncoding.O);
              count++;
              matrix.get(oracelclasss).put(BILOUEncoding.O, count);
            }
            // prepare next
            lastOracleClass = oracelclasss;
          }
          // System.out.println(classifiedClass + ":" + oracelclasss);
        } // fold file

        // System.out.println(matrix);

        // a,b,c,d
        // LOCATION
        // ORGANIZATION
        // PERSON
        // NULL
        writeBuffer(lastRun, lastFold, alg, EntityTypes.L,
            matrix.get(EntityTypes.L).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.L).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.L).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.L).get(BILOUEncoding.O).toString());
        writeBuffer(lastRun, lastFold, alg, EntityTypes.O,
            matrix.get(EntityTypes.O).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.O).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.O).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.O).get(BILOUEncoding.O).toString());
        writeBuffer(lastRun, lastFold, alg, EntityTypes.P,
            matrix.get(EntityTypes.P).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.P).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.P).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.P).get(BILOUEncoding.O).toString());
        writeBuffer(lastRun, lastFold, alg, BILOUEncoding.O,
            matrix.get(BILOUEncoding.O).get(EntityTypes.L).toString(),
            matrix.get(BILOUEncoding.O).get(EntityTypes.O).toString(),
            matrix.get(BILOUEncoding.O).get(EntityTypes.P).toString(),
            matrix.get(BILOUEncoding.O).get(BILOUEncoding.O).toString());
      }
    }

    // all files done, write buffer
    final String filename = folder + "/" + alg + ".csv";
    final CSVWriter writer =
        new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
    writer.writeNext(csv_out.toString().split(","));
    writer.close();

    TotalCSV.start();
  }

  public static void writeBuffer(final String run, final String fold, final String classifier,
      final String classs, final String a, final String b, final String c, final String d) {
    if (csv_out == null) {
      csv_out = new StringBuffer();
      csv_out.append("run,");
      csv_out.append("fold,");
      csv_out.append("classifier,");
      csv_out.append("class,");
      csv_out.append("a,");
      csv_out.append("b,");
      csv_out.append("c,");
      csv_out.append("d");
      csv_out.append('\n');
    }
    csv_out.append(run);
    csv_out.append(',');
    csv_out.append(fold);
    csv_out.append(',');
    csv_out.append(classifier);
    csv_out.append(',');
    csv_out.append(classs);
    csv_out.append(',');
    csv_out.append(a);
    csv_out.append(',');
    csv_out.append(b);
    csv_out.append(',');
    csv_out.append(c);
    csv_out.append(',');
    csv_out.append(d);
    csv_out.append('\n');
  }
}
