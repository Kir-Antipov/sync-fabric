package dev.kir.sync.block.entity;

import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.api.shell.ShellStateContainer;
import dev.kir.sync.api.shell.ShellStateManager;
import dev.kir.sync.api.networking.ShellDestroyedPacket;
import dev.kir.sync.block.AbstractShellContainerBlock;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractShellContainerBlockEntity extends BlockEntity implements ShellStateContainer, DoubleBlockEntity, TickableBlockEntity, BlockEntityClientSerializable {
    private static final NbtSerializerFactory<AbstractShellContainerBlockEntity> NBT_SERIALIZER_FACTORY;

    protected final BooleanAnimator doorAnimator;
    protected ShellState shell;
    protected DyeColor color;
    protected int comparatorOutput;
    private AbstractShellContainerBlockEntity secondPart;

    private ShellState syncedShell;
    private BlockPos syncedShellPos;
    private DyeColor syncedShellColor;
    private float syncedShellProgress;
    private DyeColor syncedColor;

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

    public int getComparatorOutput() {
        return Math.max(this.comparatorOutput, this.getSecondPart().map(x -> x.comparatorOutput).orElse(0));
    }

    protected ShellStateManager getShellStateManager() {
        return (ShellStateManager)Objects.requireNonNull(this.world).getServer();
    }

    protected Optional<AbstractShellContainerBlockEntity> getSecondPart() {
        return this.secondPart == null ? Optional.ofNullable(this.updateSecondPart(this.world, this.pos, this.world == null ? null : this.getCachedState())) : Optional.of(this.secondPart);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        this.updateSecondPart(world, pos, state);

        if (this.shell != null && this.shell.getColor() != this.color) {
            this.shell.setColor(this.color);
        }

        if (this.syncedShell != this.shell || this.syncedColor != this.color || this.shell != null && (!this.shell.getPos().equals(this.syncedShellPos) || !Objects.equals(this.shell.getColor(), this.syncedShellColor) || this.shell.getProgress() != this.syncedShellProgress)) {
            this.sync();
            world.markDirty(pos);

            ShellStateManager shellManager = this.getShellStateManager();
            if (this.syncedShell != this.shell) {
                shellManager.remove(this.syncedShell);
                shellManager.add(this.shell);
            } else {
                shellManager.update(this.shell);
            }

            int currentOutput = this.shell == null ? 0 : MathHelper.clamp((int)(this.shell.getProgress() * 15), 1, 15);
            if (this.comparatorOutput != currentOutput) {
                this.comparatorOutput = currentOutput;
                world.updateComparators(pos, state.getBlock());
                BlockPos anotherPartPos = pos.offset(AbstractShellContainerBlock.getDirectionTowardsAnotherPart(state));
                world.updateComparators(anotherPartPos, world.getBlockState(anotherPartPos).getBlock());
            }

            this.syncedShellPos = this.shell == null ? null : this.shell.getPos();
            this.syncedShellColor = this.shell == null ? null : this.shell.getColor();
            this.syncedShellProgress = this.shell == null ? -1 : this.shell.getProgress();
            this.syncedShell = this.shell;
            this.syncedColor = this.color;
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, BlockState state) {
        this.updateSecondPart(world, pos, state);
        this.doorAnimator.setValue(AbstractShellContainerBlock.isOpen(state));
        this.doorAnimator.step();
    }

    private AbstractShellContainerBlockEntity updateSecondPart(World world, BlockPos pos, BlockState state) {
        if (world == null) {
            return null;
        }

        BlockPos secondPartPos = pos.offset(AbstractShellContainerBlock.getDirectionTowardsAnotherPart(state));
        if (this.secondPart == null || !this.secondPart.pos.equals(secondPartPos)) {
            this.secondPart = world.getBlockEntity(secondPartPos) instanceof AbstractShellContainerBlockEntity shellContainer ? shellContainer : null;
        }
        return this.secondPart;
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
        float progress = this.doorAnimator.getProgress(tickDelta);
        float secondProgress = this.getSecondPart().map(second -> second.doorAnimator.getProgress(tickDelta)).orElse(Float.MAX_VALUE);
        return Math.min(progress, secondProgress);
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

    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<AbstractShellContainerBlockEntity>()
            .add(NbtCompound.class,"shell", x -> x.shell == null ? null : x.shell.writeNbt(new NbtCompound()), (x, shell) -> x.shell = shell == null ? null : ShellState.fromNbt(shell))
            .add(Integer.class,"color", x -> x.color == null ? null : x.color.getId(), (x, color) -> x.color = color == null ? null : DyeColor.byId(color))
            .build();
    }
}
