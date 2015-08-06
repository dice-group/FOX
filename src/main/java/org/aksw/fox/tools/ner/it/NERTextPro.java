package org.aksw.fox.tools.ner.it;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.fox.data.Entity;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class NERTextPro extends AbstractNER {

    public static final String               CFG_KEY_TEXTPRO_PATH = NERTextPro.class.getName().concat(".path");

    private static final String              TEXTPRO_PATH         = FoxCfg.get(CFG_KEY_TEXTPRO_PATH);
    private static final Map<String, String> ENTITY_MAP           = new HashMap<String, String>()
                                                                  {
                                                                      {
                                                                          put("ORG", "ORGANIZATION");
                                                                          put("LOC", "LOCATION");
                                                                          put("PER", "PERSON");
                                                                          put("GPE", "LOCATION"); /*@to discuss: leave out or not?
                                                                                                  //GPE = geo-political entity
                                                                                                  //not necessarily location, however in most cases*/
                                                                      }
                                                                  };

    @Override
    public List<Entity> retrieve(String input) {
        LOG.info("writing input to temporary file");

        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(TEXTPRO_PATH + "/FoxInput.tmp"), "utf-8"));
            writer.write(input);
        } catch (IOException e) {
            LOG.error("\n", e);
        } finally
        {
            try {
                writer.close();
            } catch (Exception e) {
                LOG.error("\n", e);
            }
        }

        LOG.info("executing command prompt request");

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(TEXTPRO_PATH
                    + "/./textpro.sh -l ita -c token+entity -o"
                    + TEXTPRO_PATH + " "
                    + TEXTPRO_PATH + "/FoxInput.tmp");
        } catch (IOException e) {
            LOG.error(e);
        }
        try {
            int exitVal = p.waitFor();
        } catch (InterruptedException e) {
            LOG.error(e);
        }

        LOG.info("reading request answer output");

        String answer = null;

        try
        {
            File file = new File(TEXTPRO_PATH + "/FoxInput.tmp.txp");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            Integer entity_ID = 0;

            Pattern p_entity = Pattern.compile(".*[BI]-(ORG|LOC|PER|GPE)$");
            Pattern p_entity_start = Pattern.compile(".*B-(ORG|LOC|PER|GPE)$");

            while ((line = bufferedReader.readLine()) != null) {
                Matcher m_entity = p_entity.matcher(line);
                Matcher m_entity_start = p_entity_start.matcher(line);

                if (!line.startsWith("#") && m_entity.matches())
                {
                    if (m_entity_start.matches())
                        entity_ID++;

                    stringBuffer.append(entity_ID + "\t" + line);
                    stringBuffer.append("\n");
                }
            }

            fileReader.close();

            answer = stringBuffer.toString();
        } catch (IOException e) {
            LOG.error("\n", e);
        }

        LOG.info("deleting temporary files");

        try {
            p = Runtime.getRuntime().exec("rm " + TEXTPRO_PATH + "/tmp/FoxInput.tmp"
                    + " && rm " + TEXTPRO_PATH + "/tmp/FoxInput.tmp.txp");
        } catch (IOException e) {
            LOG.error(e);
        }
        try {
            int exitVal = p.waitFor();
        } catch (InterruptedException e) {
            LOG.error(e);
        }

        LOG.info("creating entity list");

        List<Entity> entity_list = new ArrayList<Entity>();
        Integer mode = 0;
        Integer current_entity_ID = 0;
        String current_entity_type = "";
        String current_entity = "";
        Boolean is_part = false;

        for (String retval : answer.split("(\t|\n)"))
        {
            switch (mode)
            {
            case 0:
                if (Integer.parseInt(retval) == current_entity_ID)
                {
                    is_part = true;
                }
                else
                {
                    is_part = false;
                    current_entity_ID = Integer.parseInt(retval);
                }
                break;

            case 1:
                if (is_part) {
                    current_entity += " " + retval;
                }
                else
                {
                    entity_list.add(new Entity(current_entity,
                            current_entity_type,
                            1,
                            "NERTextPro"));
                    current_entity = retval;
                }
                break;

            case 2:
                current_entity_type = ENTITY_MAP.get(retval.substring(2));
                break;
            }

            mode = (mode + 1) % 3;
        }

        entity_list.add(new Entity(current_entity,
                current_entity_type,
                1,
                "NERTextPro"));
        entity_list.remove(0);
        return entity_list;
    }

    public static void main(String[] a) {

        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        try
        {
            for (Entity e : new NERTextPro().retrieve(FoxConst.NER_IT_EXAMPLE_1))
                LOG.info(e);
        } catch (NullPointerException e) {
            LOG.info("no entities found");
        }
    }
}
