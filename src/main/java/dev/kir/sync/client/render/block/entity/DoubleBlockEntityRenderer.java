package dev.kir.sync.client.render.block.entity;

import dev.kir.sync.block.entity.DoubleBlockEntity;
import dev.kir.sync.client.model.DoubleBlockModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public abstract class DoubleBlockEntityRenderer<T extends BlockEntity & DoubleBlockEntity> implements BlockEntityRenderer<T> {
    protected final BlockEntityRendererFactory.Context context;

    public DoubleBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState blockState = this.getBlockState(blockEntity);

        matrices.push();

        Direction face = this.getFacing(blockState);
        float rotation = face.asRotation();

        matrices.translate(0.5D, 0.75D, 0.5D);
        matrices.scale(-0.5F, -0.5F, 0.5F);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation));

        DoubleBlockModel model = this.getModel(blockEntity, blockState, tickDelta);
        VertexConsumer consumer = vertexConsumers.getBuffer(model.getLayer(this.getTextureId()));

        if (blockEntity.hasWorld()) {
            model.render(blockEntity.getBlockType(blockState), matrices, consumer, light, overlay);
        } else {
            model.render(matrices, consumer, light, overlay);
        }

        matrices.pop();
    }

    protected BlockState getBlockState(T blockEntity) {
        return blockEntity.hasWorld() ? blockEntity.getCachedState() : this.getDefaultState();
    }

    protected Direction getFacing(BlockState blockState) {
        return blockState.get(Properties.HORIZONTAL_FACING);
    }

    protected abstract DoubleBlockModel getModel(T blockEntity, BlockState blockState, float tickDelta);

    protected abstract BlockState getDefaultState();

    protected abstract Identifier getTextureId();
}
