package me.kirantipov.mods.sync.enery;

import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import me.kirantipov.mods.sync.block.entity.SyncBlockEntities;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class EnergyRegistry {
    private static <T extends BlockEntity & EnergyIo> void register(BlockEntityType<T> blockEntityType) {
        EnergyApi.SIDED.registerForBlockEntities((be, __) -> (EnergyIo)be, blockEntityType);
    }

    public static void init() {
        register(SyncBlockEntities.TREADMILL);
        register(SyncBlockEntities.SHELL_CONSTRUCTOR);
    }
}
