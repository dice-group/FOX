package org.aksw.fox.utils.evaluation;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.fox.CrossValidation;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxCfg;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * Calculates measures for the evaluation.
 * 
 * @author rspeck
 * 
 */
public class FoxEvaluationHelper {

    public static final String CFG_KEY_CROSSVALIDATION_RUNS = CrossValidation.class.getName().concat(".runs");

    protected int              folds                        = 10;
    protected int              runs                         = Integer.valueOf(FoxCfg.get(CFG_KEY_CROSSVALIDATION_RUNS));

    // columns
    protected int              run                          = 0;
    protected int              classifier                   = 1;
    protected int              classs                       = 2;

    // row index (L,O,P,N)
    protected int              a                            = classs + 1;
    protected int              b                            = a + 1;
    protected int              c                            = b + 1;
    protected int              d                            = c + 1;

    // measures
    protected int              recallIndex                  = d + 1;
    protected int              precisionIndex               = d + 2;
    protected int              fscoreIndex                  = d + 3;

    // in folder
    protected String           inputFolder                  = null;

    protected List<String>     files                        = null;

    // out file
    public String              outputFile                   = null;

    public List<List<String>>  values                       = new ArrayList<>();
    public List<List<String>>  meanValues                   = new ArrayList<>();

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
     *            -m `true` for calculating mean values over all classes<br>
     *            `false` for calculation values for each class
     * 
     * @throws IOException
     *             if something wrong
     */
    public static void main(String[] args) throws IOException {

        final Getopt getopt = new Getopt("FoxEval", args, "i:x o:x m:x");

        FoxEvaluationHelper foxEval = new FoxEvaluationHelper();
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

        CSVWriter writer = new CSVWriter(new FileWriter(outputFile), ',', CSVWriter.NO_QUOTE_CHARACTER);
        for (List<String> row : values)
            writer.writeNext(row.toArray(new String[row.size() - 1]));

        writer.close();
    }

    protected void meanHelper(List<String> list) {
        List<String> cur = new ArrayList<>();
        cur.add(list.get(run));
        cur.add(list.get(classifier));
        cur.add(list.get(classs));

        for (int i = a; i < list.size(); i++) {
            Double v = Double.valueOf(list.get(i)) / 3D;
            cur.add(v.toString());
        }
        meanValues.add(cur);
    }

    protected void meanTableHelper(List<String> list) {
        List<String> cur = new ArrayList<>();
        cur.add(list.get(run));
        cur.add(list.get(classifier));
        cur.add(list.get(classs));

        for (int i = a; i < list.size(); i++) {
            double v = (Double.valueOf(list.get(i)) / runs);
            cur.add(String.valueOf(v));
        }
        meanValues.add(cur);
    }

