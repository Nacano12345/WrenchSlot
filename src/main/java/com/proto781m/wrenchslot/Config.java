package com.proto781m.wrenchslot;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config options, loaded from {@code config/wrenchslot-common.toml}.
 */
public final class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue ALLOW_NON_EMPTY_HAND;
    public static final ModConfigSpec.BooleanValue ENABLE_MINECART_KILL;
    public static final ModConfigSpec.BooleanValue ENABLE_ROTATION_MENU;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");

        ENABLED = BUILDER
                .comment("Allow the Create wrench stored in the Curios wrench slot to be used.")
                .define("enabled", true);

        ALLOW_NON_EMPTY_HAND = BUILDER
                .comment("Allow wrench actions while holding an item in the main hand.",
                        "When false (default), the main hand must be empty.")
                .define("allowNonEmptyHand", false);

        ENABLE_MINECART_KILL = BUILDER
                .comment("Attacks instantly destroy minecarts, just like the Create wrench.",
                        "Requires an empty main hand unless allowNonEmptyHand is enabled.")
                .define("enableMinecartKill", true);

        ENABLE_ROTATION_MENU = BUILDER
                .comment("Allow Create's block rotation menu to open while the wrench is in the Curios slot.")
                .define("enableRotationMenu", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }

    public static boolean enabled() {
        return get(ENABLED);
    }

    public static boolean allowNonEmptyHand() {
        return get(ALLOW_NON_EMPTY_HAND);
    }

    public static boolean minecartKillEnabled() {
        return get(ENABLE_MINECART_KILL);
    }

    public static boolean rotationMenuEnabled() {
        return get(ENABLE_ROTATION_MENU);
    }

    /**
     * Config values may be accessed before the config is loaded (for example
     * during very early client initialisation), so fall back to the default.
     */
    private static boolean get(ModConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (RuntimeException ex) {
            return true;
        }
    }
}
