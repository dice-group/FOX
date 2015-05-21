package org.aksw.fox.tools.ner.es;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.NamedEntityRecognitionNerfEXT;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.Token;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.Tokenizer;

public class NERBalieES extends AbstractNER {

    @Override
    public List<Entity> retrieve(String input) {
        LOG.info("retrieve ...");

        Tokenizer tokenizer = new Tokenizer(Balie.LANGUAGE_SPANISH, true);
        tokenizer.Reset(); // ?
        tokenizer.Tokenize(input);
        PriorCorrectionNerf pcn = new PriorCorrectionNerf();
        NamedEntityRecognitionNerfEXT ner = new NamedEntityRecognitionNerfEXT(
                tokenizer.GetTokenList(),
                new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON), DisambiguationRulesNerf.Load(),
                pcn,
                NamedEntityTypeEnumMappingNerf.values(),
                LOG.isTraceEnabled());

        ner.RecognizeEntities();
        TokenList tokenList = ner.GetTokenList();

        List<Entity> list = new ArrayList<>();
        String lastType = null;
        for (int i = 0; i < tokenList.Size(); i++) {

            float re = Entity.DEFAULT_RELEVANCE;
            if (FoxCfg.get("balieDefaultRelevance") != null && !Boolean.valueOf(FoxCfg.get("balieDefaultRelevance"))) {
                Double like = ner.map.get(i);
                if (like != null) {
                    re = like.floatValue();
                }
            }
            Token token = tokenList.Get(i);
            String type = token.EntityType().GetLabel(NamedEntityTypeEnumMappingNerf.values());
            if (type != null && EntityClassMap.balie(type) != EntityClassMap.getNullCategory() && type.equals(lastType) && list.size() > 0) {
                list.get(list.size() - 1).addText(token.Raw());
            } else {
                if (EntityClassMap.balie(type) != EntityClassMap.getNullCategory())
                    list.add(getEntity(token.Raw(), EntityClassMap.balie(type), re, getToolName()));
                lastType = type;
            }
        }
        // TRACE
        if (LOG.isTraceEnabled()) {
            // LOG.trace(list);
        } // TRACE
        return list;
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        for (Entity e : new NERBalieES().retrieve(FoxConst.NER_ES_EXAMPLE_1))
            NERBalieES.LOG.info(e);
    }
}