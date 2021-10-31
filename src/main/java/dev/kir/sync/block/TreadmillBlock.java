package dev.kir.sync.block;

import dev.kir.sync.block.entity.SyncBlockEntities;
import dev.kir.sync.block.entity.TickableBlockEntity;
import dev.kir.sync.block.entity.TreadmillBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("deprecation")
public class TreadmillBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final EnumProperty<Part> PART = EnumProperty.of("treadmill_part", Part.class);

    private static final VoxelShape NORTH_SHAPE_BACK;
    private static final VoxelShape NORTH_SHAPE_FRONT;
    private static final VoxelShape SOUTH_SHAPE_BACK;
    private static final VoxelShape SOUTH_SHAPE_FRONT;
    private static final VoxelShape EAST_SHAPE_BACK;
    private static final VoxelShape EAST_SHAPE_FRONT;
    private static final VoxelShape WEST_SHAPE_BACK;
    private static final VoxelShape WEST_SHAPE_FRONT;

    public TreadmillBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(PART, Part.BACK));
    }

    public static boolean isBack(BlockState state) {
        Part part = state.get(PART);
        return part == Part.BACK;
    }

    public static DoubleBlockProperties.Type getTreadmillPart(BlockState state) {
        Part part = state.get(PART);
        return part == Part.BACK ? DoubleBlockProperties.Type.FIRST : DoubleBlockProperties.Type.SECOND;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TreadmillBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Part part = state.get(PART);
        Direction facing = state.get(FACING);
        BlockEntity first = world.getBlockEntity(pos);
        BlockEntity second = world.getBlockEntity(pos.offset(getDirectionTowardsOtherPart(part, facing)));
        if (!(first instanceof TreadmillBlockEntity firstTreadmill) || !(second instanceof TreadmillBlockEntity secondTreadmill)) {
            return;
        }
        TreadmillBlockEntity front = part == Part.BACK ? secondTreadmill : firstTreadmill;
        TreadmillBlockEntity back = part == Part.BACK ? firstTreadmill : secondTreadmill;

        if (back.isOverheated()) {
            double x = front.getPos().getX() + random.nextDouble();
            double y = front.getPos().getY() + 0.4;
            double z = front.getPos().getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0.1, 0);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == getDirectionTowardsOtherPart(state.get(PART), state.get(FACING))) {
            return neighborState.isOf(this) && neighborState.get(PART) != state.get(PART) ? state : Blocks.AIR.getDefaultState();
        } else {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerFacing();
        BlockPos blockPos = ctx.getBlockPos();
        BlockPos blockPos2 = blockPos.offset(direction);
        return ctx.getWorld().getBlockState(blockPos2).canReplace(ctx) ? this.getDefaultState().with(FACING, direction) : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            BlockPos blockPos = pos.offset(state.get(FACING));
            world.setBlockState(blockPos, state.with(PART, Part.FRONT), 3);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, 3);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player.isCreative()) {
            Part part = state.get(PART);
            if (part == Part.FRONT) {
                BlockPos blockPos = pos.offset(getDirectionTowardsOtherPart(part, state.get(FACING)));
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.getBlock() == this && blockState.get(PART) == Part.BACK) {
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 35);
                    world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        boolean isBack = isBack(state);

        return switch (direction) {
            case NORTH -> isBack ? NORTH_SHAPE_BACK : NORTH_SHAPE_FRONT;
            case SOUTH -> isBack ? SOUTH_SHAPE_BACK : SOUTH_SHAPE_FRONT;
            case EAST -> isBack ? EAST_SHAPE_BACK : EAST_SHAPE_FRONT;
            case WEST -> isBack ? WEST_SHAPE_BACK : WEST_SHAPE_FRONT;
            default -> NORTH_SHAPE_FRONT;
        };
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof TreadmillBlockEntity treadmillBlockEntity) {
            treadmillBlockEntity.onSteppedOn(pos, state, entity);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != SyncBlockEntities.TREADMILL || !isBack(state)) {
            return null;
        }

        return world.isClient ? TickableBlockEntity::clientTicker : TickableBlockEntity::serverTicker;
    }

    public static Direction getDirectionTowardsOtherPart(Part part, Direction direction) {
        return part == Part.BACK ? direction : direction.getOpposite();
    }

    public enum Part implements StringIdentifiable {
        FRONT("front"),
        BACK("back");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    static {
        final VoxelShape TRACK_NORTH_SOUTH = Block.createCuboidShape(1.5, 0, 0, 14.5, 4, 16);
        final VoxelShape TRACK_EAST_WEST = Block.createCuboidShape(0, 0, 1.5, 16, 4, 14.5);

        final VoxelShape LEFT_SIDE_GUARD_NORTH_BACK = Block.createCuboidShape(1.5, 0, 0, 1.6, 8.3, 7.4);
        final VoxelShape RIGHT_SIDE_GUARD_NORTH_BACK = Block.createCuboidShape(14.4, 0, 0, 14.5, 8.3, 7.4);

        final VoxelShape LEFT_SIDE_GUARD_NORTH_FRONT = Block.createCuboidShape(1.5, 0, 7.5, 1.6, 8.7, 16);
        final VoxelShape RIGHT_SIDE_GUARD_NORTH_FRONT = Block.createCuboidShape(14.4, 0, 7.5, 14.5, 8.7, 16);
        final VoxelShape DASHBOARD_NORTH_FRONT = Block.createCuboidShape(1.5, 0, 2, 14.5, 8.5, 3);

        final VoxelShape LEFT_SIDE_GUARD_SOUTH_BACK = Block.createCuboidShape(1.5, 0, 8.6, 1.6, 8.3, 16);
        final VoxelShape RIGHT_SIDE_GUARD_SOUTH_BACK = Block.createCuboidShape(14.4, 0, 8.6, 14.5, 8.3, 16);

        final VoxelShape LEFT_SIDE_GUARD_SOUTH_FRONT = Block.createCuboidShape(1.5, 0, 0, 1.6, 8.7, 8.5);
        final VoxelShape RIGHT_SIDE_GUARD_SOUTH_FRONT = Block.createCuboidShape(14.4, 0, 0, 14.5, 8.7, 8.5);
        final VoxelShape DASHBOARD_SOUTH_FRONT = Block.createCuboidShape(1.5, 0, 13, 14.5, 8.5, 14);

        final VoxelShape LEFT_SIDE_GUARD_EAST_BACK = Block.createCuboidShape(8.6, 0, 1.5, 16, 8.3, 1.6);
        final VoxelShape RIGHT_SIDE_GUARD_EAST_BACK = Block.createCuboidShape(8.6, 0, 14.4, 16, 8.3, 14.5);

        final VoxelShape LEFT_SIDE_GUARD_EAST_FRONT = Block.createCuboidShape(0, 0, 1.5, 8.5, 8.7, 1.6);
        final VoxelShape RIGHT_SIDE_GUARD_EAST_FRONT = Block.createCuboidShape(0, 0, 14.4, 8.5, 8.7, 14.5);
        final VoxelShape DASHBOARD_EAST_FRONT = Block.createCuboidShape(13, 0, 1.5, 14, 8.5, 14.5);

        final VoxelShape LEFT_SIDE_GUARD_WEST_BACK = Block.createCuboidShape(0, 0, 1.5, 7.4, 8.3, 1.6);
        final VoxelShape RIGHT_SIDE_GUARD_WEST_BACK = Block.createCuboidShape(0, 0, 14.4, 7.4, 8.3, 14.5);

        final VoxelShape LEFT_SIDE_GUARD_WEST_FRONT = Block.createCuboidShape(7.5, 0, 1.5, 16, 8.7, 1.6);
        final VoxelShape RIGHT_SIDE_GUARD_WEST_FRONT = Block.createCuboidShape(7.5, 0, 14.4, 16, 8.7, 14.5);
        final VoxelShape DASHBOARD_WEST_FRONT = Block.createCuboidShape(2, 0, 1.5, 3, 8.5, 14.5);

        NORTH_SHAPE_BACK = VoxelShapes.union(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_NORTH_BACK, RIGHT_SIDE_GUARD_NORTH_BACK).simplify();
        NORTH_SHAPE_FRONT = VoxelShapes.union(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_NORTH_FRONT, RIGHT_SIDE_GUARD_NORTH_FRONT, DASHBOARD_NORTH_FRONT).simplify();

        SOUTH_SHAPE_BACK = VoxelShapes.union(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_SOUTH_BACK, RIGHT_SIDE_GUARD_SOUTH_BACK).simplify();
        SOUTH_SHAPE_FRONT = VoxelShapes.union(TRACK_NORTH_SOUTH, LEFT_SIDE_GUARD_SOUTH_FRONT, RIGHT_SIDE_GUARD_SOUTH_FRONT, DASHBOARD_SOUTH_FRONT).simplify();

        EAST_SHAPE_BACK = VoxelShapes.union(TRACK_EAST_WEST, LEFT_SIDE_GUARD_EAST_BACK, RIGHT_SIDE_GUARD_EAST_BACK).simplify();
        EAST_SHAPE_FRONT = VoxelShapes.union(TRACK_EAST_WEST, LEFT_SIDE_GUARD_EAST_FRONT, RIGHT_SIDE_GUARD_EAST_FRONT, DASHBOARD_EAST_FRONT).simplify();

        WEST_SHAPE_BACK = VoxelShapes.union(TRACK_EAST_WEST, LEFT_SIDE_GUARD_WEST_BACK, RIGHT_SIDE_GUARD_WEST_BACK).simplify();
        WEST_SHAPE_FRONT = VoxelShapes.union(TRACK_EAST_WEST, LEFT_SIDE_GUARD_WEST_FRONT, RIGHT_SIDE_GUARD_WEST_FRONT, DASHBOARD_WEST_FRONT).simplify();
    }
}
