package org.cs440;

public class Log {
    public enum Level {
        INFO(0), 
        DEBUG(1),
        WARNING(1), 
        ERROR(1);

        public final int PRIORITY;

        Level(int priority) {
            PRIORITY = priority;
        }
    }

    public final String LOGGER;
    private Level level;

    Log(String logger, Level level) {
        LOGGER = logger;
        this.level = level;
    }

    Log(String logger) {
        LOGGER = logger;
        this.level = Level.INFO;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private void log(Level level, String message) {
        synchronized(this) {
            if (level.PRIORITY <= this.level.PRIORITY) {
                System.out.print("\033[2K"); // Clear line in case of previous log
                System.out.printf("%s.%s:\t%s\n", LOGGER, level, message);
                System.out.flush();
            }
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
}
