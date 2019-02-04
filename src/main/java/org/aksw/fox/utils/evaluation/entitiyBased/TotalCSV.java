package org.aksw.fox.utils.evaluation.entitiyBased;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.aksw.fox.data.EntityTypes;
import org.aksw.fox.data.encode.BILOUEncoding;
import org.aksw.fox.evaluation.CrossValidation;
import org.aksw.simba.knowledgeextraction.commons.config.PropertiesLoader;

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

  protected static int runs = Integer.valueOf(PropertiesLoader.get(CFG_KEY_CROSSVALIDATION_RUNS));

  public static StringBuffer csv_out = null;
  static Map<String, Integer> defaultmap = new HashMap<>();
  static Map<String, Map<String, Integer>> matrix = new HashMap<>();
  static {
    defaultmap.put(EntityTypes.L, 0); // a
    defaultmap.put(EntityTypes.O, 0); // b
    defaultmap.put(EntityTypes.P, 0); // c
    defaultmap.put(BILOUEncoding.O, 0); // d
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
    matrix.put(EntityTypes.L, new HashMap<>(defaultmap));
    matrix.put(EntityTypes.O, new HashMap<>(defaultmap));
    matrix.put(EntityTypes.P, new HashMap<>(defaultmap));
    matrix.put(BILOUEncoding.O, new HashMap<>(defaultmap));
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
      if (nextLine.length - 1 < d) {
        break;
      }
      currentAlg = nextLine[classifier];
      // next run
      if (!nextLine[run].equals(currentRun)) {
        writeBuffer(currentRun, currentAlg, EntityTypes.L,
            matrix.get(EntityTypes.L).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.L).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.L).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.L).get(BILOUEncoding.O).toString());
        writeBuffer(currentRun, currentAlg, EntityTypes.O,
            matrix.get(EntityTypes.O).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.O).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.O).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.O).get(BILOUEncoding.O).toString());
        writeBuffer(currentRun, currentAlg, EntityTypes.P,
            matrix.get(EntityTypes.P).get(EntityTypes.L).toString(),
            matrix.get(EntityTypes.P).get(EntityTypes.O).toString(),
            matrix.get(EntityTypes.P).get(EntityTypes.P).toString(),
            matrix.get(EntityTypes.P).get(BILOUEncoding.O).toString());
        writeBuffer(currentRun, currentAlg, BILOUEncoding.O,
            matrix.get(BILOUEncoding.O).get(EntityTypes.L).toString(),
            matrix.get(BILOUEncoding.O).get(EntityTypes.O).toString(),
            matrix.get(BILOUEncoding.O).get(EntityTypes.P).toString(),
            matrix.get(BILOUEncoding.O).get(BILOUEncoding.O).toString());
        init();
        currentRun = nextLine[run];
        currentAlg = nextLine[classifier];

      }
      // a
      Integer va = matrix.get(nextLine[classs]).get(EntityTypes.L);
      va = va + Integer.valueOf(nextLine[a]);
      matrix.get(nextLine[classs]).put(EntityTypes.L, va);
      // b
      Integer vb = matrix.get(nextLine[classs]).get(EntityTypes.O);
      vb = vb + Integer.valueOf(nextLine[b]);
      matrix.get(nextLine[classs]).put(EntityTypes.O, vb);
      // c
      Integer vc = matrix.get(nextLine[classs]).get(EntityTypes.P);
      vc = vc + Integer.valueOf(nextLine[c]);
      matrix.get(nextLine[classs]).put(EntityTypes.P, vc);
      // d
      Integer vd = matrix.get(nextLine[classs]).get(BILOUEncoding.O);
      vd = vd + Integer.valueOf(nextLine[d]);
      matrix.get(nextLine[classs]).put(BILOUEncoding.O, vd);
    }
    writeBuffer(currentRun, currentAlg, EntityTypes.L,
        matrix.get(EntityTypes.L).get(EntityTypes.L).toString(),
        matrix.get(EntityTypes.L).get(EntityTypes.O).toString(),
        matrix.get(EntityTypes.L).get(EntityTypes.P).toString(),
        matrix.get(EntityTypes.L).get(BILOUEncoding.O).toString());
    writeBuffer(currentRun, currentAlg, EntityTypes.O,
        matrix.get(EntityTypes.O).get(EntityTypes.L).toString(),
        matrix.get(EntityTypes.O).get(EntityTypes.O).toString(),
        matrix.get(EntityTypes.O).get(EntityTypes.P).toString(),
        matrix.get(EntityTypes.O).get(BILOUEncoding.O).toString());
    writeBuffer(currentRun, currentAlg, EntityTypes.P,
        matrix.get(EntityTypes.P).get(EntityTypes.L).toString(),
        matrix.get(EntityTypes.P).get(EntityTypes.O).toString(),
        matrix.get(EntityTypes.P).get(EntityTypes.P).toString(),
        matrix.get(EntityTypes.P).get(BILOUEncoding.O).toString());
    writeBuffer(currentRun, currentAlg, BILOUEncoding.O,
        matrix.get(BILOUEncoding.O).get(EntityTypes.L).toString(),
        matrix.get(BILOUEncoding.O).get(EntityTypes.O).toString(),
        matrix.get(BILOUEncoding.O).get(EntityTypes.P).toString(),
        matrix.get(BILOUEncoding.O).get(BILOUEncoding.O).toString());
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
