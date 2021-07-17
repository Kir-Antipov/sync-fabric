package me.kirantipov.mods.sync.block.entity;

import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.energy.EnergyContainer;
import me.kirantipov.mods.sync.api.energy.EnergyContainerProvider;
import me.kirantipov.mods.sync.block.ShellConstructorBlock;
import me.kirantipov.mods.sync.entity.damage.FingerstickDamageSource;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ShellConstructorBlockEntity extends AbstractShellContainerBlockEntity {
    private static final float PJ_AMOUNT = 16000;

    public ShellConstructorBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.SHELL_CONSTRUCTOR, pos, state);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        if (ShellConstructorBlock.isOpen(state)) {
            ShellConstructorBlock.setOpen(state, world, pos, BlockPosUtil.hasPlayerInside(pos, world));
        }

        if (this.shell != null && this.shell.getProgress() < 1F) {
            float pj = extractEnergyFromNeighbors(world, pos) + extractEnergyFromNeighbors(world, pos.offset(ShellConstructorBlock.getDirectionTowardsAnotherPart(state)));
            this.shell.setProgress(this.shell.getProgress() + pj / PJ_AMOUNT);
        }
    }

    private static float extractEnergyFromNeighbors(World world, BlockPos pos) {
        float pj = 0;
        for (Direction direction : Direction.values()) {
            BlockEntity be = world.getBlockEntity(pos.offset(direction));
            EnergyContainer container = null;
            if (be instanceof EnergyContainerProvider provider) {
                container = provider.getEnergyContainer();
            } else if (be instanceof EnergyContainer) {
                container = ((EnergyContainer)be);
            }
            if (container != null) {
                pj += container.extract(container.getAmount());
            }
        }
        return pj;
    }

    @Override
    public void onUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (this.shell == null && player instanceof ServerPlayerEntity serverPlayer) {
            float damage = player.getMaxHealth();
            if (serverPlayer.server.isHardcore()) {
                damage *= 2;
            }
            player.damage(FingerstickDamageSource.getInstance(), damage);
            this.shell = ShellState.empty(serverPlayer, pos);
        }
    }
}
