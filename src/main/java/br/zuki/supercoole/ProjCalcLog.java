package br.zuki.supercoole;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ProjCalcLog {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final File LOG_DIR = new File("logs", "projcalc");
    private static final File LOG_FILE;

    static {
        String name = "projcalc-" + LocalDateTime.now().format(FILE_FMT) + ".log";
        LOG_FILE = new File(LOG_DIR, name);
    }

    private ProjCalcLog() {}

    public static void debug(String message) {
        if (!shouldLog("DEBUG")) return;
        write("DEBUG", message);
        SuperCoolE.LOGGER.debug(message);
    }

    public static void info(String message) {
        if (!shouldLog("INFO")) return;
        write("INFO", message);
        SuperCoolE.LOGGER.info(message);
    }

    private static boolean shouldLog(String level) {
        if (!ProjCalcConfig.verboseLogs() && ("DEBUG".equals(level) || "INFO".equals(level))) {
            return false;
        }
        String configured = ProjCalcConfig.logLevel();
        int configuredRank = rank(configured);
        int messageRank = rank(level);
        return messageRank >= configuredRank;
    }

    private static int rank(String level) {
        if ("DEBUG".equalsIgnoreCase(level)) return 0;
        if ("INFO".equalsIgnoreCase(level)) return 1;
        if ("WARN".equalsIgnoreCase(level)) return 2;
        if ("ERROR".equalsIgnoreCase(level)) return 3;
        return 1;
    }

    public static void warn(String message) {
        if (!shouldLog("WARN")) return;
        write("WARN", message);
        SuperCoolE.LOGGER.warn(message);
    }

    public static void error(String message, Throwable t) {
        if (!shouldLog("ERROR")) return;
        write("ERROR", message + (t == null ? "" : " :: " + t));
        SuperCoolE.LOGGER.error(message, t);
    }

    private static synchronized void write(String level, String message) {
        try {
            if (!LOG_DIR.exists()) LOG_DIR.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                pw.println("[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + message);
            }
        } catch (Throwable ignored) {
        }
    }

    public static File currentFile() {
        return LOG_FILE;
    }
}
