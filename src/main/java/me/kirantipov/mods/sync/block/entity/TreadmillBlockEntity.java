package me.kirantipov.mods.sync.block.entity;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement;
import me.kirantipov.mods.sync.block.TreadmillBlock;
import me.kirantipov.mods.sync.util.nbt.NbtSerializer;
import me.kirantipov.mods.sync.util.nbt.NbtSerializerFactory;
import me.kirantipov.mods.sync.util.nbt.NbtSerializerFactoryBuilder;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class TreadmillBlockEntity extends BlockEntity implements DoubleBlockEntity, TickableBlockEntity, EnergyIo, BlockEntityClientSerializable {
    private static final NbtSerializerFactory<TreadmillBlockEntity> NBT_SERIALIZER_FACTORY;

    private UUID runnerUUID;
    private Integer runnerId;
    private TreadmillBlockEntity cachedBackPart;
    private final TreadmillStateManager treadmillStateManager;
    private final NbtSerializer<TreadmillBlockEntity> serializer;

    public TreadmillBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.TREADMILL, pos, state);
        this.serializer = NBT_SERIALIZER_FACTORY.create(this);
        this.treadmillStateManager = new TreadmillStateManager();
    }

    public Entity getRunner() {
        if (this.runnerUUID != null && this.world instanceof ServerWorld serverWorld) {
            this.setRunner(serverWorld.getEntity(this.runnerUUID));
            this.runnerUUID = null;
        }

        if (this.runnerId != null && this.world != null) {
            this.setRunner(this.world.getEntityById(this.runnerId));
            this.runnerId = null;
        }

        return this.treadmillStateManager.getRunner();
    }

    private void setRunner(Entity runner) {
        if (this.treadmillStateManager.setRunner(runner) && this.world != null && !this.world.isClient) {
            this.sync();
            this.world.markDirty(this.pos);
        }
    }

    public boolean isOverheated() {
        return this.treadmillStateManager.isOverheated();
    }

    @Override
    public void onTick(World world, BlockPos pos, BlockState state) {
        Direction face = state.get(TreadmillBlock.FACING);
        Vec3d anchor = getRunnerAnchor(pos, face);
        Entity runner = this.getRunner();
        int runningTime = this.treadmillStateManager.getRunningTime();

        if (!world.isClient) {
            if (!this.treadmillStateManager.canBeRunnerAt(runner, anchor)) {
                this.setRunner(null);
                runner = null;
            }

            if (runner == null) {
                this.setRunner(this.treadmillStateManager.findRunner(world, anchor));
                runner = this.getRunner();
            }

            if (runner != null) {
                this.transferEnergy(world, pos);
            }
        }

        this.treadmillStateManager.tick(anchor, face);

        if (!world.isClient && runningTime != this.treadmillStateManager.getRunningTime()) {
            this.markDirty();
            if (this.treadmillStateManager.getRunningTime() % 1000 == 0) {
                this.sync();
            }
        }
    }

    @Override
    public double getEnergy() {
        TreadmillBlockEntity back = this.getBackPart();
        return back == null ? 0 : back.treadmillStateManager.getEnergy();
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        TreadmillBlockEntity back = this.getBackPart();
        return back == null ? 0 : back.treadmillStateManager.extract(maxAmount);
    }

    private void transferEnergy(World world, BlockPos pos) {
        for (int i = 0; i < 2; ++i) {
            for (Direction direction : Direction.values()) {
                EnergyIo target = EnergyApi.SIDED.find(world, pos.offset(direction), direction);
                if (target != null) {
                    EnergyMovement.move(this, target, Double.MAX_VALUE);
                }
            }
            pos = pos.offset(this.getCachedState().get(TreadmillBlock.FACING));
        }
    }

    private TreadmillBlockEntity getBackPart() {
        if (this.world == null) {
            return null;
        }

        if (TreadmillBlock.isBack(this.getCachedState())) {
            return this;
        }

        BlockPos backPartPos = this.pos.offset(this.getCachedState().get(TreadmillBlock.FACING).getOpposite());
        if (this.cachedBackPart == null || !this.cachedBackPart.pos.equals(backPartPos)) {
            this.cachedBackPart = this.world.getBlockEntity(backPartPos, SyncBlockEntities.TREADMILL).orElse(null);
        }
        return this.cachedBackPart;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.serializer.readNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return this.serializer.writeNbt(super.writeNbt(nbt));
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.runnerId = tag.contains("runnerId") ? tag.getInt("runnerId") : null;
        if (this.runnerId == null) {
            this.treadmillStateManager.setRunner(null);
            this.treadmillStateManager.setRunningTime(0);
        } else {
            this.treadmillStateManager.setRunningTime(tag.getInt("time"));
        }
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        if (this.getRunner() != null) {
            tag.putInt("runnerId", this.getRunner().getId());
            tag.putInt("time", this.treadmillStateManager.getRunningTime());
        }
        return tag;
    }

    @Override
    public DoubleBlockProperties.Type getBlockType(BlockState state) {
        return TreadmillBlock.getTreadmillPart(state);
    }

    private static Vec3d getRunnerAnchor(BlockPos pos, Direction face) {
        double x = switch (face) {
            case WEST -> pos.getX();
            case EAST -> pos.getX() + 1;
            default -> pos.getX() + 0.5D;
        };
        double y = pos.getY() + 0.175;
        double z = switch (face) {
            case SOUTH -> pos.getZ() + 1;
            case NORTH -> pos.getZ();
            default -> pos.getZ() + 0.5D;
        };
        return new Vec3d(x, y, z);
    }

    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<TreadmillBlockEntity>()
            .add(UUID.class, "runner", x -> x.getRunner() == null ? null : x.getRunner().getUuid(), (x, uuid) -> x.runnerUUID = uuid)
            .add(Integer.class, "time", x -> x.treadmillStateManager.getRunningTime(), (x, time) -> x.treadmillStateManager.setRunningTime(time))
            .build();
    }
}
