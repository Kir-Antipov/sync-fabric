package dev.kir.sync.util.client.render;

import dev.kir.sync.util.math.Voxel;
import dev.kir.sync.util.math.VoxelIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Environment(EnvType.CLIENT)
public final class ModelUtil {
    public static ModelPart copy(ModelPart original) {
        ModelPart copy = new ModelPart(original.cuboids, original.children);
        copy.copyTransform(original);
        return copy;
    }

    public static Stream<Voxel> asVoxels(ModelPart part) {
        return asVoxels(0, 0, 0, part);
    }

    public static Stream<Voxel> asVoxels(float x, float y, float z, ModelPart part) {
        final float pivotX = x + part.pivotX;
        final float pivotY = y + part.pivotY;
        final float pivotZ = z + part.pivotZ;
        return Stream.concat(
            part.cuboids.stream().flatMap(cuboid -> asVoxels(pivotX, pivotY, pivotZ, cuboid)),
            part.children.values().stream().flatMap(p -> asVoxels(pivotX, pivotY, pivotZ, p))
        );
    }

    public static Stream<Voxel> asVoxels(float x, float y, float z, ModelPart.Cuboid cuboid) {
        int sizeX = (int)(cuboid.maxX - cuboid.minX);
        int sizeY = (int)(cuboid.maxY - cuboid.minY);
        int sizeZ = (int)(cuboid.maxZ - cuboid.minZ);
        Iterator<Voxel> iterator = new VoxelIterator(x, y, z, sizeX, sizeY, sizeZ);
        Spliterator<Voxel> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }
}