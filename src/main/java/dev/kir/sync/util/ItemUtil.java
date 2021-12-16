package dev.kir.sync.util;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public final class ItemUtil {
    public static boolean isArmor(ItemStack itemStack) {
        return isArmor(itemStack.getItem());
    }

    public static boolean isArmor(ItemConvertible item) {
        return DispenserBlock.BEHAVIORS.get(item.asItem()) == ArmorItem.DISPENSER_BEHAVIOR;
    }


    public static EquipmentSlot getPreferredEquipmentSlot(ItemStack itemStack) {
        return MobEntity.getPreferredEquipmentSlot(itemStack);
    }

    public static EquipmentSlot getPreferredEquipmentSlot(ItemConvertible item) {
        return getPreferredEquipmentSlot(new ItemStack(item));
    }
}
