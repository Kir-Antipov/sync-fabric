package dev.kir.sync.enery;

import dev.kir.sync.block.entity.SyncBlockEntities;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import team.reborn.energy.api.EnergyStorage;

public class EnergyRegistry {
    private static <T extends BlockEntity & EnergyStorage> void register(BlockEntityType<T> blockEntityType) {
        EnergyStorage.SIDED.registerForBlockEntities((be, __) -> (EnergyStorage)be, blockEntityType);
    }

    public static void init() {
        register(SyncBlockEntities.TREADMILL);
        register(SyncBlockEntities.SHELL_CONSTRUCTOR);
    }
}
