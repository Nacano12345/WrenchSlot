package com.proto781m.wrenchslot;

import com.mojang.logging.LogUtils;
import com.proto781m.wrenchslot.network.WrenchActionPayload;
import com.simibubi.create.AllItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Main entry point for the Wrench Slot mod.
 *
 * <p>Adds a Curios slot that stores the Create wrench and lets the player use it
 * while their main hand is empty.
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

        // Mod-bus events, e.g. setup and network payloads.
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(WrenchSlot::registerPayloads);

        // The Curios wrench slot only accepts Create's wrench.
        ResourceLocation predicateId =
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "create_wrench");
        CuriosApi.registerCurioPredicate(predicateId,
                slotResult -> slotResult.stack().is(AllItems.WRENCH.get()));
        LOGGER.info("Registered Curios wrench predicate {}", predicateId);

        LOGGER.info("Wrench Slot initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.debug("Wrench Slot common setup complete");
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                WrenchActionPayload.TYPE,
                WrenchActionPayload.STREAM_CODEC,
                WrenchActionPayload::handle);
    }
}
