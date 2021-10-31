package dev.kir.sync.mixin;

import dev.kir.sync.client.model.VoxelProvider;
import dev.kir.sync.util.client.render.ModelUtil;
import dev.kir.sync.util.math.Voxel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin implements VoxelProvider {
    @Final
    @Shadow
    private boolean thinArms;

    @Override
    @SuppressWarnings("ConstantConditions")
    public Stream<Voxel> getVoxels() {
        PlayerEntityModel<?> model = (PlayerEntityModel<?>)(Object)this;

        float cX = -2;
        float cY = 0;
        float cZ = -2;

        ModelPart head = ModelUtil.copy(model.head);
        head.setPivot(cX - 2, cY - 8, cZ - 2);

        ModelPart body = ModelUtil.copy(model.body);
        body.setPivot(cX - 2, cY, cZ);

        ModelPart leftArm = ModelUtil.copy(model.leftArm);
        ModelPart rightArm = ModelUtil.copy(model.rightArm);
        if (this.thinArms) {
            leftArm.setPivot(cX + 6, cY + 0.5F, cZ);
            rightArm.setPivot(cX - 5, cY + 0.5F, cZ);
        } else {
            leftArm.setPivot(cX + 6, cY, cZ);
            rightArm.setPivot(cX - 6, cY, cZ);
        }

        ModelPart leftLeg = ModelUtil.copy(model.leftLeg);
        leftLeg.setPivot(cX + 1.9F, cY + 12, cZ);

        ModelPart rightLeg = ModelUtil.copy(model.rightLeg);
        rightLeg.setPivot(cX - 1.9F, cY + 12, cZ);

        return Stream.of(leftLeg, rightLeg, leftArm, rightArm, body, head).flatMap(ModelUtil::asVoxels);
    }
}
