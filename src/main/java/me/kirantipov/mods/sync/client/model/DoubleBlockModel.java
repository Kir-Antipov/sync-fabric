package me.kirantipov.mods.sync.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public abstract class DoubleBlockModel extends Model {
    private final Map<DoubleBlockProperties.Type, List<ModelPart>> parts;
    protected final int textureWidth;
    protected final int textureHeight;

    public DoubleBlockModel(int textureWidth, int textureHeight) {
        this(RenderLayer::getEntityCutout, textureWidth, textureHeight);
    }

    public DoubleBlockModel(Function<Identifier, RenderLayer> layerFactory, int textureWidth, int textureHeight) {
        super(layerFactory);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.parts = new HashMap<>();
    }


    protected ModelPart createCuboid(int textureOffsetU, int textureOffsetV, float x, float y, float z, float xSize, float ySize, float zSize) {
        ModelPart.Cuboid cuboid = new ModelPart.Cuboid(textureOffsetU, textureOffsetV, 0, 0, 0, xSize, ySize, zSize, 0, 0, 0, true, this.textureWidth, this.textureHeight);
        ModelPart part = new ModelPart(List.of(cuboid), Map.of());
        part.setPivot(x, y, z);
        return part;
    }

    protected ModelPart createCuboid(int textureOffsetU, int textureOffsetV, float x, float y, float z, float xSize, float ySize, float zSize, ModelPart template) {
        ModelPart part = this.createCuboid(textureOffsetU, textureOffsetV, x, y, z, xSize, ySize, zSize);
        part.copyTransform(template);
        part.setPivot(x, y, z);
        return part;
    }

    protected ModelPart addCuboid(DoubleBlockProperties.Type type, int textureOffsetU, int textureOffsetV, float x, float y, float z, float xSize, float ySize, float zSize) {
        ModelPart part = this.createCuboid(textureOffsetU, textureOffsetV, x, y, z, xSize, ySize, zSize);
        if (!this.parts.containsKey(type)) {
            this.parts.put(type, new ArrayList<>());
        }
        this.parts.get(type).add(part);

        return part;
    }

    protected ModelPart addCuboid(DoubleBlockProperties.Type type, int textureOffsetU, int textureOffsetV, float x, float y, float z, float xSize, float ySize, float zSize, ModelPart template) {
        ModelPart part = this.addCuboid(type, textureOffsetU, textureOffsetV, x, y, z, xSize, ySize, zSize);
        part.copyTransform(template);
        part.setPivot(x, y, z);
        return part;
    }


    protected ModelPart createTemplate() {
        return new ModelPart(List.of(), Map.of());
    }

    protected ModelPart createRotationTemplate(float pitch, float yaw, float roll) {
        ModelPart template = this.createTemplate();
        template.pitch = pitch;
        template.yaw = yaw;
        template.roll = roll;
        return template;
    }

    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        this.render(matrices, vertices, light, overlay, 1F, 1F, 1F, 1F);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.push();
        this.render(DoubleBlockProperties.Type.FIRST, matrices, vertices, light, overlay, red, green, blue, alpha);
        this.translate(matrices);
        this.render(DoubleBlockProperties.Type.SECOND, matrices, vertices, light, overlay, red, green, blue, alpha);
        matrices.pop();
    }

    public void render(DoubleBlockProperties.Type type, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        this.render(type, matrices, vertices, light, overlay, 1F, 1F, 1F, 1F);
    }

    public void render(DoubleBlockProperties.Type type, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        List<ModelPart> currentParts = this.parts.get(type);
        if (currentParts == null) {
            return;
        }

        for (ModelPart part : currentParts) {
            part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }

    protected abstract void translate(MatrixStack matrices);
}
