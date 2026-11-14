package com.proto781m.wrenchslot.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.proto781m.wrenchslot.WrenchSlot;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings. Right-clicking with an empty hand rotates; holding the
 * dismantle key while right-clicking dismantles instead.
 */
@SuppressWarnings("removal")
@EventBusSubscriber(modid = WrenchSlot.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Keybinds {

    public static final String CATEGORY = "key.categories.wrenchslot";

    public static final KeyMapping DISMANTLE = new KeyMapping(
            "key.wrenchslot.dismantle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY);

    private Keybinds() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(DISMANTLE);
    }
}
