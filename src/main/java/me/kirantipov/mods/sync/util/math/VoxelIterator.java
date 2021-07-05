package me.kirantipov.mods.sync.util.math;

import java.util.Iterator;

public class VoxelIterator implements Iterator<Voxel> {
    private final float pivotX;
    private final float pivotY;
    private final float pivotZ;

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private int x;
    private int y;
    private int z;

    public VoxelIterator(float pivotX, float pivotY, float pivotZ, int sizeX, int sizeY, int sizeZ) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public boolean hasNext() {
        return !(this.z == this.sizeZ && this.x == this.sizeX - 1 && this.y == this.sizeY - 1);
    }

    @Override
    public Voxel next() {
        if (!this.hasNext()) {
            return null;
        }

        if (this.z >= this.sizeZ) {
            ++this.x;
            this.z = 0;
        }

        if (this.x >= this.sizeX) {
            ++this.y;
            this.x = 0;
        }

        if (this.y >= this.sizeY) {
            return null;
        }

        return new Voxel(this.pivotX + this.x, this.pivotY + this.y, this.pivotZ + (this.z++));
    }
}
