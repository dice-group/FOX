package org.aksw.fox.nerlearner.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.EntityClassMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import edu.stanford.nlp.util.Triple;
// 
// preprocessing tweets
// 
// https://github.com/cproof/tweet-analysis/tree/master/server/src/main/java/at/tuwien/aic/tweetanalysis/preprocessing

public class NEEL2014Reader implements INERReader {

    public static Logger          LOG            = LogManager.getLogger(NEEL2014Reader.class);
    protected File                tweetFile      = null;
    protected File                annotationFile = null;

    protected StringBuffer        input          = new StringBuffer();
    protected Map<String, String> entities       = new HashMap<>();

    /**
     * Test
     */
    public static void main(String[] a) throws IOException {

        String[] files = new String[2];
        files[0] = "/home/rspeck/Downloads/datasets_neel2015/NEEL2015-training-tweets.tsv";
        files[1] = "/home/rspeck/Downloads/datasets_neel2015/NEEL2015-training-gold_v1.tsv";

        NEEL2014Reader r = new NEEL2014Reader();
        r.initFiles(files);

        LOG.info(r.getInput().length());
        LOG.info(r.getEntities().size());
    }

    @Override
    public void initFiles(String[] initFiles) throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("NEEL2014Reader ...");

        if (LOG.isDebugEnabled())
            LOG.debug("search files ...");

        if (initFiles.length != 2)
            throw new IOException("Parameter has to be a length of 2.");

        tweetFile = new File(initFiles[0]);
        annotationFile = new File(initFiles[1]);

        if (!tweetFile.exists())
            throw new FileNotFoundException(tweetFile.getPath());
        if (!annotationFile.exists())
            throw new FileNotFoundException(annotationFile.getPath());

        readData();

    }

    protected void readData() throws IOException {
        // -------- read data -----------
        FileReader tweetFileReader = new FileReader(tweetFile);
        FileReader annotationFileReader = new FileReader(annotationFile);

        CSVReader tweetCSVReader = new CSVReader(tweetFileReader, '\t');
        CSVReader annotationCSVReader = new CSVReader(annotationFileReader, '\t');

        List<String[]> allTweets = tweetCSVReader.readAll();
        tweetCSVReader.close();
        tweetCSVReader = null;
        tweetFileReader = null;
        // LOG.info(allTweets.size());
        // LOG.info(allTweets.get(0).length);

        List<String[]> allAnnotations = annotationCSVReader.readAll();
        annotationCSVReader.close();
        annotationCSVReader = null;
        annotationFileReader = null;

        // -------- create data -----------
        Map<String, String> idToTweet = new HashMap<>();
        Map<String, Triple<Integer, Integer, String>> idToNEIndexType = new HashMap<>();
        for (String[] line : allTweets) {

            // LOG.info(line[0] + " -> " + line[1]);
            idToTweet.put(line[0], line[1]);
            input.append(line[1]);
            input.append("\n");
        }
        for (String[] line : allAnnotations) {
            idToNEIndexType.put(line[0], Triple.makeTriple(Integer.valueOf(line[1]), Integer.valueOf(line[2]), line[4]));
        }

        for (Entry<String, Triple<Integer, Integer, String>> e : idToNEIndexType.entrySet()) {
            String tweet = idToTweet.get(e.getKey());
            String surfaceform = tweet.substring(e.getValue().first, e.getValue().second);
            // LOG.info(surfaceform);
            entities.put(surfaceform, EntityClassMap.neel(e.getValue().third));
        }

    }

    @Override
    public String getInput() {
        return input.toString();
    }

    @Override
    public Map<String, String> getEntities() {
        return entities;
    }

}
