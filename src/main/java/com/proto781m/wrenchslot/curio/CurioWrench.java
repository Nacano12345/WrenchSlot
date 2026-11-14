package com.proto781m.wrenchslot.curio;

import com.simibubi.create.AllItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

/**
 * Helpers for looking up the Create wrench equipped in a Curios slot.
 */
public final class CurioWrench {

    private CurioWrench() {
    }

    /**
     * @return the equipped Create wrench, or an empty optional if there is none
     */
    public static Optional<ItemStack> find(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.is(AllItems.WRENCH.get())))
                .map(SlotResult::stack);
    }

    public static boolean has(LivingEntity entity) {
        return find(entity).isPresent();
    }
}
