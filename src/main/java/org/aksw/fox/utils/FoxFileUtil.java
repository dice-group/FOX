package org.aksw.fox.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author rspeck
 * 
 */
public class FoxFileUtil {

    public static Logger logger = Logger.getLogger(FoxFileUtil.class);

    private FoxFileUtil() {
    }

    /**
     * creates path structure ignores the file
     */
    public static synchronized void createFileStructure(String file) {
        String path = FilenameUtils.getPath(file);
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            logger.error("\n", e);
        }
    }

}
