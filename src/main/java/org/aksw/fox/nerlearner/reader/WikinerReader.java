package org.aksw.fox.nerlearner.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FileUtil;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.PropertyConfigurator;

public class WikinerReader implements INERReader {

    protected File[]                                              inputFiles;
    protected StringBuilder                                       input          = new StringBuilder();
    protected HashMap<String, String>                             entities       = new HashMap<>();
    protected HashMap<String, List<SimpleEntry<String, Integer>>> disambEntities = new HashMap<>();

    public static void main(String[] a) throws IOException {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);

        String[] files = new String[1];
        files[0] = "input/Wikiner/aij-wikiner-de-wp2.bz2";

        WikinerReader r = new WikinerReader(files);
        LOG.info(r.getEntities().size());
        /*
        for (Entry<?, ?> e : r.getEntities().entrySet()) {
            LOG.info(e.getValue());
        }
        LOG.info(r.getInput());
        */

    }

    /**
     * Constructor for loading class.
     */
    public WikinerReader() {
    }

    public WikinerReader(String[] inputPaths) throws IOException {
        initFiles(inputPaths);
    }

    @Override
    public void initFiles(String[] initFiles) throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("WikinerReader ...");

        inputFiles = new File[initFiles.length];

        if (LOG.isDebugEnabled())
            LOG.debug("search files (" + initFiles.length + ")...");

        for (int i = 0; i < initFiles.length; i++) {
            inputFiles[i] = new File(initFiles[i]);
            if (!inputFiles[i].exists())
                throw new FileNotFoundException(initFiles[i]);
        }

        readData();
    }

    /**
     * Tags are:
     * 
     * [I-MISC, B-LOC, I-PER, B-PER, I-LOC, B-MISC, I-ORG, B-ORG, O]
     * 
     * @throws IOException
     */
    protected void readData() throws IOException {
        int sentenceCount = 0;
        StringBuilder inputLine = new StringBuilder();
        StringBuilder currentEntity = new StringBuilder();
        String currentTag = "";
        for (int i = 0; i < inputFiles.length; i++) {
            if (maxSentences > 0 && sentenceCount >= maxSentences)
                break;
            for (String line : FileUtil.bzip2ToList(inputFiles[i].getAbsolutePath())) {
                if (maxSentences > 0 && sentenceCount >= maxSentences)
                    break;
                String[] taggedWords = line.split(" ");

                for (int ii = 0; ii < taggedWords.length; ii++) {
                    String[] tags = taggedWords[ii].split("\\|");

                    if (tags.length > 1) {
                        String word = tags[0];
                        String nerTag = tags[2];

                        inputLine.append(word).append(" ");

                        if (currentTag.isEmpty()) {
                            if (!nerTag.equals("O")) {
                                currentTag = nerTag.split("-")[1];
                                currentEntity.append(word).append(" ");
                            }
                        } else {

                            if (nerTag.endsWith(currentTag)) {
                                currentEntity.append(word).append(" ");
                            } else {

                                if (currentTag.endsWith("PER"))
                                    currentTag = EntityClassMap.P;
                                else if (currentTag.endsWith("LOC"))
                                    currentTag = EntityClassMap.L;
                                else if (currentTag.endsWith("ORG"))
                                    currentTag = EntityClassMap.O;
                                else
                                    currentTag = ""; // unsupported tag

                                if (!currentTag.isEmpty()) {

                                    String e = currentEntity.toString().trim();
                                    if (entities.get(e) == null) {

                                        entities.put(
                                                e,
                                                currentTag
                                                );
                                    } else {

                                        if (!entities.get(e).equals(currentTag)) {

                                            if (disambEntities.get(e) == null) {
                                                disambEntities.put(e, new ArrayList<SimpleEntry<String, Integer>>());
                                                disambEntities.get(e).add(
                                                        new SimpleEntry<String, Integer>(entities.get(e), 1));
                                            }
                                            {

                                                boolean found = false;
                                                for (SimpleEntry<String, Integer> disambEntry : disambEntities.get(e)) {
                                                    if (disambEntry.getKey().equals(currentTag)) {
                                                        disambEntry.setValue(disambEntry.getValue() + 1);
                                                        found = true;
                                                    }
                                                }
                                                if (!found) {
                                                    disambEntities.get(e).add(
                                                            new SimpleEntry<String, Integer>(currentTag, 1)
                                                            );
                                                }
                                            }
                                        }
                                    }
                                }

                                currentEntity = new StringBuilder();
                                currentTag = "";

                                if (!nerTag.equals("O")) {
                                    currentTag = nerTag.split("-")[1];
                                    currentEntity.append(word).append(" ");
                                }
                            }
                        }
                    }
                }// line end
                input.append(inputLine).append(System.lineSeparator());
                inputLine = new StringBuilder();
                sentenceCount++;
            }
        }
        // removes disambs
        for (Entry<String, List<SimpleEntry<String, Integer>>> entry : disambEntities.entrySet())
            entities.remove(entry.getKey());
    }

    @Override
    public String getInput() {
        return input.toString().trim();
    }

    @Override
    public Map<String, String> getEntities() {
        return entities;
    }
}
