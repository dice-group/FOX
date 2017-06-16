package org.aksw.fox.utils.evaluation.entitiyBased;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.fox.utils.FoxCfg;

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
  protected static int runs = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

  //
  static int currentRow = 0;

  static Map<String, Integer> defaultmap = new HashMap<String, Integer>();
  static Map<String, Map<String, Integer>> matrix = new HashMap<String, Map<String, Integer>>();
  static {
    defaultmap.put(EntityClassMap.L, 0); // a
    defaultmap.put(EntityClassMap.O, 0); // b
    defaultmap.put(EntityClassMap.P, 0); // c
    defaultmap.put(EntityClassMap.N, 0); // d
  }

  static List<String> classifiedClasses = new ArrayList<>();

  static StringBuffer csv_out = null;

  static int nullRows = 0; // just
                           // debugging

  public static void init() {
    matrix.put(EntityClassMap.L, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.O, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.P, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.N, new HashMap<String, Integer>(defaultmap));

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
          if (oracelclasss.equals(EntityClassMap.N)) {
            nullRows++;
          }
          currentRow++;

          //
          if (lastOracleClass.equals(oracelclasss) && !oracelclasss.equals(EntityClassMap.N)
              && (ii < (part.numInstances() - 1))) {
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
              Integer count = matrix.get(oracelclasss).get(EntityClassMap.N);
              count++;
              matrix.get(oracelclasss).put(EntityClassMap.N, count);
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
        writeBuffer(lastRun, lastFold, alg, EntityClassMap.L,
            matrix.get(EntityClassMap.L).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.N).toString());
        writeBuffer(lastRun, lastFold, alg, EntityClassMap.O,
            matrix.get(EntityClassMap.O).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.N).toString());
        writeBuffer(lastRun, lastFold, alg, EntityClassMap.P,
            matrix.get(EntityClassMap.P).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.N).toString());
        writeBuffer(lastRun, lastFold, alg, EntityClassMap.N,
            matrix.get(EntityClassMap.N).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.N).toString());
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
