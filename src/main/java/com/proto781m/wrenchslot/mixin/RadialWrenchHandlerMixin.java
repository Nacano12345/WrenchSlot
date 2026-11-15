package com.proto781m.wrenchslot.mixin;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.event.Keybinds;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets Create's "Open Block Rotation Menu" key bind work while the wrench is
 * stored in the Curios slot, as long as this mod's primary modifier is held.
 */
@Mixin(value = RadialWrenchHandler.class, remap = false)
public abstract class RadialWrenchHandlerMixin {

    @Redirect(
            method = "onKeyInput",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack wrenchslot$useCurioWrenchForRotationMenu(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if ((!mainHand.isEmpty() && !Config.allowNonEmptyHand())
                || !Config.rotationMenuEnabled()
                || !Keybinds.MODIFIER.isDown()) {
            return mainHand;
        }
        return CurioWrench.find(player).orElse(mainHand);
    }
}
