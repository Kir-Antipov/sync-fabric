package me.kirantipov.mods.sync.api.shell;

import me.kirantipov.mods.sync.entity.ShellEntity;
import me.kirantipov.mods.sync.item.SimpleInventory;
import me.kirantipov.mods.sync.util.WorldUtil;
import me.kirantipov.mods.sync.util.math.Radians;
import me.kirantipov.mods.sync.util.nbt.NbtSerializer;
import me.kirantipov.mods.sync.util.nbt.NbtSerializerFactory;
import me.kirantipov.mods.sync.util.nbt.NbtSerializerFactoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A state that can be applied to a shell.
 */
public class ShellState {
    public static final float PROGRESS_START = 0F;
    public static final float PROGRESS_DONE = 1F;
    public static final float PROGRESS_PRINTING = 0.75F;
    public static final float PROGRESS_PAINTING = PROGRESS_DONE - PROGRESS_PRINTING;

    private static final NbtSerializerFactory<ShellState> NBT_SERIALIZER_FACTORY;

    private UUID uuid;
    private float progress;
    private DyeColor color;
    private boolean isArtificial;

    private UUID ownerUuid;
    private String ownerName;
    private float health;
    private int gameMode;
    private SimpleInventory inventory;
    private ShellStateComponent component;

    private int foodLevel;
    private float saturationLevel;
    private float exhaustion;

    private int experienceLevel;
    private float experienceProgress;
    private int totalExperience;

    private Identifier world;
    private BlockPos pos;

    private final NbtSerializer<ShellState> serializer;

    // <========================== Java Is Shit ==========================> //
    public UUID getUuid() {
        return this.uuid;
    }

    public DyeColor getColor() {
        return this.color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = MathHelper.clamp(progress, 0F, 1F);
    }

