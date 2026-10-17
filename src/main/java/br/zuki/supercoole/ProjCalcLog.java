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

    public static void info(String message) {
        write("INFO", message);
        SuperCoolE.LOGGER.info(message);
    }

    public static void warn(String message) {
        write("WARN", message);
        SuperCoolE.LOGGER.warn(message);
    }

    public static void error(String message, Throwable t) {
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
