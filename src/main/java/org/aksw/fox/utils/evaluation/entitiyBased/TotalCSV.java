package org.aksw.fox.utils.evaluation.entitiyBased;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.fox.utils.FoxCfg;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

@Deprecated
// TODO: clean up this quick n dirty code
public class TotalCSV {

  public static String folder = DetailCSV.folder;
  public static String file = "/" + DetailCSV.alg + ".csv";
  public static String fileout = "/" + DetailCSV.alg + "_total.csv";

  public static final String CFG_KEY_CROSSVALIDATION_RUNS =
      CrossValidation.class.getName().concat(".runs");

  protected static int runs = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

  public static StringBuffer csv_out = null;
  static Map<String, Integer> defaultmap = new HashMap<String, Integer>();
  static Map<String, Map<String, Integer>> matrix = new HashMap<String, Map<String, Integer>>();
  static {
    defaultmap.put(EntityClassMap.L, 0); // a
    defaultmap.put(EntityClassMap.O, 0); // b
    defaultmap.put(EntityClassMap.P, 0); // c
    defaultmap.put(EntityClassMap.N, 0); // d
  }

  // csv index
  static int run = 0;
  static int fold = 1;
  static int classifier = 2;
  static int classs = 3;
  static int a = 4;
  static int b = 5;
  static int c = 6;
  static int d = 7;

  public static void init() {
    matrix.put(EntityClassMap.L, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.O, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.P, new HashMap<String, Integer>(defaultmap));
    matrix.put(EntityClassMap.N, new HashMap<String, Integer>(defaultmap));
  }

  public static void start() throws Exception {

    // public static void main(String[] args) throws Exception {

    final String filename = folder + file;
    final CSVReader reader =
        new CSVReader(new FileReader(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
    init();
    String[] nextLine;
    // no header
    reader.readNext();
    String currentRun = "1";
    String currentAlg = "na";

    while ((nextLine = reader.readNext()) != null) {
      if ((nextLine.length - 1) < d) {
        break;
      }
      currentAlg = nextLine[classifier];
      // next run
      if (!nextLine[run].equals(currentRun)) {
        writeBuffer(currentRun, currentAlg, EntityClassMap.L,
            matrix.get(EntityClassMap.L).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.L).get(EntityClassMap.N).toString());
        writeBuffer(currentRun, currentAlg, EntityClassMap.O,
            matrix.get(EntityClassMap.O).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.O).get(EntityClassMap.N).toString());
        writeBuffer(currentRun, currentAlg, EntityClassMap.P,
            matrix.get(EntityClassMap.P).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.P).get(EntityClassMap.N).toString());
        writeBuffer(currentRun, currentAlg, EntityClassMap.N,
            matrix.get(EntityClassMap.N).get(EntityClassMap.L).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.O).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.P).toString(),
            matrix.get(EntityClassMap.N).get(EntityClassMap.N).toString());
        init();
        currentRun = nextLine[run];
        currentAlg = nextLine[classifier];

      }
      // a
      Integer va = matrix.get(nextLine[classs]).get(EntityClassMap.L);
      va = va + Integer.valueOf(nextLine[a]);
      matrix.get(nextLine[classs]).put(EntityClassMap.L, va);
      // b
      Integer vb = matrix.get(nextLine[classs]).get(EntityClassMap.O);
      vb = vb + Integer.valueOf(nextLine[b]);
      matrix.get(nextLine[classs]).put(EntityClassMap.O, vb);
      // c
      Integer vc = matrix.get(nextLine[classs]).get(EntityClassMap.P);
      vc = vc + Integer.valueOf(nextLine[c]);
      matrix.get(nextLine[classs]).put(EntityClassMap.P, vc);
      // d
      Integer vd = matrix.get(nextLine[classs]).get(EntityClassMap.N);
      vd = vd + Integer.valueOf(nextLine[d]);
      matrix.get(nextLine[classs]).put(EntityClassMap.N, vd);
    }
    writeBuffer(currentRun, currentAlg, EntityClassMap.L,
        matrix.get(EntityClassMap.L).get(EntityClassMap.L).toString(),
        matrix.get(EntityClassMap.L).get(EntityClassMap.O).toString(),
        matrix.get(EntityClassMap.L).get(EntityClassMap.P).toString(),
        matrix.get(EntityClassMap.L).get(EntityClassMap.N).toString());
    writeBuffer(currentRun, currentAlg, EntityClassMap.O,
        matrix.get(EntityClassMap.O).get(EntityClassMap.L).toString(),
        matrix.get(EntityClassMap.O).get(EntityClassMap.O).toString(),
        matrix.get(EntityClassMap.O).get(EntityClassMap.P).toString(),
        matrix.get(EntityClassMap.O).get(EntityClassMap.N).toString());
    writeBuffer(currentRun, currentAlg, EntityClassMap.P,
        matrix.get(EntityClassMap.P).get(EntityClassMap.L).toString(),
        matrix.get(EntityClassMap.P).get(EntityClassMap.O).toString(),
        matrix.get(EntityClassMap.P).get(EntityClassMap.P).toString(),
        matrix.get(EntityClassMap.P).get(EntityClassMap.N).toString());
    writeBuffer(currentRun, currentAlg, EntityClassMap.N,
        matrix.get(EntityClassMap.N).get(EntityClassMap.L).toString(),
        matrix.get(EntityClassMap.N).get(EntityClassMap.O).toString(),
        matrix.get(EntityClassMap.N).get(EntityClassMap.P).toString(),
        matrix.get(EntityClassMap.N).get(EntityClassMap.N).toString());
    reader.close();
    write();
  }

  public static void write() throws Exception {

    final String filename = folder + fileout;
    final CSVWriter writer =
        new CSVWriter(new FileWriter(filename), ',', CSVWriter.NO_QUOTE_CHARACTER);
    writer.writeNext(csv_out.toString().split(","));
    writer.close();

  }

  public static void writeBuffer(final String run, final String classifier, final String classs,
      final String a, final String b, final String c, final String d) {
    if (csv_out == null) {
      csv_out = new StringBuffer();
      csv_out.append("run,");
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
