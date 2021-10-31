package dev.kir.sync.client.model;

import dev.kir.sync.util.math.Voxel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public interface VoxelProvider {
    default boolean isUpsideDown() {
        return true;
    }

    Stream<Voxel> getVoxels();
}