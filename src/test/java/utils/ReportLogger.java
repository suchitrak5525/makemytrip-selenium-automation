package utils;

public class ReportLogger {

    private static final ThreadLocal<StringBuilder> LOGS =
            ThreadLocal.withInitial(StringBuilder::new);

    private ReportLogger() {
    }

    public static void log(String message) {
        LOGS.get().append(message).append(System.lineSeparator());
    }

    public static String getLogs() {
        return LOGS.get().toString();
    }

    public static void clear() {
        LOGS.remove();
    }
}