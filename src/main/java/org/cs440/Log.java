package org.cs440;

public class Log {
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
        if (level.PRIORITY <= this.level.PRIORITY) {
            System.out.print("\033[2K\r");
            System.out.printf("%s.%s:\t%s\n", LOGGER, level, message);
            System.out.print("\033[J");
            System.out.flush();
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