    public boolean isArtificial() {
        return this.isArtificial;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public float getHealth() {
        return this.health;
    }

    public int getGameMode() {
        return this.gameMode;
    }

    public SimpleInventory getInventory() {
        return this.inventory;
    }

    public ShellStateComponent getComponent() {
        return this.component;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public float getExhaustion() {
        return this.exhaustion;
    }

    public int getExperienceLevel() {
        return this.experienceLevel;
    }

    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public Identifier getWorld() {
        return this.world;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
    // <========================== Java Is Shit ==========================> //

    private ShellState() {
        this.serializer = NBT_SERIALIZER_FACTORY.create(this);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayerEntity player, BlockPos pos) {
        return empty(player, pos, null);
    }

    /**
     * Creates empty shell of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Empty shell of the specified player.
     */
    public static ShellState empty(ServerPlayerEntity player, BlockPos pos, DyeColor color) {
        return create(player, pos, color, 0, true, false);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayerEntity player, BlockPos pos) {
        return of(player, pos, null);
    }

    /**
     * Creates shell that is a full copy of the specified player.
     *
     * @param player The player.
     * @param pos Position of the shell.
     * @param color Color of the shell.
     * @return Shell that is a full copy of the specified player.
     */
    public static ShellState of(ServerPlayerEntity player, BlockPos pos, DyeColor color) {
        return create(player, pos, color, 1, ((Shell)player).isArtificial(), true);
    }

    /**
     * Creates shell from the nbt data.
     * @param nbt The nbt data.
     * @return Shell created from the nbt data.
     */
    public static ShellState fromNbt(NbtCompound nbt) {
        ShellState state = new ShellState();
        state.readNbt(nbt);
        return state;
    }

    private static ShellState create(ServerPlayerEntity player, BlockPos pos, DyeColor color, float progress, boolean isArtificial, boolean copyPlayerState) {
        ShellState shell = new ShellState();

        shell.uuid = UUID.randomUUID();
        shell.progress = progress;
        shell.color = color;
        shell.isArtificial = isArtificial;

        shell.ownerUuid = player.getUuid();
        shell.ownerName = player.getName().asString();
        shell.gameMode = player.interactionManager.getGameMode().getId();
        shell.inventory = new SimpleInventory();
        shell.component = ShellStateComponent.empty();

        if (copyPlayerState) {
            shell.health = player.getHealth();
            shell.inventory.clone(player.getInventory());
            shell.component.clone(ShellStateComponent.of(player));

            shell.foodLevel = player.getHungerManager().getFoodLevel();
            shell.saturationLevel = player.getHungerManager().getSaturationLevel();
            shell.exhaustion = player.getHungerManager().getExhaustion();

            shell.experienceLevel = player.experienceLevel;
            shell.experienceProgress = player.experienceProgress;
            shell.totalExperience = player.totalExperience;
        } else {
            shell.health = player.getMaxHealth();
            shell.foodLevel = 20;
            shell.saturationLevel = 5;
        }

        shell.world = WorldUtil.getId(player.world);
        shell.pos = pos;

        return shell;
    }


    public void dropInventory(ServerWorld world) {
        this.dropInventory(world, this.pos);
    }

    public void dropInventory(ServerWorld world, BlockPos pos) {
        Stream
            .of(this.inventory.main, this.inventory.armor, this.inventory.offHand, this.component.getItems())
            .flatMap(Collection::stream)
            .forEach(x -> this.dropItemStack(world, pos, x));
    }

    public void dropXp(ServerWorld world) {
        this.dropXp(world, this.pos);
    }

    public void dropXp(ServerWorld world, BlockPos pos) {
        int xp = Math.min(this.experienceLevel * 7, 100) + this.component.getXp();
        Vec3d vecPos = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        ExperienceOrbEntity.spawn(world, vecPos, xp);
    }

    public void drop(ServerWorld world) {
        this.drop(world, this.pos);
    }

    public void drop(ServerWorld world, BlockPos pos) {
        this.dropInventory(world, pos);
        this.dropXp(world, pos);
    }

    private void dropItemStack(World world, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        item.setPickupDelay(40);
        item.setThrower(this.getOwnerUuid());

        float h = world.random.nextFloat() * 0.5F;
        float v = world.random.nextFloat() * 2 * Radians.R_PI;
        item.setVelocity(-MathHelper.sin(v) * h, 0.2, MathHelper.cos(v) * h);
        world.spawnEntity(item);
    }


    public NbtCompound writeNbt(NbtCompound nbt) {
        return this.serializer.writeNbt(nbt);
    }

    public void readNbt(NbtCompound nbt) {
        this.serializer.readNbt(nbt);
    }


    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ShellState state && Objects.equals(this.uuid, state.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }


    @Environment(EnvType.CLIENT)
    private ShellEntity entityInstance = null;

    @Environment(EnvType.CLIENT)
    public ShellEntity asEntity() {
        if (this.entityInstance == null) {
            this.entityInstance = new ShellEntity(this);
        }
        return this.entityInstance;
    }


    static {
        NBT_SERIALIZER_FACTORY = new NbtSerializerFactoryBuilder<ShellState>()
            .add(UUID.class, "uuid", x -> x.uuid, (x, uuid) -> x.uuid = uuid)
            .add(Integer.class, "color", x -> x.color == null ? -1 : x.color.getId(), (x, color) -> x.color = color == -1 ? null : DyeColor.byId(color))
            .add(Float.class, "progress", x -> x.progress, (x, progress) -> x.progress = progress)
            .add(Boolean.class, "isArtificial", x -> x.isArtificial, (x, isArtificial) -> x.isArtificial = isArtificial)

            .add(UUID.class, "ownerUuid", x -> x.ownerUuid, (x, ownerUuid) -> x.ownerUuid = ownerUuid)
            .add(String.class, "ownerName", x -> x.ownerName, (x, ownerName) -> x.ownerName = ownerName)
            .add(Float.class, "health", x -> x.health, (x, health) -> x.health = health)
            .add(Integer.class, "gameMode", x -> x.gameMode, (x, gameMode) -> x.gameMode = gameMode)
            .add(NbtList.class, "inventory", x -> x.inventory.writeNbt(new NbtList()), (x, inventory) -> { x.inventory = new SimpleInventory(); x.inventory.readNbt(inventory); })
            .add(NbtCompound.class, "components", x -> x.component.writeNbt(new NbtCompound()), (x, component) -> { x.component = ShellStateComponent.empty(); if (component != null) { x.component.readNbt(component); } })

            .add(Integer.class, "foodLevel", x -> x.foodLevel, (x, foodLevel) -> x.foodLevel = foodLevel)
            .add(Float.class, "saturationLevel", x -> x.saturationLevel, (x, saturationLevel) -> x.saturationLevel = saturationLevel)
            .add(Float.class, "exhaustion", x -> x.exhaustion, (x, exhaustion) -> x.exhaustion = exhaustion)

            .add(Integer.class, "experienceLevel", x -> x.experienceLevel, (x, experienceLevel) -> x.experienceLevel = experienceLevel)
            .add(Float.class, "experienceProgress", x -> x.experienceProgress, (x, experienceProgress) -> x.experienceProgress = experienceProgress)
            .add(Integer.class, "totalExperience", x -> x.totalExperience, (x, totalExperience) -> x.totalExperience = totalExperience)

            .add(Identifier.class, "world", x -> x.world, (x, world) -> x.world = world)
            .add(BlockPos.class, "pos", x -> x.pos, (x, pos) -> x.pos = pos)
            .build();
    }
}