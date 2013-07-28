package org.aksw.fox.nertools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;

import ca.uottawa.balie.Balie;
import ca.uottawa.balie.DisambiguationRulesNerf;
import ca.uottawa.balie.LexiconOnDisk;
import ca.uottawa.balie.NamedEntityRecognitionNerf;
import ca.uottawa.balie.NamedEntityTypeEnumMappingNerf;
import ca.uottawa.balie.PriorCorrectionNerf;
import ca.uottawa.balie.Token;
import ca.uottawa.balie.TokenList;
import ca.uottawa.balie.TokenListIterator;
import ca.uottawa.balie.Tokenizer;

public class NERBalie extends AbstractNER {

    @Override
    public Set<Entity> retrieve(String input) {
        logger.info("retrieve ...");

        Tokenizer tokenizer = new Tokenizer(Balie.LANGUAGE_ENGLISH, true);
        tokenizer.Reset(); // ?
        tokenizer.Tokenize(input);

        NamedEntityRecognitionNerf ner = new NamedEntityRecognitionNerf(tokenizer.GetTokenList(), new LexiconOnDisk(LexiconOnDisk.Lexicon.OPEN_SOURCE_LEXICON), DisambiguationRulesNerf.Load(), new PriorCorrectionNerf(), NamedEntityTypeEnumMappingNerf.values(), false);

        ner.RecognizeEntities();

        TokenList tokenList = ner.GetTokenList();
        List<Entity> list = new ArrayList<>();
        TokenListIterator iter = tokenList.Iterator();
        String lastType = null;
        while (iter.HasNext()) {
            Token token = iter.Next();
            String type = token.EntityType().GetLabel(NamedEntityTypeEnumMappingNerf.values());

            if (type != null && EntityClassMap.balie(type) != EntityClassMap.getNullCategory() && type.equals(lastType) && list.size() > 0) {

                list.get(list.size() - 1).addText(token.Raw());

            } else {
                if (EntityClassMap.balie(type) != EntityClassMap.getNullCategory())
                    list.add(getEntiy(token.Raw(), EntityClassMap.balie(type), Entity.DEFAULT_RELEVANCE, getToolName()));
                lastType = type;
            }
        }

        return post(new HashSet<Entity>(list));
    }
}
