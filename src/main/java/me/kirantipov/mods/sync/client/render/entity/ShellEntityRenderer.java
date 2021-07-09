package me.kirantipov.mods.sync.client.render.entity;

import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.client.model.ShellModel;
import me.kirantipov.mods.sync.entity.ShellEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

public class ShellEntityRenderer extends PlayerEntityRenderer {
    private final ShellModel<PlayerEntityModel<?>> shellModel;

    public ShellEntityRenderer(EntityRendererFactory.Context context, boolean slim) {
        super(context, slim);
        this.shellModel = new ShellModel<>(this.model);
        this.shadowRadius = 0;
        this.shadowOpacity = 0;
    }

    @Override
    public void render(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        if (player instanceof ShellEntity shell && !shell.isActive) {
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180F));
            matrices.scale(-1.0F, -1.0F, 1.0F);
            this.scale(player, matrices, yaw);
            matrices.translate(0.0D, -1.5010000467300415D, 0.0D);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw));

            this.applyStateToModel(this.shellModel, shell.getState());
            RenderLayer layer = this.shellModel.getLayer(player.getSkinTexture());
            VertexConsumer consumer = vertexConsumers.getBuffer(layer);
            this.shellModel.render(matrices, consumer, light, getOverlay(player, tickDelta), 1F, 1F, 1F, 1F);
        } else {
            Direction direction = Direction.fromRotation(yaw);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw));
            if (direction == Direction.WEST || direction == Direction.EAST) {
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180F));
            }

            float maxPitch = player.getEquippedStack(EquipmentSlot.CHEST).isEmpty() ? 15 : 5;
            float pitch = maxPitch * (player instanceof ShellEntity shell ? shell.pitchProgress : 0);
            player.setPitch(pitch);
            player.prevPitch = pitch;
            super.render(player, yaw, tickDelta, matrices, vertexConsumers, light);
        }
        matrices.pop();
    }

    @Override
    protected boolean hasLabel(AbstractClientPlayerEntity player) {
        return player.shouldRenderName() && super.hasLabel(player);
    }

    protected void applyStateToModel(ShellModel<PlayerEntityModel<?>> model, ShellState state) {
        AnimalModel<?> animalModel = model.parentModel;
        animalModel.getHeadParts().forEach(x -> x.setAngles(0, 0, 0));
        animalModel.getBodyParts().forEach(x -> x.setAngles(0, 0, 0));
        animalModel.child = false;
        model.parentModel.setVisible(true);
        model.setBuildProgress(state.getProgress());
    }
}