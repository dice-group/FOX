package org.aksw.fox.tools.ner.en;

import java.io.IOException;

import org.aksw.fox.nerlearner.reader.TrainingInputReader;
import org.aksw.fox.tools.ner.common.SpotlightCommon;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.PropertyConfigurator;

public class SpotlightEN extends SpotlightCommon {

    public SpotlightEN() {
        super("en");
    }

    public static void main(String[] a) throws IOException {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);

        TrainingInputReader tr = new TrainingInputReader();
        tr.initFiles("input/4");
        String input = tr.getInput();
        LOG.info(new SpotlightEN().retrieve(input));

    }
}
