package me.kirantipov.mods.sync.api.enery;

import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import me.kirantipov.mods.sync.block.entity.SyncBlockEntities;
import net.minecraft.block.entity.BlockEntityType;

public class EnergyRegistry {
    private static void register(BlockEntityType<?> blockEntityType) {
        EnergyApi.SIDED.registerForBlockEntities((be, c) -> be instanceof EnergyIo energyIo ? energyIo : null, blockEntityType);
    }

    static {
        register(SyncBlockEntities.TREADMILL);
        register(SyncBlockEntities.SHELL_CONSTRUCTOR);
    }

    public static void init() { }
}