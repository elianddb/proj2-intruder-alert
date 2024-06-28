package org.cs440;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Log {
    public final String LOGGER;
    private Level level;
    private List<String> logMessages = new ArrayList<>();

    Log(String logger, Level level) {
        LOGGER = logger;
        this.level = level;
    }

    Log(String logger) {
        LOGGER = logger;
        this.level = Level.INFO;
    }

    public boolean is(Level level) {
        return level == this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private synchronized void log(Level level, String message) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        String className = stacktrace[3].getClassName();
        String caller = stacktrace[3].getMethodName();
        if (level.PRIORITY <= this.level.PRIORITY) {
            System.out.print("\033[0J"); // Clear lines from the cursor to the end of the screen
            String header = String.format("%s.%s::%s.%s():", LOGGER, level, className, caller);
            String logMessage = String.format("%-60s\t%s", header, message);
            System.out.println(logMessage);
            System.out.flush();
            logMessages.add(logMessage);
        }
    }

    public void writeTo(String filename) {
        try (FileWriter writer = new FileWriter(filename + ".log")) {
            for (String logMessage : logMessages) {
                writer.write(logMessage + "\n");
            }
        } catch (IOException e) {
            error("Failed to write log messages to file: " + e.getMessage());
        }
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void warn(String message) {
        log(Level.WARNING, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public enum Level {
        INFO(0), 
        WARNING(1), 
        DEBUG(2),
        ERROR(2);

        public final int PRIORITY;

        Level(int priority) {
            PRIORITY = priority;
        }
    }
}
