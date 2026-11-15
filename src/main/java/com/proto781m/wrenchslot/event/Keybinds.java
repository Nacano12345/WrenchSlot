package com.proto781m.wrenchslot.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.proto781m.wrenchslot.WrenchSlot;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings.
 *
 * <p>{@link #MODIFIER} is this mod's primary modifier: the Curios wrench is only
 * used while it is held. Create's original primary modifier (sneaking) becomes
 * the secondary modifier: Ctrl + right-click rotates, Ctrl + Shift + right-click
 * dismantles.
 */
@SuppressWarnings("removal")
@EventBusSubscriber(modid = WrenchSlot.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Keybinds {

    public static final String CATEGORY = "key.categories.wrenchslot";

    public static final KeyMapping MODIFIER = new KeyMapping(
            "key.wrenchslot.modifier",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY);

    private Keybinds() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(MODIFIER);
    }

    /**
     * @return whether this mod's primary modifier is currently held. When the
     *         key bind is still on a Ctrl key, both left and right Ctrl work.
     */
    public static boolean isModifierDown() {
        if (MODIFIER.isDown()) {
            return true;
        }
        InputConstants.Key key = MODIFIER.getKey();
        if (key.getType() == InputConstants.Type.KEYSYM
                && (key.getValue() == GLFW.GLFW_KEY_LEFT_CONTROL
                    || key.getValue() == GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            return Screen.hasControlDown();
        }
        return false;
    }
}
