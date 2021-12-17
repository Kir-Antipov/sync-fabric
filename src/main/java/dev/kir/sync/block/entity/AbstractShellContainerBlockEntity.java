package dev.kir.sync.block.entity;

import dev.kir.sync.api.networking.ShellDestroyedPacket;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.api.shell.ShellStateContainer;
import dev.kir.sync.api.shell.ShellStateManager;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.item.SimpleInventory;
import dev.kir.sync.util.ItemUtil;
import dev.kir.sync.util.nbt.NbtSerializer;
import dev.kir.sync.util.nbt.NbtSerializerFactory;
import dev.kir.sync.util.nbt.NbtSerializerFactoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractShellContainerBlockEntity extends BlockEntity implements ShellStateContainer, DoubleBlockEntity, TickableBlockEntity, BlockEntityClientSerializable, Inventory {
    private static final NbtSerializerFactory<AbstractShellContainerBlockEntity> NBT_SERIALIZER_FACTORY;

    protected final BooleanAnimator doorAnimator;
    protected ShellState shell;
    protected DyeColor color;
    protected int progressComparatorOutput;
    protected int inventoryComparatorOutput;
    private AbstractShellContainerBlockEntity bottomPart;

    private ShellState syncedShell;
    private BlockPos syncedShellPos;
    private DyeColor syncedShellColor;
    private float syncedShellProgress;
    private DyeColor syncedColor;
    private boolean inventoryDirty;
    private boolean visibleInventoryDirty;

    private final NbtSerializer<AbstractShellContainerBlockEntity> serializer;


    public AbstractShellContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.doorAnimator = new BooleanAnimator(AbstractShellContainerBlock.isOpen(state));
        this.serializer = NBT_SERIALIZER_FACTORY.create(this);
    }


    @Override
    public void setShellState(ShellState shell) {
        this.shell = shell;
    }

    @Override
    public ShellState getShellState() {
        return this.shell;
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public int getProgressComparatorOutput() {
        return this.getBottomPart().map(x -> x.progressComparatorOutput).orElse(0);
    }

    public int getInventoryComparatorOutput() {
        return this.getBottomPart().map(x -> x.inventoryComparatorOutput).orElse(0);
    }

    protected ShellStateManager getShellStateManager() {
        return (ShellStateManager)Objects.requireNonNull(this.world).getServer();
    }

    protected Optional<AbstractShellContainerBlockEntity> getBottomPart() {
        if (this.bottomPart == null && this.world != null) {
            this.bottomPart = AbstractShellContainerBlock.isBottom(this.getCachedState()) ? this : (this.world.getBlockEntity(this.pos.offset(Direction.DOWN)) instanceof AbstractShellContainerBlockEntity x ? x : null);
        }
        return Optional.ofNullable(this.bottomPart);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        if (this.shell != null && this.shell.getColor() != this.color) {
            this.shell.setColor(this.color);
        }

        if (this.requiresSync()) {
            this.updateShell(this.shell != this.syncedShell, !this.visibleInventoryDirty);
            this.updateComparatorOutput(world, pos, state);

            this.syncedShellPos = this.shell == null ? null : this.shell.getPos();
            this.syncedShellColor = this.shell == null ? null : this.shell.getColor();
            this.syncedShellProgress = this.shell == null ? -1 : this.shell.getProgress();
            this.syncedShell = this.shell;
            this.syncedColor = this.color;
            this.inventoryDirty = false;
            this.visibleInventoryDirty = false;

            this.sync();
            world.markDirty(pos);
        }

        if (this.inventoryDirty) {
            this.updateComparatorOutput(world, pos, state);
            this.inventoryDirty = false;
            world.markDirty(pos);
        }
    }

    private boolean requiresSync() {
        return (
            this.visibleInventoryDirty ||
            this.syncedShell != this.shell ||
            this.syncedColor != this.color ||
            this.shell != null && (
                !this.shell.getPos().equals(this.syncedShellPos) ||
                !Objects.equals(this.shell.getColor(), this.syncedShellColor) ||
                this.shell.getProgress() != this.syncedShellProgress
            )
        );
    }

    private void updateShell(boolean isNew, boolean partialUpdate) {
        ShellStateManager shellManager = this.getShellStateManager();
        if (isNew) {
            shellManager.remove(this.syncedShell);
            shellManager.add(this.shell);
        } else if (partialUpdate) {
            shellManager.update(this.shell);
        } else {
            shellManager.add(this.shell);
        }
    }

    private void updateComparatorOutput(World world, BlockPos pos, BlockState state) {
        int currentProgressOutput = this.shell == null ? 0 : MathHelper.clamp((int)(this.shell.getProgress() * 15), 1, 15);
        int currentInventoryOutput = this.shell == null ? 0 : ScreenHandler.calculateComparatorOutput(this.shell.getInventory());
        BlockPos topPartPos = pos.offset(AbstractShellContainerBlock.getDirectionTowardsAnotherPart(state));
        BlockState topPartState = world.getBlockState(topPartPos);
        if (this.progressComparatorOutput != currentProgressOutput) {
            this.progressComparatorOutput = currentProgressOutput;
            if (state.get(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.PROGRESS) {
                world.updateComparators(pos, state.getBlock());
            }
            if (topPartState.contains(AbstractShellContainerBlock.OUTPUT) && topPartState.get(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.PROGRESS) {
                world.updateComparators(topPartPos, topPartState.getBlock());
            }
        }
        if (this.inventoryComparatorOutput != currentInventoryOutput) {
            this.inventoryComparatorOutput = currentInventoryOutput;
            if (state.get(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.INVENTORY) {
                world.updateComparators(pos, state.getBlock());
            }
            if (topPartState.contains(AbstractShellContainerBlock.OUTPUT) && topPartState.get(AbstractShellContainerBlock.OUTPUT) == AbstractShellContainerBlock.ComparatorOutputType.INVENTORY) {
                world.updateComparators(topPartPos, topPartState.getBlock());
            }
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, BlockState state) {
        this.doorAnimator.setValue(AbstractShellContainerBlock.isOpen(state));
        this.doorAnimator.step();
    }

    public void onBreak(World world, BlockPos pos) {
        if (this.shell != null && world instanceof ServerWorld serverWorld) {
            this.getShellStateManager().remove(this.shell);
            this.destroyShell(serverWorld, pos);
        }
    }

    protected void destroyShell(ServerWorld world, BlockPos pos) {
        if (this.shell != null) {
            this.shell.drop(world, pos);
            new ShellDestroyedPacket(pos).send(PlayerLookup.around(world, pos, 32));
            this.shell = null;
        }
    }

    public abstract ActionResult onUse(World world, BlockPos pos, PlayerEntity player, Hand hand);

    @Environment(EnvType.CLIENT)
    public float getDoorOpenProgress(float tickDelta) {
        return this.getBottomPart().map(x -> x.doorAnimator.getProgress(tickDelta)).orElse(0f);
    }

    @Override
    public DoubleBlockProperties.Type getBlockType(BlockState state) {
        return AbstractShellContainerBlock.getShellContainerHalf(state);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.serializer.readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return this.serializer.writeNbt(tag);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return this.serializer.writeNbt(super.writeNbt(nbt));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.serializer.readNbt(nbt);
    }

    private static int reorderSlotIndex(int slot, SimpleInventory inventory) {
        final int mainSize = inventory.main.size();
        final int armorSize = inventory.armor.size();
        final int offHandSize = inventory.offHand.size();
        return (
            slot >= 0 && slot < armorSize
                ? (slot + mainSize)
                : slot >= armorSize && slot < (armorSize + offHandSize)
                    ? (slot + mainSize)
                    : (slot - armorSize - offHandSize)
        );
    }

    private static boolean isVisibleSlot(int slot, SimpleInventory inventory) {
        final int armorSize = inventory.armor.size();
        final int offHandSize = inventory.offHand.size();
        return slot >= 0 && slot <= (armorSize + offHandSize);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return false;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        final int armorSize = inventory.armor.size();
        boolean isArmorSlot = slot >= 0 && slot < armorSize;
        if (isArmorSlot) {
            EquipmentSlot equipmentSlot = ItemUtil.getPreferredEquipmentSlot(stack);
            return ItemUtil.isArmor(stack) && equipmentSlot.getType() == EquipmentSlot.Type.ARMOR && slot == equipmentSlot.getEntitySlotId();
        }

        boolean isOffHandSlot = slot >= armorSize && slot < (armorSize + inventory.offHand.size());
        if (isOffHandSlot) {
            return ItemUtil.getPreferredEquipmentSlot(stack) == EquipmentSlot.OFFHAND || inventory.main.stream().noneMatch(x -> x.isEmpty() || (x.getCount() + stack.getCount()) <= x.getMaxCount() && ItemStack.canCombine(x, stack));
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.getBottomPart().filter(x -> x.shell != null).map(x -> x.shell.getInventory().getStack(reorderSlotIndex(slot, x.shell.getInventory()))).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null || bottom.shell.getProgress() < ShellState.PROGRESS_DONE) {
            return;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        inventory.setStack(reorderSlotIndex(slot, inventory), stack);
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= isVisibleSlot(slot, inventory);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return ItemStack.EMPTY;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        ItemStack removed = inventory.removeStack(reorderSlotIndex(slot, inventory), amount);
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= !removed.isEmpty() && isVisibleSlot(slot, inventory);
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return ItemStack.EMPTY;
        }

        SimpleInventory inventory = bottom.shell.getInventory();
        ItemStack removed = inventory.removeStack(reorderSlotIndex(slot, inventory));
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty |= !removed.isEmpty() && isVisibleSlot(slot, inventory);
        return removed;
    }

    @Override
    public void clear() {
        AbstractShellContainerBlockEntity bottom = this.getBottomPart().orElse(null);
        if (bottom == null || bottom.shell == null) {
            return;
        }

        bottom.shell.getInventory().clear();
        bottom.inventoryDirty = true;
        bottom.visibleInventoryDirty = true;
    }

    @Override
    public int size() {
        return this.getBottomPart().map(x -> x.shell == null || x.shell.getProgress() < ShellState.PROGRESS_DONE ? 0 : x.shell.getInventory().size()).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return this.getBottomPart().map(x -> x.shell == null || x.shell.getInventory().isEmpty()).orElse(true);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<AbstractShellContainerBlockEntity>()
            .add(NbtCompound.class,"shell", x -> x.shell == null ? null : x.shell.writeNbt(new NbtCompound()), (x, shell) -> x.shell = shell == null ? null : ShellState.fromNbt(shell))
            .add(Integer.class,"color", x -> x.color == null ? null : x.color.getId(), (x, color) -> x.color = color == null ? null : DyeColor.byId(color))
            .build();
    }
}
