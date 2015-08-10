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
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.log4j.PropertyConfigurator;

public class TextProIT extends AbstractNER {

    public static final String               CFG_KEY_TEXTPRO_PATH = TextProIT.class.getName().concat(".path");
    private static final String              TEXTPRO_PATH         = FoxCfg.get(CFG_KEY_TEXTPRO_PATH);

    private static final Map<String, String> ENTITY_MAP           = new HashMap<>();
    static {
        ENTITY_MAP.put("ORG", EntityClassMap.O);
        ENTITY_MAP.put("LOC", EntityClassMap.L);
        ENTITY_MAP.put("PER", EntityClassMap.P);
        ENTITY_MAP.put("GPE", EntityClassMap.L);
    };

    @Override
    public List<Entity> retrieve(String input) {
        LOG.info("writing input to temporary file");

        Writer writer = null;
        LOG.info(input);
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(TEXTPRO_PATH + "/FoxInput.tmp"), "utf-8"));
            writer.write(input);

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally
        {
            try {
                writer.close();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        LOG.info("executing command prompt request");

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(
                    TEXTPRO_PATH
                            + "/./textpro.sh -l ita -c token+entity -o"
                            + TEXTPRO_PATH + " "
                            + TEXTPRO_PATH + "/FoxInput.tmp");

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
        }
        try {
            int exitVal = p.waitFor();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }

        LOG.info("reading request answer output");

        String answer = null;
        try {
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
            LOG.error(e.getLocalizedMessage());
        }

        LOG.info("deleting temporary files");

        try {
            p = Runtime.getRuntime().exec(
                    "rm " + TEXTPRO_PATH + "/tmp/FoxInput.tmp  && " +
                            "rm " + TEXTPRO_PATH + "/tmp/FoxInput.tmp.txp");

            int exitVal = p.waitFor();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
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
                            getToolName()));
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
                getToolName()));
        entity_list.remove(0);
        return entity_list;
    }

    public static void main(String[] a) throws IOException {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new TextProIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
    }
}
