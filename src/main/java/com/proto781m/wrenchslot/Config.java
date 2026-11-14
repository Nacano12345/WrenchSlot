package com.proto781m.wrenchslot;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config options, loaded from {@code config/wrenchslot-common.toml}.
 *
 * <p>Add your own options inside the {@code general} section, or create
 * additional sections with {@link ModConfigSpec.Builder#push(String)}.
 */
public final class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue EXAMPLE_VALUE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");

        ENABLED = BUILDER
                .comment("Whether the mod's gameplay features are enabled.")
                .define("enabled", true);

        EXAMPLE_VALUE = BUILDER
                .comment("An example integer option. Replace or remove it.")
                .defineInRange("exampleValue", 10, 0, 1000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }
}