    // mean over classes except null
    public void mean() {
        meanValues = new ArrayList<>();
        String alg = "";
        List<String> current = new ArrayList<>();
        for (List<String> value : values) {
            // header
            if (values.get(0) == value)
                meanValues.add(values.get(0));
            else {

                if (!value.get(classifier).equals(alg)) {
                    alg = value.get(classifier);
                    // next
                    if (current.size() > 0) {
                        meanHelper(current);
                        current.clear();
                    }
                }

                // mean
                List<String> row = new ArrayList<>();
                row.add(value.get(run));
                row.add(value.get(classifier));
                row.add(value.get(classs));

                // ignore NULL category
                if (!value.get(classs).equals(EntityClassMap.N)) {

                    for (int i = a; i < value.size(); i++) {
                        Double v = Double.valueOf(value.get(i));
                        Double vm = 0D;
                        if (current.size() > 0)
                            vm = Double.valueOf(current.get(i) == null ? "0" : current.get(i));

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

        List<List<String>> rounded = new ArrayList<>();
        for (List<String> value : values) {
            // header
            if (values.get(0) == value)
                rounded.add(values.get(0));
            else {
                List<String> l = new ArrayList<>();
                for (int i = 0; i < recallIndex; i++) {
                    l.add(value.get(i));
                }
                for (int i = recallIndex; i < value.size() - 2; i++) {
                    l.add(String.valueOf(
                            Math.round(Double.valueOf(value.get(i)) * 10000) / 100D
                            ));
                }
                l.add(String.valueOf(
                        Math.round(Double.valueOf(value.get(value.size() - 2)) * 10000) / 100D
                        ));
                l.add(String.valueOf(
                        Math.round(Double.valueOf(value.get(value.size() - 1)) * 1000) / 1000D
                        ));
                rounded.add(l);
            }
        }
        values = rounded;
    }

    public void meanTable() {
        List<String> loc = new ArrayList<>();
        List<String> org = new ArrayList<>();
        List<String> per = new ArrayList<>();
        List<String> no = new ArrayList<>();

        String alg = "";
        for (List<String> value : values) {
            // header
            if (values.get(0) == value)
                meanValues.add(values.get(0));
            else {
                if (!value.get(classifier).equals(alg)) {
                    alg = value.get(classifier);

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
                if (value.get(classs).equals(EntityClassMap.L))
                    current = loc;
                if (value.get(classs).equals(EntityClassMap.O))
                    current = org;
                if (value.get(classs).equals(EntityClassMap.P))
                    current = per;
                if (value.get(classs).equals(EntityClassMap.N))
                    current = no;

                if (current.isEmpty())
                    current.addAll(value);
                else {
                    List<String> cur = new ArrayList<>();
                    cur.add(value.get(run));
                    cur.add(value.get(classifier));
                    cur.add(value.get(classs));

                    for (int i = a; i < value.size(); i++) {
                        Double v = Double.valueOf(value.get(i));
                        Double vm = Double.valueOf(current.get(i));
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

    protected double getFP(List<String> row) {
        int i = values.indexOf(row);
        if (row.get(classs).equals(EntityClassMap.L)) {
            return Double.valueOf(values.get(i + 1).get(a)) +
                    Double.valueOf(values.get(i + 2).get(a)) +
                    Double.valueOf(values.get(i + 3).get(a));
        }
        if (row.get(classs).equals(EntityClassMap.O)) {
            return Double.valueOf(values.get(i - 1).get(b)) +
                    Double.valueOf(values.get(i + 1).get(b)) +
                    Double.valueOf(values.get(i + 2).get(b));
        }
        if (row.get(classs).equals(EntityClassMap.P)) {
            return Double.valueOf(values.get(i - 2).get(c)) +
                    Double.valueOf(values.get(i - 1).get(c)) +
                    Double.valueOf(values.get(i + 1).get(c));
        }
        if (row.get(classs).equals(EntityClassMap.N)) {
            return Double.valueOf(values.get(i - 3).get(d)) +
                    Double.valueOf(values.get(i - 2).get(d)) +
                    Double.valueOf(values.get(i - 1).get(d));
        }
        return 0;
    }

    protected double getTP(List<String> row) {
        if (row.get(classs).equals(EntityClassMap.L)) {
            return Double.valueOf(row.get(a));
        }
        if (row.get(classs).equals(EntityClassMap.O)) {
            return Double.valueOf(row.get(b));
        }
        if (row.get(classs).equals(EntityClassMap.P)) {
            return Double.valueOf(row.get(c));
        }
        if (row.get(classs).equals(EntityClassMap.N)) {
            return Double.valueOf(row.get(d));
        }
        return 0;
    }

    protected double getFN(List<String> row) {
        if (row.get(classs).equals(EntityClassMap.L)) {
            return Double.valueOf(row.get(b)) + Double.valueOf(row.get(c)) + Double.valueOf(row.get(d));
        }
        if (row.get(classs).equals(EntityClassMap.O)) {
            return Double.valueOf(row.get(a)) + Double.valueOf(row.get(c)) + Double.valueOf(row.get(d));

        }
        if (row.get(classs).equals(EntityClassMap.P)) {
            return Double.valueOf(row.get(a)) + Double.valueOf(row.get(b)) + Double.valueOf(row.get(d));

        }
        if (row.get(classs).equals(EntityClassMap.N)) {
            return Double.valueOf(row.get(a)) + Double.valueOf(row.get(b)) + Double.valueOf(row.get(c));
        }
        return 0;
    }

    protected double getTN(List<String> row) {
        int i = values.indexOf(row);
        if (row.get(classs).equals(EntityClassMap.L)) {
            return Double.valueOf(values.get(i + 1).get(b)) + Double.valueOf(values.get(i + 1).get(c)) + Double.valueOf(values.get(i + 1).get(d)) +
                    Double.valueOf(values.get(i + 2).get(b)) + Double.valueOf(values.get(i + 2).get(c)) + Double.valueOf(values.get(i + 2).get(d)) +
                    Double.valueOf(values.get(i + 3).get(b)) + Double.valueOf(values.get(i + 3).get(c)) + Double.valueOf(values.get(i + 3).get(d));
        }
        if (row.get(classs).equals(EntityClassMap.O)) {
            return Double.valueOf(values.get(i - 1).get(a)) + Double.valueOf(values.get(i - 1).get(c)) + Double.valueOf(values.get(i - 1).get(d)) +
                    Double.valueOf(values.get(i + 1).get(a)) + Double.valueOf(values.get(i + 1).get(c)) + Double.valueOf(values.get(i + 1).get(d)) +
                    Double.valueOf(values.get(i + 2).get(a)) + Double.valueOf(values.get(i + 2).get(c)) + Double.valueOf(values.get(i + 2).get(d));
        }
        if (row.get(classs).equals(EntityClassMap.P)) {
            return Double.valueOf(values.get(i - 2).get(a)) + Double.valueOf(values.get(i - 2).get(b)) + Double.valueOf(values.get(i - 2).get(d)) +
                    Double.valueOf(values.get(i - 1).get(a)) + Double.valueOf(values.get(i - 1).get(b)) + Double.valueOf(values.get(i - 1).get(d)) +
                    Double.valueOf(values.get(i + 1).get(a)) + Double.valueOf(values.get(i + 1).get(b)) + Double.valueOf(values.get(i + 1).get(d));
        }
        if (row.get(classs).equals(EntityClassMap.N)) {
            return Double.valueOf(values.get(i - 3).get(a)) + Double.valueOf(values.get(i - 3).get(b)) + Double.valueOf(values.get(i - 3).get(c)) +
                    Double.valueOf(values.get(i - 2).get(a)) + Double.valueOf(values.get(i - 2).get(b)) + Double.valueOf(values.get(i - 2).get(c)) +
                    Double.valueOf(values.get(i - 1).get(a)) + Double.valueOf(values.get(i - 1).get(b)) + Double.valueOf(values.get(i - 1).get(c));
        }
        return 0;
    }

    // Matthews correlation coefficient
    protected void addMcc() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("mcc");
                continue;
            }

            Double tp = Double.valueOf(getTP(row)), fp = Double.valueOf(getFP(row)), fn = Double.valueOf(getFN(row)), tn = Double.valueOf(getTN(row));
            Double d = (tp + fp) * (tp + fn) * (tn + fp) * (tn + fn);

            Double mcc = 0D;
            if (d > 0) {
                mcc = ((tp * tn) - (fp * fn)) / Math.sqrt(d);
            }

            row.add(mcc.toString());
        }
    }

    protected void addError() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("error");
                continue;
            }

            Double tp = getTP(row), fp = getFP(row), fn = getFN(row), tn = getTN(row);
            Double accuracy = 0D;
            if (tp + fn + fp + tn > 0)
                accuracy = 1D - Double.valueOf(tp + tn) / Double.valueOf(tp + fn + fp + tn);

            row.add(accuracy.toString());
        }
    }

    // (tp+tn)/(tp+fn+fp+tn)
    protected void addAccuracy() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("accuracy");
                continue;
            }

            Double tp = getTP(row), fp = getFP(row), fn = getFN(row), tn = getTN(row);
            Double accuracy = 0D;
            if (tp + fn + fp + tn > 0)
                accuracy = Double.valueOf(tp + tn) / Double.valueOf(tp + fn + fp + tn);

            row.add(accuracy.toString());
        }
    }

    // 2*((pre*recall)/(pre+recall))
    protected void addFscore() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("fscore");
                continue;
            }
            Double precision = Double.valueOf(row.get(precisionIndex));
            Double recall = Double.valueOf(row.get(recallIndex));
            if (precision + recall > 0) {
                row.add(String.valueOf(2 * (Double.valueOf(precision * recall) / Double.valueOf(precision + recall))));
            } else
                row.add("0");
        }
    }

    // tp/(tp+fp)
    protected void addPrecision() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("precision");
                continue;
            }
            Double tp = getTP(row), fp = getFP(row);
            Double precision = 0D;
            if (tp > 0)
                precision = Double.valueOf(tp) / Double.valueOf(tp + fp);

            row.add(precision.toString());
        }
    }

    // tp/(tp+fn)
    protected void addRecall() {
        for (List<String> row : values) {
            // header
            if (values.get(0) == row) {
                row.add("recall");
                continue;
            }
            Double tp = getTP(row), fn = getFN(row);

            Double recall = 0D;
            if (tp > 0)
                recall = tp / Double.valueOf(tp + fn);

            row.add(recall.toString());
        }
    }

    // read folder to files
    public List<String> getFiles(String folder) throws IOException {
        files = new ArrayList<>();
        File file = new File(folder);

        if (!file.exists()) {
            throw new IOException("Can't find file or directory.");
        } else {
            if (file.isDirectory()) {
                for (File fileEntry : file.listFiles()) {
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
        for (String file : files) {
            CSVReader csvReader = new CSVReader(new FileReader(file));
            String[] header = csvReader.readNext();
            String[] row = null;
            while ((row = csvReader.readNext()) != null) {
                if (!headeradded) {
                    values.add(new ArrayList<String>(Arrays.asList(header)));
                    headeradded = true;
                }
                if (row.length == header.length)
                    values.add(new ArrayList<String>(Arrays.asList(row)));
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
    public void setOutputFile(String outputFile) {
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
    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

}