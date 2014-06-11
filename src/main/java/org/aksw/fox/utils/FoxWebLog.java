package org.aksw.fox.utils;

import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;

public class FoxWebLog {

    private Logger       logger        = Logger.getLogger(FoxWebLog.class);
    private StringWriter consoleWriter = new StringWriter();

    /**
     * 
     */
    public FoxWebLog() {

        // add appender to log
        WriterAppender appender = new WriterAppender(new PatternLayout("%d{HH:mm:ss} - %m%n"), consoleWriter);
        appender.setName("CONSOLE_APPENDER");
        appender.setThreshold(Level.INFO);
        logger.removeAllAppenders();
        logger.addAppender(appender);
    }

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        logger.info(message);
    }

    /**
     * 
     * @return
     */
    public String getConsoleOutput() {
        return consoleWriter.toString();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

        FoxWebLog foxWebLog = new FoxWebLog();
        foxWebLog.setMessage("That is a test message.");
        foxWebLog.setMessage("That is an other test message.");

        System.out.println("==============================");
        System.out.print(foxWebLog.getConsoleOutput());
        System.out.println("==============================");
    }
}
