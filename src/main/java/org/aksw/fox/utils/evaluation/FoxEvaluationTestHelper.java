package org.aksw.fox.utils.evaluation;

import java.io.IOException;

import gnu.getopt.Getopt;

@Deprecated
/**
 *
 * Calculates measures to use for Wilcoxon signed-rank test.
 *
 *
 * @author rspeck
 *
 */
public class FoxEvaluationTestHelper extends FoxEvaluationHelper {

  protected int fold;

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
   * @throws IOException if something wrong
   */
  public static void main(final String[] args) throws IOException {

    final Getopt getopt = new Getopt("FoxEvalTest", args, "i:x o:x");

    final FoxEvaluationHelper fet = new FoxEvaluationTestHelper();

    int arg = -1;
    while ((arg = getopt.getopt()) != -1) {
      switch (arg) {
        case 'i':
          fet.setInputFolder(String.valueOf(getopt.getOptarg()));
          break;
        case 'o':
          fet.setOutputFile(String.valueOf(getopt.getOptarg()));
          break;

      }
    }

    fet.getFiles(fet.getInputFolder());
    fet.read();
    fet.addValues();
    fet.write();
  }

  public FoxEvaluationTestHelper() {

    col_run = 0;
    fold = 1;
    col_classifier = 2;
    col_classs = 3;

    // row index (L,O,P,N)
    row_a = col_classs + 1;
    row_b = row_a + 1;
    row_c = row_b + 1;
    row_d = row_c + 1;

    row_recallIndex = row_d + 1;
    row_precisionIndex = row_d + 2;
    row_fscoreIndex = row_d + 3;
  }
}
