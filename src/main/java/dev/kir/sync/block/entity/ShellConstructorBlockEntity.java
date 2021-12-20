package dev.kir.sync.block.entity;

import dev.kir.sync.util.BlockPosUtil;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.api.shell.ShellStateContainer;
import dev.kir.sync.api.event.PlayerSyncEvents;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.block.ShellConstructorBlock;
import dev.kir.sync.config.SyncConfig;
import dev.kir.sync.entity.damage.FingerstickDamageSource;
import dev.kir.sync.Sync;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings({"UnstableApiUsage"})
public class ShellConstructorBlockEntity extends AbstractShellContainerBlockEntity implements EnergyStorage {
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
            SyncConfig config = Sync.getConfig();

            float damage = serverPlayer.server.isHardcore() ? config.hardcoreFingerstickDamage : config.fingerstickDamage;

            boolean isCreative = !serverPlayer.interactionManager.getGameMode().isSurvivalLike();
            boolean isLowOnHealth = (player.getHealth() + player.getAbsorptionAmount()) <= damage;
            boolean hasTotemOfUndying = player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            if (isLowOnHealth && !isCreative && !hasTotemOfUndying && config.warnPlayerInsteadOfKilling) {
                return PlayerSyncEvents.ShellConstructionFailureReason.NOT_ENOUGH_HEALTH;
            }

            player.damage(FingerstickDamageSource.getInstance(), damage);
            this.shell = ShellState.empty(serverPlayer, pos);
            if (isCreative && config.enableInstantShellConstruction) {
                this.shell.setProgress(ShellState.PROGRESS_DONE);
            }
        }
        return null;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
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
    public long insert(long amount, TransactionContext context) {
        ShellConstructorBlockEntity bottom = (ShellConstructorBlockEntity)this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null || bottom.shell.getProgress() >= ShellState.PROGRESS_DONE) {
            return 0;
        }

        long requiredEnergyAmount = Sync.getConfig().shellConstructorCapacity;
        long maxEnergy = (long)((ShellState.PROGRESS_DONE - bottom.shell.getProgress()) * requiredEnergyAmount);
        context.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
                bottom.shell.setProgress(bottom.shell.getProgress() + (float)amount / requiredEnergyAmount);
            }
        });
        return MathHelper.clamp(amount, 0, maxEnergy);
    }

    @Override
    public long extract(long maxAmount, TransactionContext context) {
        return 0;
    }

    static {
        ShellStateContainer.LOOKUP.registerForBlockEntity((x, s) -> x.hasWorld() && AbstractShellContainerBlock.isBottom(x.getCachedState()) && (s == null || s.equals(x.getShellState())) ? x : null, SyncBlockEntities.SHELL_CONSTRUCTOR);
        EnergyStorage.SIDED.registerForBlockEntities((x, __) -> (EnergyStorage)x, SyncBlockEntities.SHELL_CONSTRUCTOR);
    }
}
