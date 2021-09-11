package me.kirantipov.mods.sync.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Collection;

public abstract class TrinketInventory {
    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    public static TrinketInventory of(LivingEntity entity) {
        return TRINKETS_LOADED ? new TrinketInventoryWrapper(entity) : DummyTrinketInventory.INSTANCE;
    }

    public static TrinketInventory empty(EntityType<? extends LivingEntity> entityType) {
        return TRINKETS_LOADED ? new TrinketInventoryWrapper(entityType) : DummyTrinketInventory.INSTANCE;
    }


    public abstract Collection<ItemStack> getItems();

    public abstract void clear();

    public abstract void clone(TrinketInventory inventory);

    public abstract NbtCompound writeNbt(NbtCompound nbt);

    public abstract void readNbt(NbtCompound nbt);
}