package org.aksw.fox.tools.ner.en;

import org.aksw.fox.tools.ner.common.SpotlightCommon;

public class SpotlightEN extends SpotlightCommon {

    public SpotlightEN() {
        super("en");
    }
    /*
        public static void main(String[] a) throws IOException {
            PropertyConfigurator.configure(FoxCfg.LOG_FILE);

            TrainingInputReader tr = new TrainingInputReader();
            tr.initFiles("input/4");
            String input = tr.getInput();
            // input = FoxConst.NER_EN_EXAMPLE_2;
            new SpotlightEN()
                    .retrieve(input)
                    .forEach(
                            p -> LOG.info(p)
                    );
        }
        */
}
