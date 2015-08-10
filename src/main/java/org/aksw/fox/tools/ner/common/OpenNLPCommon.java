package org.aksw.fox.tools.ner.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxTextUtil;

public class OpenNLPCommon extends AbstractNER {

    protected String[]               modelPath;
    protected TokenNameFinderModel[] tokenNameFinderModels;

    @SuppressWarnings("unused")
    private OpenNLPCommon() {
    };

    public OpenNLPCommon(String[] modelPath) {

        entityClasses.put("location", EntityClassMap.L);
        entityClasses.put("organization", EntityClassMap.O);
        entityClasses.put("person", EntityClassMap.P);

        this.modelPath = modelPath;

        tokenNameFinderModels = new TokenNameFinderModel[modelPath.length];
        InputStream[] modelIn = new InputStream[modelPath.length];

        for (int i = 0; i < tokenNameFinderModels.length; i++) {
            try {
                modelIn[i] = new FileInputStream(modelPath[i]);
                if (modelIn[i] != null)
                    tokenNameFinderModels[i] = new TokenNameFinderModel(modelIn[i]);

            } catch (IOException e) {
                LOG.error("\n", e);
            } finally {
                try {
                    if (modelIn[i] != null)
                        modelIn[i].close();
                } catch (IOException e) {
                    LOG.error("\n", e);
                }
            }
        }
    }

    // TODO: do parallel for each model
    @Override
    public List<Entity> retrieve(String input) {
        LOG.info("retrieve ...");

        List<Entity> list = new ArrayList<>();
        String[] sentences = FoxTextUtil.getSentences(input);
        LOG.debug("sentences: " + sentences.length);

        for (int i = 0; i < tokenNameFinderModels.length; i++) {
            if (tokenNameFinderModels[i] != null) {
                NameFinderME nameFinder = new NameFinderME(tokenNameFinderModels[i]);
                for (String sentence : sentences) {
                    String[] tokens = FoxTextUtil.getSentenceToken(sentence);
                    LOG.debug("tokens: " + tokens.length);
                    if (tokens.length > 0 && tokens[tokens.length - 1].trim().isEmpty())
                        tokens[tokens.length - 1] = ".";

                    Span[] nameSpans = nameFinder.find(tokens);
                    double[] probs = nameFinder.probs(nameSpans);
                    for (int ii = 0; ii < nameSpans.length; ii++) {
                        Span span = nameSpans[ii];

                        String word = "";
                        for (int j = 0; j < span.getEnd() - span.getStart(); j++)
                            word += tokens[span.getStart() + j] + " ";
                        word = word.trim();

                        float p = Entity.DEFAULT_RELEVANCE;
                        if (FoxCfg.get("openNLPDefaultRelevance") != null && !Boolean.valueOf(FoxCfg.get("openNLPDefaultRelevance")))
                            p = Double.valueOf(probs[ii]).floatValue();
                        String cl = mapTypeToSupportedType(span.getType());
                        if (cl != EntityClassMap.getNullCategory())
                            list.add(getEntity(word, cl, p, getToolName()));
                    }
                }
                nameFinder.clearAdaptiveData();
            }
        }
        // TRACE
        if (LOG.isTraceEnabled()) {
            LOG.trace(list);
        } // TRACE
        return list;
    }

}
