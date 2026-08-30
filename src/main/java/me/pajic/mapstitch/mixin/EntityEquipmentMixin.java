package me.pajic.mapstitch.mixin;

//? >=26.1 {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityEquipment.class)
public class EntityEquipmentMixin {

    @WrapOperation(
            method = "dropAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity preventAtlasDrop(LivingEntity instance, ItemStack stack, boolean randomly, boolean thrownFromHand, Operation<ItemEntity> original) {
        return MapStitch.CONFIG.keepAtlasOnDeath.get() && stack.is(ModItems.ATLAS) ? null : original.call(instance, stack, randomly, thrownFromHand);
    }

    @ModifyExpressionValue(
            method = "lambda$clear$0",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/ItemStack;EMPTY:Lnet/minecraft/world/item/ItemStack;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static ItemStack preventAtlasRemoval(ItemStack original, @Local(argsOnly = true) ItemStack v) {
        return MapStitch.CONFIG.keepAtlasOnDeath.get() && v.is(ModItems.ATLAS) ? v : original;
    }
}
//?}
