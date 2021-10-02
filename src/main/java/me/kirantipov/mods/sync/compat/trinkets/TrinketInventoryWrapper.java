package me.kirantipov.mods.sync.compat.trinkets;

import dev.emi.trinkets.api.SlotGroup;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketsApi;
import me.kirantipov.mods.sync.item.SimpleInventory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.*;

class TrinketInventoryWrapper extends TrinketInventory {
    final Map<String, Map<String, Inventory>> inventory;

    @SuppressWarnings("unchecked")
    public TrinketInventoryWrapper(LivingEntity entity) {
        this.inventory = TrinketsApi.getTrinketComponent(entity).map(x -> (Map<String, Map<String, Inventory>>)(Object)x.getInventory()).orElseGet(HashMap::new);
    }

    public TrinketInventoryWrapper(EntityType<?> entityType) {
        this.inventory = new HashMap<>();
        for (Map.Entry<String, SlotGroup> groupEntry : TrinketsApi.getEntitySlots(entityType).entrySet()) {
            Map<String, Inventory> group = this.inventory.computeIfAbsent(groupEntry.getKey(), x -> new HashMap<>());
            for (Map.Entry<String, SlotType> slotEntry : groupEntry.getValue().getSlots().entrySet()) {
                group.put(slotEntry.getKey(), new SimpleInventory());
            }
        }
    }

    @Override
    public Collection<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Inventory inv : (Iterable<Inventory>)this.inventory.values().stream().flatMap(x -> x.values().stream())::iterator) {
            for (int i = 0; i < inv.size(); ++i) {
                ItemStack stack = inv.getStack(i);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        return items;
    }

    @Override
    public void clear() {
        this.inventory.values().forEach(x -> x.values().forEach(Inventory::clear));
    }

    @Override
    public void clone(TrinketInventory inventory) {
        this.clear();
        if (!(inventory instanceof TrinketInventoryWrapper wrapper)) {
            return;
        }

        for (Map.Entry<String, Map<String, Inventory>> sourceGroupEntry : wrapper.inventory.entrySet()) {
            Map<String, Inventory> targetGroup = this.inventory.get(sourceGroupEntry.getKey());
            if (targetGroup == null) {
                continue;
            }

            for (Map.Entry<String, Inventory> sourceSlotEntry : sourceGroupEntry.getValue().entrySet()) {
                Inventory targetSlot = targetGroup.get(sourceSlotEntry.getKey());
                if (targetSlot == null) {
                    continue;
                }

                Inventory sourceSlot = sourceSlotEntry.getValue();
                int size = Math.min(sourceSlot.size(), targetSlot.size());
                for (int i = 0; i < size; ++i) {
                    targetSlot.setStack(i, sourceSlot.getStack(i));
                }
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (Map.Entry<String, Map<String, Inventory>> group : this.inventory.entrySet()) {
            NbtCompound groupTag = new NbtCompound();
            for (Map.Entry<String, Inventory> slot : group.getValue().entrySet()) {
                NbtCompound slotTag = new NbtCompound();
                NbtList list = new NbtList();
                Inventory inv = slot.getValue();
                for (int i = 0; i < inv.size(); i++) {
                    list.add(inv.getStack(i).writeNbt(new NbtCompound()));
                }
                slotTag.put("Items", list);
                groupTag.put(slot.getKey(), slotTag);
            }
            nbt.put(group.getKey(), groupTag);
        }
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        for (String groupKey : nbt.getKeys()) {
            NbtCompound groupTag = nbt.getCompound(groupKey);
            Map<String, Inventory> groupSlots = this.inventory.get(groupKey);
            if (groupTag == null || groupSlots == null) {
                continue;
            }

            for (String slotKey : groupTag.getKeys()) {
                NbtCompound slotTag = groupTag.getCompound(slotKey);
                NbtList list = slotTag.getList("Items", NbtType.COMPOUND);
                Inventory inv = groupSlots.get(slotKey);
                if (inv == null) {
                    continue;
                }

                int size = Math.min(list.size(), inv.size());
                for (int i = 0; i < size; i++) {
                    ItemStack stack = ItemStack.fromNbt(list.getCompound(i));
                    inv.setStack(i, stack);
                }
            }
        }
    }
}