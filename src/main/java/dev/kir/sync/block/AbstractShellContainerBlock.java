package dev.kir.sync.block;

import dev.kir.sync.block.entity.AbstractShellContainerBlockEntity;
import dev.kir.sync.block.entity.TickableBlockEntity;
import dev.kir.sync.util.ItemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public abstract class AbstractShellContainerBlock extends BlockWithEntity {
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final EnumProperty<ComparatorOutputType> OUTPUT = EnumProperty.of("output", ComparatorOutputType.class);

    private static final VoxelShape SOLID_SHAPE_TOP;
    private static final VoxelShape SOLID_SHAPE_BOTTOM;
    private static final VoxelShape NORTH_SHAPE_TOP;
    private static final VoxelShape NORTH_SHAPE_BOTTOM;
    private static final VoxelShape SOUTH_SHAPE_TOP;
    private static final VoxelShape SOUTH_SHAPE_BOTTOM;
    private static final VoxelShape EAST_SHAPE_TOP;
    private static final VoxelShape EAST_SHAPE_BOTTOM;
    private static final VoxelShape WEST_SHAPE_TOP;
    private static final VoxelShape WEST_SHAPE_BOTTOM;

    protected AbstractShellContainerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(
            this.getStateManager().getDefaultState()
                .with(OPEN, false)
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(FACING, Direction.NORTH)
                .with(OUTPUT, ComparatorOutputType.PROGRESS)
        );
    }


    public static void setOpen(BlockState state, World world, BlockPos pos, boolean open) {
        if (state.get(OPEN) != open) {
            world.setBlockState(pos, state.with(OPEN, open), 10);

            BlockPos secondPos = pos.offset(getDirectionTowardsAnotherPart(state));
            BlockState secondState = world.getBlockState(secondPos);
            if (secondState != null) {
                world.setBlockState(secondPos, secondState.with(OPEN, open), 10);
            }
        }
    }

    public static boolean isOpen(BlockState state) {
        return state.get(OPEN);
    }

    public static boolean isBottom(BlockState state) {
        DoubleBlockHalf half = state.get(HALF);
        return half == DoubleBlockHalf.LOWER;
    }

    public static DoubleBlockProperties.Type getShellContainerHalf(BlockState state) {
        DoubleBlockHalf part = state.get(HALF);
        return part == DoubleBlockHalf.LOWER ? DoubleBlockProperties.Type.FIRST : DoubleBlockProperties.Type.SECOND;
    }

    public static Direction getDirectionTowardsAnotherPart(BlockState state) {
        return isBottom(state) ? Direction.UP : Direction.DOWN;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf doubleBlockHalf = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (doubleBlockHalf == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighborState.isOf(this) && neighborState.get(HALF) != doubleBlockHalf ? state.with(FACING, neighborState.get(FACING)) : Blocks.AIR.getDefaultState();
        } else {
            return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        if (World.isValid(blockPos) && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(HALF, DoubleBlockHalf.LOWER);
        }

        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (!world.isClient && entity instanceof PlayerEntity && isBottom(state)) {
            setOpen(state, world, pos, true);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        boolean bottom = isBottom(state);
        BlockPos bottomPos = bottom ? pos : pos.down();
        if (world.getBlockEntity(bottomPos) instanceof AbstractShellContainerBlockEntity shellContainer) {
            shellContainer.onBreak(world, bottomPos);
        }
        if (!world.isClient && player.isCreative()) {
            if (!bottom) {
                BlockState blockState = world.getBlockState(bottomPos);
                if (blockState.getBlock() == state.getBlock() && blockState.get(HALF) == DoubleBlockHalf.LOWER) {
                    world.setBlockState(bottomPos, Blocks.AIR.getDefaultState(), 35);
                    world.syncWorldEvent(player, 2001, bottomPos, Block.getRawIdFromState(blockState));
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (ItemUtil.isWrench(player.getStackInHand(hand))) {
            if (!world.isClient) {
                world.setBlockState(pos, state.cycle(OUTPUT), 10);
                world.updateComparators(pos, state.getBlock());
            }
            return ActionResult.SUCCESS;
        }

        if (!isBottom(state)) {
            pos = pos.down();
            state = world.getBlockState(pos);
        }
        if (world.getBlockEntity(pos) instanceof AbstractShellContainerBlockEntity shellContainer) {
            return shellContainer.onUse(world, pos, player, hand);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof AbstractShellContainerBlockEntity shellContainer
                ? state.get(OUTPUT) == ComparatorOutputType.PROGRESS
                    ? shellContainer.getProgressComparatorOutput()
                    : shellContainer.getInventoryComparatorOutput()
                : 0;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Environment(EnvType.CLIENT)
    public long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN, OUTPUT);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!isBottom(state)) {
            return null;
        }
        return world.isClient ? TickableBlockEntity::clientTicker : TickableBlockEntity::serverTicker;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean isBottom = isBottom(state);
        if (!isOpen(state)) {
            return isBottom ? SOLID_SHAPE_BOTTOM : SOLID_SHAPE_TOP;
        }

        Direction direction = state.get(FACING);
        return switch (direction) {
            case NORTH -> isBottom ? NORTH_SHAPE_BOTTOM : NORTH_SHAPE_TOP;
            case SOUTH -> isBottom ? SOUTH_SHAPE_BOTTOM : SOUTH_SHAPE_TOP;
            case EAST -> isBottom ? EAST_SHAPE_BOTTOM : EAST_SHAPE_TOP;
            case WEST -> isBottom ? WEST_SHAPE_BOTTOM : WEST_SHAPE_TOP;
            default -> throw new IllegalArgumentException();
        };
    }

    public enum ComparatorOutputType implements StringIdentifiable {
        PROGRESS,
        INVENTORY;

        @Override
        public String asString() {
            return this == PROGRESS ? "progress" : "inventory";
        }

        @Override
        public String toString() {
            return this.asString();
        }
    }

    static {
        final VoxelShape ROOF = Block.createCuboidShape(0, 15, 0, 16, 16, 16);
        final VoxelShape FLOOR = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
        final VoxelShape NORTH_WALL = Block.createCuboidShape(0, 0, 0, 16, 16, 1);
        final VoxelShape SOUTH_WALL = Block.createCuboidShape(0, 0, 15, 16, 16, 16);
        final VoxelShape EAST_WALL = Block.createCuboidShape(15, 0, 0, 16, 16, 16);
        final VoxelShape WEST_WALL = Block.createCuboidShape(0, 0, 0, 1, 16, 16);

        final VoxelShape NORTH_SHAPE = VoxelShapes.union(NORTH_WALL, EAST_WALL, WEST_WALL).simplify();
        final VoxelShape SOUTH_SHAPE = VoxelShapes.union(SOUTH_WALL, EAST_WALL, WEST_WALL).simplify();
        final VoxelShape EAST_SHAPE = VoxelShapes.union(NORTH_WALL, SOUTH_WALL, EAST_WALL).simplify();
        final VoxelShape WEST_SHAPE = VoxelShapes.union(NORTH_WALL, SOUTH_WALL, WEST_WALL).simplify();

        SOLID_SHAPE_TOP = VoxelShapes.union(NORTH_WALL, SOUTH_WALL, EAST_WALL, WEST_WALL, ROOF).simplify();
        SOLID_SHAPE_BOTTOM = VoxelShapes.union(NORTH_WALL, SOUTH_WALL, EAST_WALL, WEST_WALL, FLOOR).simplify();

        NORTH_SHAPE_TOP = VoxelShapes.union(NORTH_SHAPE, ROOF).simplify();
        NORTH_SHAPE_BOTTOM = VoxelShapes.union(NORTH_SHAPE, FLOOR).simplify();
        SOUTH_SHAPE_TOP = VoxelShapes.union(SOUTH_SHAPE, ROOF).simplify();
        SOUTH_SHAPE_BOTTOM = VoxelShapes.union(SOUTH_SHAPE, FLOOR).simplify();
        EAST_SHAPE_TOP = VoxelShapes.union(EAST_SHAPE, ROOF).simplify();
        EAST_SHAPE_BOTTOM = VoxelShapes.union(EAST_SHAPE, FLOOR).simplify();
        WEST_SHAPE_TOP = VoxelShapes.union(WEST_SHAPE, ROOF).simplify();
        WEST_SHAPE_BOTTOM = VoxelShapes.union(WEST_SHAPE, FLOOR).simplify();
    }
}
