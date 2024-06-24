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
            String header = String.format("%s.%s.%s.%s():", LOGGER, level, className, caller);
            System.out.printf("%60s\t%s\n", header, message);
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
