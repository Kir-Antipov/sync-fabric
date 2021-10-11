package me.kirantipov.mods.sync.block.entity;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import me.kirantipov.mods.sync.api.core.ShellState;
import me.kirantipov.mods.sync.api.core.ShellStateContainer;
import me.kirantipov.mods.sync.api.event.PlayerSyncEvents;
import me.kirantipov.mods.sync.block.AbstractShellContainerBlock;
import me.kirantipov.mods.sync.block.ShellConstructorBlock;
import me.kirantipov.mods.sync.entity.damage.FingerstickDamageSource;
import me.kirantipov.mods.sync.util.BlockPosUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShellConstructorBlockEntity extends AbstractShellContainerBlockEntity implements EnergyIo {
    private static final float LF_AMOUNT = 256000;

    public ShellConstructorBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.SHELL_CONSTRUCTOR, pos, state);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        if (ShellConstructorBlock.isOpen(state)) {
            ShellConstructorBlock.setOpen(state, world, pos, BlockPosUtil.hasPlayerInside(pos, world));
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        PlayerSyncEvents.ShellConstructionFailureReason failureReason = this.beginShellConstruction(player);
        if (failureReason == null) {
            return ActionResult.SUCCESS;
        } else {
            player.sendMessage(failureReason.toText(), true);
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    private PlayerSyncEvents.ShellConstructionFailureReason beginShellConstruction(PlayerEntity player) {
        PlayerSyncEvents.ShellConstructionFailureReason failureReason = this.shell == null
                ? PlayerSyncEvents.ALLOW_SHELL_CONSTRUCTION.invoker().allowShellConstruction(player, this)
                : PlayerSyncEvents.ShellConstructionFailureReason.OCCUPIED;

        if (failureReason != null) {
            return failureReason;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            float damage = player.getMaxHealth();
            if (serverPlayer.server.isHardcore()) {
                damage *= 2;
            }
            player.damage(FingerstickDamageSource.getInstance(), damage);
            this.shell = ShellState.empty(serverPlayer, pos);
        }
        return null;
    }

    @Override
    public double getEnergy() {
        return 0;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public double insert(double amount, Simulation simulation) {
        if (!AbstractShellContainerBlock.isBottom(this.getCachedState())) {
            if (this.getSecondPart().orElse(null) instanceof EnergyIo energyIo) {
                return energyIo.insert(amount, simulation);
            }
            return amount;
        }

        if (this.shell == null || this.shell.getProgress() >= ShellState.PROGRESS_DONE) {
            return amount;
        }

        double maxEnergy = (ShellState.PROGRESS_DONE - this.shell.getProgress()) * LF_AMOUNT;
        if (simulation != Simulation.SIMULATE) {
            this.shell.setProgress(this.shell.getProgress() + (float)amount / LF_AMOUNT);
        }
        return MathHelper.clamp(amount - maxEnergy, 0, amount);
    }

    static {
        ShellStateContainer.LOOKUP.registerForBlockEntity((x, s) -> x.hasWorld() && AbstractShellContainerBlock.isBottom(x.getCachedState()) && (s == null || s.equals(x.getShellState())) ? x : null, SyncBlockEntities.SHELL_CONSTRUCTOR);
    }
}
