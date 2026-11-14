package com.proto781m.wrenchslot;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main entry point for the Wrench Slot mod.
 *
 * <p>Mod ID: {@value #MOD_ID}<br>
 * Loader: NeoForge 1.21.1
 */
@Mod(WrenchSlot.MOD_ID)
public class WrenchSlot {

    /** Mod ID. Must match the modId in neoforge.mods.toml. */
    public static final String MOD_ID = "wrenchslot";

    /** Shared logger. */
    public static final Logger LOGGER = LogUtils.getLogger();

    public WrenchSlot(IEventBus modEventBus, ModContainer modContainer) {
        // Register the common config file.
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Mod-bus events, e.g. registration and setup.
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Wrench Slot initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Runs once during mod loading, before the game starts.
        LOGGER.debug("Wrench Slot common setup complete");
    }
}
