package com.proto781m.wrenchslot.mixin;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets Create's "Open Block Rotation Menu" key bind work while the wrench is
 * stored in the Curios slot and the player's hand is empty.
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
        if (!mainHand.isEmpty() || !Config.rotationMenuEnabled()) {
            return mainHand;
        }
        return CurioWrench.find(player).orElse(mainHand);
    }
}
