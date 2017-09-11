package org.arquillian.smart.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import static org.arquillian.smart.testing.Configuration.SMART_TESTING_DEBUG;

public class LoggerConfigurator {

    public  static final String SMART_TESTING_LOG_ENABLE = "smart.testing.log.file";

    private static final String DEFAULT_SMART_TESTING_LOG_FILE = getDefaultLogFile();

    private static boolean mavenDebugLogLevel = false;

    private java.util.logging.Logger jul;

    private static String debugLogFile;

    LoggerConfigurator(java.util.logging.Logger logger) {
        this.jul = logger;
        setLogLevel();
    }

    public static Boolean enableDebugLogLevel() {
        return Boolean.valueOf(System.getProperty(SMART_TESTING_DEBUG, "false")) || mavenDebugLogLevel;
    }

    public static void enableMavenDebugLogLevel(Boolean mavenDebugLevel) {
        mavenDebugLogLevel = mavenDebugLevel;
    }

    public static boolean shouldEnableLogFile() {
        String logFile = System.getProperty(SMART_TESTING_LOG_ENABLE);
        if (logFile != null) {
            debugLogFile = logFile;
            return true;
        }
        return false;
    }

    private void setLogLevel() {
        if (enableDebugLogLevel()) {
            jul.setLevel(Level.FINEST);
            if (shouldEnableLogFile()) {
                addFileHandler();
            }
            addConsoleHandler();
        }
    }

    private void addConsoleHandler() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINEST);
        jul.addHandler(consoleHandler);
    }

    private void addFileHandler() {
        try {
            FileHandler fileHandler = new FileHandler(getLogFile());
            fileHandler.setLevel(Level.FINEST);
            jul.addHandler(fileHandler);
            System.out.println("Debug Log stored at " + Paths.get(getLogFile()).toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLogFile() {
        if (debugLogFile == null || debugLogFile.isEmpty()) {
            return DEFAULT_SMART_TESTING_LOG_FILE;
        }
        return debugLogFile;
    }

    private static String getDefaultLogFile() {
        final File logDirectory = new File("target/smart-testing-debug.log");
        logDirectory.getParentFile().mkdir();
        try {
            logDirectory.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logDirectory.getPath();
    }
}
