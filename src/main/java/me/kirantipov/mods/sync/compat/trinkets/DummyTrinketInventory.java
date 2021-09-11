package me.kirantipov.mods.sync.compat.trinkets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Collection;
import java.util.List;

class DummyTrinketInventory extends TrinketInventory {
    public static final DummyTrinketInventory INSTANCE = new DummyTrinketInventory();

    @Override
    public Collection<ItemStack> getItems() {
        return List.of();
    }

    @Override
    public void clear() { }

    @Override
    public void clone(TrinketInventory inventory) { }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) { }
}