package org.aksw.fox.tools.ner.en;

import java.io.IOException;

import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.tools.ner.common.TagMeCommon;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.PropertyConfigurator;

public class TagMeEN extends TagMeCommon {

    public TagMeEN() {
        super("en", "http://dbpedia.org/sparql", "http://dbpedia.org");
    }

    public static void main(String[] a) throws IOException {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        TrainingInputReader tr = new TrainingInputReader();
        tr.initFiles("input/4");

        LOG.info(
                new TagMeEN().retrieve(
                        tr.getInput()
                        )
                );

    }
}