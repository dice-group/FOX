package org.aksw.fox.nerlearner.reader;

import org.aksw.fox.data.exception.LoadingNotPossibleException;
import org.aksw.fox.utils.FoxCfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NERReaderFactory {

    public static final Logger LOG             = LogManager.getLogger(NERReaderFactory.class);
    public final static String INER_READER_KEY = NERReaderFactory.class.getName().concat(".readerclass");

    public static INERReader getINERReader() throws LoadingNotPossibleException {

        if (FoxCfg.get(INER_READER_KEY) != null) {
            String classes = FoxCfg.get(INER_READER_KEY).trim();
            return (INERReader) FoxCfg.getClass(classes);
        } else
            return null;
    }
}
