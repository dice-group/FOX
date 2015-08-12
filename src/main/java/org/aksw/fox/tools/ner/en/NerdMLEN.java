package org.aksw.fox.tools.ner.en;

import java.util.ArrayList;
import java.util.List;

import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;

import fr.eurecom.nerd.client.NERD;
import fr.eurecom.nerd.client.schema.Entity;
import fr.eurecom.nerd.client.type.DocumentType;
import fr.eurecom.nerd.client.type.ExtractorType;

/*
"idEntity":10428853,
"label":"Leipzig",
"extractorType":null,
"nerdType":"http://nerd.eurecom.fr/ontology#Location",
"uri":"http://dbpedia.org/resource/Leipzig",
"confidence":0.206021,
"relevance":0.5,
"extractor":"nerdml",
"startChar":54,
"endChar":61


 */

public class NerdMLEN extends AbstractNER {

    public static final String CFG_KEY_API_KEY       = NerdMLEN.class.getName().concat(".apiKey");
    public static final String CFG_KEY_ExtractorType = NerdMLEN.class.getName().concat(".extractorType");
    public static final String CFG_KEY_DocumentType  = NerdMLEN.class.getName().concat(".documentType");

    public static void main(String[] a) {

        NerdMLEN nerdml = new NerdMLEN();
        LOG.info(nerdml.retrieve("@Pretty_Since naa,im going too be chilling w. mad heads."));
        /*
        [text=Gottfried Wilhelm Leibniz, type=http://nerd.eurecom.fr/ontology#Person, tool=NERNerdML, relevance=0.5]
        */
    }

    @Override
    public List<org.aksw.fox.data.Entity> retrieve(String input) {
        List<org.aksw.fox.data.Entity> foxlist = new ArrayList<>();

        NERD nerd = new NERD(FoxCfg.get(CFG_KEY_API_KEY));
        List<Entity> nerdlist = nerd.annotate(
                // ExtractorType.NERDML,
                ExtractorType.valueOf(FoxCfg.get(CFG_KEY_ExtractorType)),
                DocumentType.valueOf(FoxCfg.get(CFG_KEY_DocumentType)),
                input
                );
        /* 
         for (Entity e : nerdlist)
             System.out.println(e.getUri());
             */

        for (Entity entity : nerdlist) {
            foxlist.add(getEntity(entity.getLabel(), entity.getNerdType(), entity.getRelevance().floatValue(), getToolName()));
        }
        return foxlist;
    }
}
