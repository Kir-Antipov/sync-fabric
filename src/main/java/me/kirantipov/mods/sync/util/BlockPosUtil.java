package me.kirantipov.mods.sync.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.EntityView;

import java.util.Optional;

public final class BlockPosUtil {
    public static Optional<Direction> getHorizontalFacing(BlockPos pos, BlockView blockView) {
        BlockState state = blockView.getBlockState(pos);
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return Optional.of(state.get(Properties.HORIZONTAL_FACING));
        }
        return Optional.empty();
    }

    public static boolean hasPlayerInside(BlockPos pos, EntityView world) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        return world.getClosestPlayer(x, y, z, 1, false) != null;
    }

    public static boolean isEntityInside(Entity entity, BlockPos pos) {
        double dX = Math.abs((pos.getX() + 0.5) - entity.getX());
        double dZ = Math.abs((pos.getZ() + 0.5) - entity.getZ());
        final double MAX_DELTA = 0.01;
        return dX < MAX_DELTA && dZ < MAX_DELTA;
    }

    public static void moveEntity(Entity entity, BlockPos target, Direction facing, boolean inside) {
        Direction targetDirection = facing.getOpposite();
        Vec3d currentPos = entity.getPos();
        double targetX = target.getX() + 0.5;
        double targetZ = target.getZ() + 0.5;
        if (!inside) {
            targetX += targetDirection.getOffsetX();
            targetZ += targetDirection.getOffsetZ();
        }
        double currentX = currentPos.x;
        double currentZ = currentPos.z;
        final double MAX_SPEED = 0.33;
        double velocityX = getMinVelocity(targetX - currentX, MAX_SPEED);
        double velocityZ = getMinVelocity(targetZ - currentZ, MAX_SPEED);
        float yaw = targetDirection.asRotation();

        entity.setVelocity(velocityX, 0, velocityZ);
        entity.setPitch(0);
        entity.setYaw(yaw);
        entity.setHeadYaw(yaw);
        entity.setBodyYaw(yaw);
        entity.prevYaw = yaw;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.prevBodyYaw = yaw;
            livingEntity.prevHeadYaw = yaw;
        }
    }

    private static double getMinVelocity(double velocity, double absLimit) {
        return Math.abs(velocity) < absLimit ? velocity : absLimit * Math.signum(velocity);
    }
}