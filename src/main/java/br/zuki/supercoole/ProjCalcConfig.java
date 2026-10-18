package br.zuki.supercoole;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProjCalcConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue CACHE_TTL_MS;
    public static final ModConfigSpec.BooleanValue VERBOSE_LOGS;
    public static final ModConfigSpec.BooleanValue TEAM_SHARING;

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
        SPEC = builder.build();
    }

    private ProjCalcConfig() {}
}
