package me.kirantipov.mods.sync.client.model;

import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.client.render.CustomRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class ShellModel<T extends AnimalModel<?>> extends Model {
    public final T parentModel;
    private float buildProgress;
    private final VoxelModel voxelModel;

    public ShellModel(T parentModel) {
        this(parentModel, new Random());
    }

    public ShellModel(T parentModel, Random random) {
        super(null);
        this.parentModel = parentModel;
        this.buildProgress = 0;
        this.voxelModel = VoxelModel.fromModel(parentModel, random);
    }

    public void setBuildProgress(float buildProgress) {
        this.buildProgress = buildProgress;
        this.voxelModel.completeness = buildProgress / ShellState.PROGRESS_PRINTING;
    }

    public void setDestructionProgress(float destructionProgress) {
        this.voxelModel.destructionProgress = destructionProgress;
    }

    @Override
    public RenderLayer getLayer(Identifier textureId) {
        if (this.isBeingPrinted() || this.isBeingDestroyed()) {
            return this.voxelModel.getLayer(textureId);
        }

        if (this.isBeingPainted()) {
            float paintingProgress = (this.buildProgress - ShellState.PROGRESS_PRINTING) / ShellState.PROGRESS_PAINTING;
            float cutoutY = this.voxelModel.pivotY + this.voxelModel.sizeY * (1F - paintingProgress);
            return CustomRenderLayer.getEntityTranslucentPartiallyTextured(textureId, cutoutY);
        }

        return RenderLayer.getEntityTranslucent(textureId);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        Model target = (this.isBeingPrinted() || this.isBeingDestroyed()) ? this.voxelModel : this.parentModel;
        target.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    private boolean isBeingPrinted() {
        return this.buildProgress >= ShellState.PROGRESS_START && this.buildProgress < ShellState.PROGRESS_PRINTING;
    }

    private boolean isBeingPainted() {
        return this.buildProgress >= ShellState.PROGRESS_PRINTING && this.buildProgress < ShellState.PROGRESS_DONE;
    }

    private boolean isBeingDestroyed() {
        return this.voxelModel.destructionProgress > 0F;
    }
}
