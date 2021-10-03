package me.kirantipov.mods.sync.compat.trinkets;

import dev.emi.trinkets.api.LivingEntityTrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import me.kirantipov.mods.sync.entity.ShellEntity;
import net.minecraft.inventory.Inventory;

import java.util.Map;

class ShellEntityTrinketComponent extends LivingEntityTrinketComponent {
    public ShellEntityTrinketComponent(ShellEntity entity) {
        super(entity);
    }

    @Override
    public void update() {
        if (!(this.entity instanceof ShellEntity shellEntity)) {
            return;
        }

        // FFS, please, stop using uninitialized objects!
        if (shellEntity.getState() == null) {
            shellEntity.onInitialized(this::update);
            return;
        }

        super.update();
        TrinketShellStateComponent trinketComponent = shellEntity.getState().getComponent().as(TrinketShellStateComponent.class);
        if (trinketComponent == null) {
            return;
        }

        for (Map.Entry<String, Map<String, Inventory>> groupEntry : trinketComponent.inventory.entrySet()) {
            Map<String, TrinketInventory> targetGroup = this.inventory.get(groupEntry.getKey());
            if (targetGroup == null) {
                continue;
            }

            for (Map.Entry<String, Inventory> slotEntry : groupEntry.getValue().entrySet()) {
                TrinketInventory targetSlot = targetGroup.get(slotEntry.getKey());
                if (targetSlot == null) {
                    continue;
                }

                Inventory slot = slotEntry.getValue();
                int size = Math.min(targetSlot.size(), slot.size());
                for (int i = 0; i < size; ++i) {
                    targetSlot.setStack(i, slot.getStack(i));
                }
            }
        }
    }
}