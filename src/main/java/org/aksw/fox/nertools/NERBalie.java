package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.utils.FoxCfg;
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

// http://github.com/smtlaissezfaire/balie
public class NERBalie extends AbstractNER {

    @Override
    public Set<Entity> retrieve(String input) {
        logger.info("retrieve ...");

        Tokenizer tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);
        tokenizer.Reset(); // ?
        tokenizer.Tokenize(input);
        PriorCorrectionNerf pcn = new PriorCorrectionNerf();
        NamedEntityRecognitionNerfEXT ner = new NamedEntityRecognitionNerfEXT(
                tokenizer.GetTokenList(),
                new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON), DisambiguationRulesNerf.Load(),
                pcn,
                NamedEntityTypeEnumMappingNerf.values(),
                logger.isTraceEnabled());

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

        return new HashSet<Entity>(list);
    }

    public static void main(String[] a) {
        PropertyConfigurator.configure("log4j.properties");
        for (Entity e : new NERBalie()
                .retrieve("Stanford University is located in California. It is a great university."
                        +
                        "The Leipzig region was the arena of the 1813 Battle of Leipzig between Napoleonic France and an allied coalition of Prussia, Russia, Austria, and Sweden. It was the largest battle in Europe prior to World War I and ended Napoleon's presence in Germany and would ultimately lead to his first exile on Elba. In 1913 the Monument to the Battle of the Nations monument celebrating the centenary of this event was completed."))
            NERBalie.logger.info(e);
    }
}
