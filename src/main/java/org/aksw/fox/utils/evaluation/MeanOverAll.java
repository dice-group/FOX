package org.aksw.fox.utils.evaluation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

@Deprecated
// TODO: clean up this quick n dirty code
public class MeanOverAll {

  static int f1index = 3;
  static int nameindex = 0;

  static Map<String, Double> means = new HashMap<>();

  static String file1 = "evaluation/tokenBasedAll/1.csv";
  static String file2 = "evaluation/tokenBasedAll/2.csv";
  static String file3 = "evaluation/tokenBasedAll/3.csv";
  static String file4 = "evaluation/tokenBasedAll/4.csv";
  static String file5 = "evaluation/tokenBasedAll/5.csv";

  static List<String> files = new ArrayList<>();

  public static void main(final String[] args) throws Exception {

    Collections.addAll(files, file1, file2, file3, file4, file5);

    for (final String file : files) {

      final CSVReader reader =
          new CSVReader(new FileReader(file), ',', CSVWriter.NO_QUOTE_CHARACTER);
      String[] nextLine;
      // no header
      reader.readNext();
      while ((nextLine = reader.readNext()) != null) {
        final String name = nextLine[nameindex];
        final Double f1 = Double.valueOf(nextLine[f1index]);

        if (means.get(name) == null) {
          means.put(name, f1);
        } else {
          Double v = means.get(name);
          v = v + f1;
          means.put(name, v);
        }
      }
      reader.close();

    }

    for (final Entry<String, Double> e : means.entrySet()) {
      System.out.println(e.getKey() + "," + (e.getValue() / 5));
    }
  }
}
