package me.kirantipov.mods.sync.block.entity;

import me.kirantipov.mods.sync.api.event.EntityFitnessEvents;
import me.kirantipov.mods.sync.block.TreadmillBlock;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class TreadmillBlockEntity extends BlockEntity implements DoubleBlockEntity, TickableBlockEntity, EnergyStorage, BlockEntityClientSerializable {
    private static final int MAX_RUNNING_TIME = 20 * 60 * 15; // ticks -> seconds -> minutes
    private static final double MAX_SQUARED_DISTANCE = 0.5;
    private static final Map<Class<? extends Entity>, Long> ENERGY_MAP;

    private UUID runnerUUID;
    private Integer runnerId;
    private Entity runner;
    private int runningTime;
    private long storedEnergy;
    private long producibleEnergyQuantity;
    private TreadmillBlockEntity cachedBackPart;


    public TreadmillBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.TREADMILL, pos, state);
    }


    private void setRunner(Entity entity) {
        if (this.runner == entity) {
            return;
        }

        if (this.runner != null) {
            EntityFitnessEvents.STOP_RUNNING.invoker().onStopRunning(this.runner, this);
        }

        if (entity == null) {
            this.runningTime = 0;
            this.producibleEnergyQuantity = 0;
        }
        this.runner = entity;

        if (this.runner != null) {
            EntityFitnessEvents.START_RUNNING.invoker().onStartRunning(this.runner, this);
        }

        if (this.world != null && !this.world.isClient) {
            this.markDirty();
            this.sync();
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, BlockState state) {
        if (this.runnerId != null) {
            this.setRunner(world.getEntityById(this.runnerId));
            this.runnerId = null;
        }

        if (this.runner == null) {
            return;
        }

        if (this.runner instanceof LivingEntity livingEntity) {
            livingEntity.limbDistance = 1.5F + 2F * this.runningTime / MAX_RUNNING_TIME;
        }
        this.runningTime = Math.min(++this.runningTime, MAX_RUNNING_TIME);
    }

    @Override
    public void onServerTick(World world, BlockPos pos, BlockState state) {
        if (this.runnerUUID != null && world instanceof ServerWorld serverWorld) {
            this.setRunner(serverWorld.getEntity(this.runnerUUID));
            this.runnerUUID = null;
        }

        if (this.runner == null) {
            return;
        }

        Direction face = state.get(TreadmillBlock.FACING);
        Vec3d anchor = computeTreadmillPivot(pos, face);
        if (!isValidEntity(this.runner) || !isEntityNear(this.runner, anchor)) {
            this.setRunner(null);
            return;
        }

        if (!this.runner.isPlayer()) {
            float yaw = face.asRotation();
            this.runner.updatePositionAndAngles(anchor.x, anchor.y, anchor.z, yaw, 0);
            this.runner.setHeadYaw(yaw);
            this.runner.setBodyYaw(yaw);
            this.runner.setYaw(yaw);
            this.runner.prevYaw = yaw;
        }

        if (this.runner instanceof LivingEntity livingEntity) {
            livingEntity.setDespawnCounter(0);
        }
        this.storedEnergy = this.producibleEnergyQuantity * (long)(1.0 + 0.5 * this.runningTime / MAX_RUNNING_TIME);
        this.transferEnergy(world, pos);
        if (this.runningTime < MAX_RUNNING_TIME) {
            ++this.runningTime;
            if (this.runningTime % 1000 == 0) {
                this.markDirty();
                this.sync();
            }
        }
    }

    public void onSteppedOn(BlockPos pos, BlockState state, Entity entity) {
        if (this.runner != null || !isEntityNear(entity, computeTreadmillPivot(pos, state.get(TreadmillBlock.FACING)))) {
            return;
        }

        Long energy = isValidEntity(entity) ? getOutputEnergyQuantityForEntity(entity, this) : null;
        if (energy != null) {
            this.setRunner(entity);
            this.producibleEnergyQuantity = energy;
        }
    }

    public boolean isOverheated() {
        return this.runner != null && this.runningTime >= MAX_RUNNING_TIME;
    }

    @Override
    public long getAmount() {
        TreadmillBlockEntity back = this.getBackPart();
        return back == null ? 0 : back.storedEnergy;
    }

    @Override
    public long getCapacity() {
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null || back.runner == null) {
            return 0;
        }
        return back.producibleEnergyQuantity * (long)(1.0 + 0.5 * back.runningTime / MAX_RUNNING_TIME);
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
    public long insert(long maxAmount, TransactionContext context) {
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext context) {
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null) {
            return 0;
        }

        long extracted = Math.min(back.storedEnergy, maxAmount);
        context.addCloseCallback((ctx, result) -> {
            if (result.wasCommitted()) {
                back.storedEnergy -= extracted;
            }
        });
        return extracted;
    }

    private void transferEnergy(World world, BlockPos pos) {
        TreadmillBlockEntity back = this.getBackPart();
        if (back == null) {
            return;
        }

        for (int i = 0; i < 2; ++i) {
            for (Direction direction : Direction.values()) {
                EnergyStorage target = EnergyStorage.SIDED.find(world, pos.offset(direction), direction);
                if (target != null && target.supportsInsertion()) {
                    EnergyStorageUtil.move(back, target, Long.MAX_VALUE, null);
                    if (back.storedEnergy <= 0) {
                        return;
                    }
                }
            }
            pos = pos.offset(this.getCachedState().get(TreadmillBlock.FACING));
        }
    }

    @Override
    public DoubleBlockProperties.Type getBlockType(BlockState state) {
        return TreadmillBlock.getTreadmillPart(state);
    }

    private TreadmillBlockEntity getBackPart() {
        if (this.cachedBackPart != null || this.world == null) {
            return this.cachedBackPart;
        }

        if (TreadmillBlock.isBack(this.getCachedState())) {
            this.cachedBackPart = this;
        } else {
            BlockPos backPartPos = this.pos.offset(this.getCachedState().get(TreadmillBlock.FACING).getOpposite());
            this.cachedBackPart = this.world.getBlockEntity(backPartPos, SyncBlockEntities.TREADMILL).orElse(null);
        }
        return this.cachedBackPart;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.runnerUUID = nbt.containsUuid("runner") ? nbt.getUuid("runner") : null;
        this.producibleEnergyQuantity = nbt.getLong("energy");
        this.runningTime = nbt.getInt("time");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        UUID runnerId = this.runnerUUID == null ? this.runner == null ? null : this.runner.getUuid() : this.runnerUUID;
        if (runnerId != null) {
            nbt.putUuid("runner", runnerId);
        }
        nbt.putLong("energy", this.producibleEnergyQuantity);
        nbt.putInt("time", this.runningTime);
        return nbt;
    }

    @Override
    public void fromClientTag(NbtCompound nbt) {
        this.runnerId = nbt.contains("runner") ? nbt.getInt("runner") : -1;
        this.runningTime = nbt.getInt("time");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbt) {
        Integer runnerId = this.runnerId == null ? this.runner == null ? null : this.runner.getId() : this.runnerId;
        if (runnerId != null) {
            nbt.putInt("runner", runnerId);
        }
        nbt.putInt("time", this.runningTime);
        return nbt;
    }

    private static Long getOutputEnergyQuantityForEntity(Entity entity, EnergyStorage energyStorage) {
        return EntityFitnessEvents.MODIFY_OUTPUT_ENERGY_QUANTITY.invoker().modifyOutputEnergyQuantity(entity, energyStorage, ENERGY_MAP.get(entity.getClass()));
    }

    private static boolean isValidEntity(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }

        return (
            !entity.isSpectator() && !entity.isSneaking() && !entity.isSwimming() &&
            (!(entity instanceof LivingEntity livingEntity) || livingEntity.hurtTime <= 0 && !livingEntity.isBaby()) &&
            (!(entity instanceof MobEntity mobEntity) || !mobEntity.isLeashed()) &&
            (!(entity instanceof TameableEntity tameableEntity) || !tameableEntity.isSitting())
        );
    }

    private static boolean isEntityNear(Entity entity, Vec3d pos) {
        return entity.squaredDistanceTo(pos) < MAX_SQUARED_DISTANCE;
    }

    private static Vec3d computeTreadmillPivot(BlockPos pos, Direction face) {
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
        ENERGY_MAP = Map.of(
            ChickenEntity.class, 2L,
            PigEntity.class, 16L,
            ServerPlayerEntity.class, 20L,
            WolfEntity.class, 24L,
            CreeperEntity.class, 80L,
            EndermanEntity.class, 160L
        );
    }
}