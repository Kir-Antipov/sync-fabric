package dev.kir.sync.block.entity;

import dev.kir.sync.Sync;
import dev.kir.sync.api.event.PlayerSyncEvents;
import dev.kir.sync.api.shell.ShellStateContainer;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.block.ShellStorageBlock;
import dev.kir.sync.client.gui.ShellSelectorGUI;
import dev.kir.sync.config.SyncConfig;
import dev.kir.sync.util.BlockPosUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings({"UnstableApiUsage"})
public class ShellStorageBlockEntity extends AbstractShellContainerBlockEntity implements EnergyStorage {
    private EntityState entityState;
    private int ticksWithoutPower;
    private long storedEnergy;
    private final BooleanAnimator connectorAnimator;

    public ShellStorageBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.SHELL_STORAGE, pos, state);
        this.entityState = EntityState.NONE;
        this.connectorAnimator = new BooleanAnimator(false);
    }

    public DyeColor getIndicatorColor() {
        if (this.world != null && ShellStorageBlock.isPowered(this.getCachedState())) {
            return this.color == null ? DyeColor.LIME : this.color;
        }

        return DyeColor.RED;
    }

    @Environment(EnvType.CLIENT)
    public float getConnectorProgress(float tickDelta) {
        return this.getBottomPart().map(x -> ((ShellStorageBlockEntity)x).connectorAnimator.getProgress(tickDelta)).orElse(0f);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);

        SyncConfig config = Sync.getConfig();
        boolean isReceivingRedstonePower = config.shellStorageAcceptsRedstone && ShellStorageBlock.isEnabled(state);
        boolean hasEnergy = this.storedEnergy > 0;
        boolean isPowered = isReceivingRedstonePower || hasEnergy;
        boolean shouldBeOpen = isPowered && this.getBottomPart().map(x -> x.shell == null).orElse(true);

        ShellStorageBlock.setPowered(state, world, pos, isPowered);
        ShellStorageBlock.setOpen(state, world, pos, shouldBeOpen);

        if (this.shell != null && !isPowered) {
            ++this.ticksWithoutPower;
            if (this.ticksWithoutPower >= config.shellStorageMaxUnpoweredLifespan) {
                this.destroyShell((ServerWorld)world, pos);
            }
        } else {
            this.ticksWithoutPower = 0;
        }

        if (!isReceivingRedstonePower && hasEnergy) {
            this.storedEnergy = MathHelper.clamp(this.storedEnergy - config.shellStorageConsumption, 0, config.shellStorageCapacity);
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, BlockState state) {
        super.onClientTick(world, pos, state);
        this.connectorAnimator.setValue(this.shell != null);
        this.connectorAnimator.step();
        if (this.entityState == EntityState.LEAVING || this.entityState == EntityState.CHILLING) {
            this.entityState = BlockPosUtil.hasPlayerInside(pos, world) ? this.entityState : EntityState.NONE;
        }
    }

    @Environment(EnvType.CLIENT)
    public void onEntityCollisionClient(Entity entity, BlockState state) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        if (this.entityState == EntityState.NONE) {
            boolean isInside = BlockPosUtil.isEntityInside(entity, this.pos);
            PlayerSyncEvents.ShellSelectionFailureReason failureReason = !isInside && client.player == entity ? PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, this) : null;
            this.entityState = isInside || failureReason != null ? EntityState.CHILLING : EntityState.ENTERING;
            if (failureReason != null) {
                player.sendMessage(failureReason.toText(), true);
            }
        } else if (this.entityState != EntityState.CHILLING && client.currentScreen == null) {
            BlockPosUtil.moveEntity(entity, this.pos, state.get(ShellStorageBlock.FACING), this.entityState == EntityState.ENTERING);
        }

        if (this.entityState == EntityState.ENTERING && client.player == entity && client.currentScreen == null && BlockPosUtil.isEntityInside(entity, this.pos)) {
            client.setScreen(new ShellSelectorGUI(() -> this.entityState = EntityState.LEAVING, () -> this.entityState = EntityState.CHILLING));
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        if (stack.getCount() > 0 && item instanceof DyeItem dye) {
            stack.decrement(1);
            this.color = dye.getColor();
        }
        return ActionResult.SUCCESS;
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
        ShellStorageBlockEntity bottom = (ShellStorageBlockEntity)this.getBottomPart().orElse(null);
        if (bottom == null) {
            return 0;
        }

        long capacity = bottom.getCapacity();
        long maxEnergy = MathHelper.clamp(capacity - bottom.storedEnergy, 0, capacity);
        long inserted = MathHelper.clamp(amount, 0, maxEnergy);
        context.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
                bottom.storedEnergy += inserted;
            }
        });
        return inserted;
    }

    @Override
    public long extract(long amount, TransactionContext context) {
        return 0;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Sync.getConfig().shellStorageCapacity;
    }

    private enum EntityState {
        NONE,
        ENTERING,
        CHILLING,
        LEAVING
    }

    static {
        ShellStateContainer.LOOKUP.registerForBlockEntity((x, s) -> x.hasWorld() && AbstractShellContainerBlock.isBottom(x.getCachedState()) && (s == null || s.equals(x.getShellState())) ? x : null, SyncBlockEntities.SHELL_STORAGE);
        EnergyStorage.SIDED.registerForBlockEntities((x, __) -> (EnergyStorage)x, SyncBlockEntities.SHELL_STORAGE);
    }
}