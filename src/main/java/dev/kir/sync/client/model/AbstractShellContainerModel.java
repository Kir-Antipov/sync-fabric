package dev.kir.sync.client.model;

import dev.kir.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class AbstractShellContainerModel extends DoubleBlockModel {
    protected static final DoubleBlockProperties.Type BOTTOM = DoubleBlockProperties.Type.FIRST;
    protected static final DoubleBlockProperties.Type TOP = DoubleBlockProperties.Type.SECOND;

    public final ModelPart doorLL;
    public final ModelPart doorLU;
    public final ModelPart doorRL;
    public final ModelPart doorRU;

    public float doorOpenProgress;

    public AbstractShellContainerModel() {
        super(256, 256);
        ModelPart rightDoor = this.createRotationTemplate(0, Radians.R_PI, 0);

        this.doorLL = this.addCuboid(BOTTOM, 224, 32, -15, -8, -15, 15, 31, 1);
        this.doorLU = this.addCuboid(TOP, 224, 0, -15, -6, -15, 15, 30, 1);

        this.doorRL = this.addCuboid(BOTTOM, 224, 32, 15, -8, -14, 15, 31, 1, rightDoor);
        this.doorRU = this.addCuboid(TOP, 224, 0, 15, -6, -14, 15, 30, 1, rightDoor);
    }

    @Override
    public void render(DoubleBlockProperties.Type type, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.doorLL.yaw = this.doorLU.yaw = Radians.R_PI_2 * this.doorOpenProgress;
        this.doorRL.yaw = this.doorRU.yaw = Radians.R_PI - Radians.R_PI_2 * this.doorOpenProgress;

        this.doorLL.pivotZ = this.doorLU.pivotZ = -15 + 15 * this.doorOpenProgress;
        this.doorRL.pivotZ = this.doorRU.pivotZ = -14 + 14 * this.doorOpenProgress;
        this.doorRL.pivotX = this.doorRU.pivotX = 15 - this.doorOpenProgress;

        super.render(type, matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    protected void translate(MatrixStack matrices) {
        matrices.translate(0, -2F, 0);
    }
}