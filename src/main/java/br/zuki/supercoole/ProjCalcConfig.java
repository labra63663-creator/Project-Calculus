package br.zuki.supercoole;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProjCalcConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue CACHE_TTL_MS;
    public static final ModConfigSpec.BooleanValue VERBOSE_LOGS;
    public static final ModConfigSpec.BooleanValue TEAM_SHARING;
    public static final ModConfigSpec.ConfigValue<String> LOG_LEVEL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CACHE_TTL_MS = builder
                .comment("Cache TTL (ms) for module entries queried by SCC.")
                .defineInRange("cacheTtlMs", 250, 50, 5000);
        VERBOSE_LOGS = builder
                .comment("Enable verbose Project Calculus logs.")
                .define("verboseLogs", true);
        TEAM_SHARING = builder
                .comment("Enable scoreboard team sharing for EMC/knowledge.")
                .define("teamSharing", true);
        LOG_LEVEL = builder
                .comment("Log level: DEBUG, INFO, WARN, ERROR.")
                .defineInList("logLevel", "INFO", java.util.List.of("DEBUG", "INFO", "WARN", "ERROR"));
        SPEC = builder.build();
    }

    public static int cacheTtlMs() {
        try {
            return CACHE_TTL_MS.get();
        } catch (Throwable ignored) {
            return 250;
        }
    }

    public static boolean verboseLogs() {
        try {
            return VERBOSE_LOGS.get();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static boolean teamSharing() {
        try {
            return TEAM_SHARING.get();
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static String logLevel() {
        try {
            return LOG_LEVEL.get();
        } catch (Throwable ignored) {
            return "INFO";
        }
    }

    private ProjCalcConfig() {}
}
