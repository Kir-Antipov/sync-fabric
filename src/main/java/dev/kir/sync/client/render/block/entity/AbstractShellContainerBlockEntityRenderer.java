package dev.kir.sync.client.render.block.entity;

import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.block.entity.AbstractShellContainerBlockEntity;
import dev.kir.sync.client.model.AbstractShellContainerModel;
import dev.kir.sync.client.model.DoubleBlockModel;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public abstract class AbstractShellContainerBlockEntityRenderer<T extends AbstractShellContainerBlockEntity> extends DoubleBlockEntityRenderer<T> {
    public AbstractShellContainerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.render(blockEntity, tickDelta, matrices, vertexConsumers, light, overlay);
        if (blockEntity.getShellState() != null) {
            this.renderShell(blockEntity.getShellState(), blockEntity, tickDelta, this.getBlockState(blockEntity), matrices, vertexConsumers, light);
        }
    }

    protected void renderShell(ShellState shellState, T blockEntity, float tickDelta, BlockState blockState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float yaw = this.getFacing(blockState).getOpposite().asRotation();
        ShellEntity shellEntity = this.createEntity(shellState, blockEntity, tickDelta);

        EntityRenderer<? super ShellEntity> renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(shellEntity);
        renderer.render(shellEntity, yaw, 0, matrices, vertexConsumers, light);
    }

    @Override
    protected DoubleBlockModel getModel(T blockEntity, BlockState blockState, float tickDelta) {
        AbstractShellContainerModel model = this.getShellContainerModel(blockEntity, blockState, tickDelta);
        model.doorOpenProgress = blockEntity.getDoorOpenProgress(tickDelta);
        return model;
    }

    protected abstract ShellEntity createEntity(ShellState shellState, T blockEntity, float tickDelta);

    protected abstract AbstractShellContainerModel getShellContainerModel(T blockEntity, BlockState blockState, float tickDelta);
}