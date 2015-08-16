package org.aksw.fox.tools.ner.it;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.fox.data.Entity;
import org.aksw.fox.data.EntityClassMap;
import org.aksw.fox.tools.ner.AbstractNER;
import org.aksw.fox.utils.CfgManager;
import org.aksw.fox.utils.FoxCfg;
import org.aksw.fox.utils.FoxConst;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.PropertyConfigurator;

public class TextProIT extends AbstractNER {

    public static final XMLConfiguration    CFG                  = CfgManager.getCfg(TextProIT.class);
    public static final Charset             UTF_8                = Charset.forName("UTF-8");

    public static final String              CFG_KEY_TEXTPRO_PATH = "textPro.path";
    public static final String              CFG_KEY_TMP_FOLDER   = "textPro.tmpFolder";

    public static final String              TEXTPRO_PATH         = CFG.getString(CFG_KEY_TEXTPRO_PATH);
    public static final String              TMP_FOLDER           = CFG.getString(CFG_KEY_TMP_FOLDER);

    public static final Map<String, String> ENTITY_MAP           = new HashMap<>();
    static {
        ENTITY_MAP.put("ORG", EntityClassMap.O);
        ENTITY_MAP.put("LOC", EntityClassMap.L);
        ENTITY_MAP.put("PER", EntityClassMap.P);
        ENTITY_MAP.put("GPE", EntityClassMap.L);
    };

    /**
     * 
     */
    @Override
    public List<Entity> retrieve(String input) {
        LOG.debug(input);
        String file = TMP_FOLDER + File.separator + "input" + UUID.randomUUID().toString() + ".tmp";
        writeInputFile(input, file);
        runTextPro(file);
        String answer = readResultFile(file);
        // removeFiles(file);
        return createEntities(answer);
    }

    /**
     * 
     * @param input
     * @param filename
     */
    protected void writeInputFile(String input, String filename) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(filename),
                            UTF_8.name()));
            writer.write(input);

        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * 
     * @param filename
     */
    protected void runTextPro(String filename) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(
                    TEXTPRO_PATH
                            + "/./textpro.sh -l ita -c token+entity -o"
                            + TMP_FOLDER + " "
                            + filename);
            int exitVal = p.waitFor();
        } catch (IOException | InterruptedException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 
     * @param file
     * @return
     */
    protected String readResultFile(String file) {
        String answer = null;
        try {
            FileReader fileReader = new FileReader(new File(file + ".txp"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            Integer entity_ID = 0;

            Pattern p_entity = Pattern.compile(".*[BI]-(ORG|LOC|PER|GPE)$");
            Pattern p_entity_start = Pattern.compile(".*B-(ORG|LOC|PER|GPE)$");

            while ((line = bufferedReader.readLine()) != null) {
                Matcher m_entity = p_entity.matcher(line);
                Matcher m_entity_start = p_entity_start.matcher(line);

                if (!line.startsWith("#") && m_entity.matches()) {
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
        LOG.debug(answer);
        return answer;
    }

    /**
     * 
     * @param file
     */
    protected void removeFiles(String file) {
        new File(file).delete();
        new File(file + ".txp").delete();
    }

    /**
     * 
     * @param answer
     * @return
     */
    protected List<Entity> createEntities(String answer) {
        entityList = new ArrayList<Entity>();

        int mode = 0;
        int current_entity_ID = 0;
        String current_entity_type = "";
        String current_entity = "";
        boolean is_part = false;

        for (String retval : answer.split("(\t|\n)")) {
            switch (mode) {
            case 0:
                if (Integer.parseInt(retval) == current_entity_ID) {
                    is_part = true;
                }
                else {
                    is_part = false;
                    current_entity_ID = Integer.parseInt(retval);
                }
                break;

            case 1:
                if (is_part) {
                    current_entity += " " + retval;
                }
                else {
                    entityList.add(new Entity(current_entity,
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

        entityList.add(new Entity(current_entity,
                current_entity_type,
                1,
                getToolName()));

        entityList.remove(0);
        return entityList;
    }

    public static void main(String[] a) throws IOException {
        PropertyConfigurator.configure(FoxCfg.LOG_FILE);
        LOG.info(new TextProIT().retrieve(FoxConst.NER_IT_EXAMPLE_1));
    }
}
