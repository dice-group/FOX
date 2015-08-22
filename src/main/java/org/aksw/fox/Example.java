package org.aksw.fox;

import java.util.Map;
import java.util.Set;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.ToolsGenerator;
import org.aksw.fox.tools.ner.en.StanfordEN;
import org.aksw.fox.utils.FoxCfg;
import org.apache.jena.riot.Lang;
import org.apache.log4j.PropertyConfigurator;

public class Example {
    /**
     * Example of programmatic use of FOX
     * 
     * @param args
     *            no arguments
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);

        String lang = "en";

        Set<String> langs = ToolsGenerator.usedLang;
        if (!langs.contains(lang))
            System.out.println("language not supported");
        else {
            // init fox
            Fox fox = new Fox(lang);

            // set parameters
            Map<String, String> defaults = fox.getDefaultParameter();
            defaults.put(Fox.parameter_output, Lang.TURTLE.getName());
            fox.setParameter(defaults);

            // fox light version
            String tool = StanfordEN.class.getName();
            Set<Entity> e;
            if (!ToolsGenerator.nerTools.get(lang).contains(tool))
                System.out.println("can't find the given tool " + tool);
            e = fox.doNERLight(tool);
            // e = fox.doNER();

            // linking
            fox.setURIs(e);

            // output
            fox.setOutput(e, null);

            System.out.println(fox.getResults());
        }
    }
}
