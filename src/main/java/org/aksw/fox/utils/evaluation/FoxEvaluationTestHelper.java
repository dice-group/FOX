package org.aksw.fox.utils.evaluation;

import gnu.getopt.Getopt;

import java.io.IOException;

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
     *            <p>
     *            -i for an input directory <br>
     *            (files in csv format with columns:<br>
     *            run, classifier, class, a, b, c, d) <br>
     *            <br>
     * 
     *            -o for an output file<br>
     *            </p>
     * 
     * @throws IOException
     *             if something wrong
     */
    public static void main(String[] args) throws IOException {

        final Getopt getopt = new Getopt("FoxEvalTest", args, "i:x o:x");

        FoxEvaluationHelper fet = new FoxEvaluationTestHelper();

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

        run = 0;
        fold = 1;
        classifier = 2;
        classs = 3;

        // row index (L,O,P,N)
        a = classs + 1;
        b = a + 1;
        c = b + 1;
        d = c + 1;

        recallIndex = d + 1;
        precisionIndex = d + 2;
        fscoreIndex = d + 3;
    }
}