package dev.kir.sync.api.shell;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Describes priority of one shell over others.
 */
public enum ShellPriority implements StringIdentifiable {
    /**
     * White shells are prioritized.
     */
    WHITE(DyeColor.WHITE),

    /**
     * Orange shells are prioritized.
     */
    ORANGE(DyeColor.ORANGE),

    /**
     * Magenta shells are prioritized.
     */
    MAGENTA(DyeColor.MAGENTA),

    /**
     * Light blue shells are prioritized.
     */
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),

    /**
     * Yellow shells are prioritized.
     */
    YELLOW(DyeColor.YELLOW),

    /**
     * Lime shells are prioritized.
     */
    LIME(DyeColor.LIME),

    /**
     * Pink shells are prioritized.
     */
    PINK(DyeColor.PINK),

    /**
     * Gray shells are prioritized.
     */
    GRAY(DyeColor.GRAY),

    /**
     * Light gray shells are prioritized.
     */
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),

    /**
     * Cyan shells are prioritized.
     */
    CYAN(DyeColor.CYAN),

    /**
     * Purple shells are prioritized.
     */
    PURPLE(DyeColor.PURPLE),

    /**
     * Blue shells are prioritized.
     */
    BLUE(DyeColor.BLUE),

    /**
     * Brown shells are prioritized.
     */
    BROWN(DyeColor.BROWN),

    /**
     * Green shells are prioritized.
     */
    GREEN(DyeColor.GREEN),

    /**
     * Red shells are prioritized.
     */
    RED(DyeColor.RED),

    /**
     * Black shells are prioritized.
     */
    BLACK(DyeColor.BLACK),

    /**
     * Nearest shells are prioritized.
     */
    NEAREST(16, "nearest", null, true),

    /**
     * Non-artificial shells are prioritized.
     */
    NATURAL(17, "natural", true, null);


    private final int id;
    private final String name;
    private final Boolean nearest;
    private final Boolean natural;


    ShellPriority(DyeColor color) {
        this(color.getId(), color.getName(), null, null);
    }

    ShellPriority(int id, String name, Boolean natural, Boolean nearest) {
        this.id = id;
        this.name = name;
        this.natural = natural;
        this.nearest = nearest;
    }


    /**
     * @return the name of this enum constant.
     */
    @Override
    public String asString() {
        return this.name;
    }

    /**
     * @return the name of this enum constant.
     */
    @Override
    public String toString() {
        return this.name;
    }


    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priority.
     *
     * @param priority The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priority.
     */
    public static Comparator<ShellState> asComparator(ShellPriority priority) {
        return asComparator(null, null, priority);
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(ShellPriority... priorities) {
        return asComparator(null, null, priorities);
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(Collection<ShellPriority> priorities) {
        return asComparator(null, null, priorities);
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(Stream<ShellPriority> priorities) {
        return asComparator(null, null, priorities);
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priority.
     *
     * @param world The identifier of the world the given pos belongs to.
     * @param pos The position in the given world used to sort shells by their positions.
     * @param priority The {@linkplain ShellPriority} value.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priority.
     */
    public static Comparator<ShellState> asComparator(Identifier world, BlockPos pos, ShellPriority priority) {
        return (a, b) -> {
            DyeColor color = priority.id >= 0 && priority.id <= 15 ? DyeColor.byId(priority.id) : null;
            if (color != null && (a.getColor() != b.getColor()) && (a.getColor() == color || b.getColor() == color)) {
                return a.getColor() == color ? -1 : 1;
            }

            if (priority.natural != null && a.isArtificial() != b.isArtificial()) {
                return (a.isArtificial() ? 1 : -1) * (priority.natural ? 1 : -1);
            }

            if (priority.nearest != null && world != null && pos != null) {
                boolean aWorld = a.getWorld().equals(world);
                boolean bWorld = b.getWorld().equals(world);
                if (aWorld && bWorld) {
                    return Double.compare(a.getPos().getSquaredDistance(pos), b.getPos().getSquaredDistance(pos));
                }
                return Boolean.compare(bWorld, aWorld);
            }
            return 0;
        };
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param world The identifier of the world the given pos belongs to.
     * @param pos The position in the given world used to sort shells by their positions.
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(Identifier world, BlockPos pos, ShellPriority... priorities) {
        return asComparator(world, pos, Arrays.stream(priorities));
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param world The identifier of the world the given pos belongs to.
     * @param pos The position in the given world used to sort shells by their positions.
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(Identifier world, BlockPos pos, Collection<ShellPriority> priorities) {
        return asComparator(world, pos, priorities.stream());
    }

    /**
     * Creates a comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     *
     * @param world The identifier of the world the given pos belongs to.
     * @param pos The position in the given world used to sort shells by their positions.
     * @param priorities The {@linkplain ShellPriority} values.
     * @return the comparator that can be used to sort {@linkplain ShellState}s with respect to the specified priorities.
     */
    public static Comparator<ShellState> asComparator(Identifier world, BlockPos pos, Stream<ShellPriority> priorities) {
        return priorities.reduce((__, ___) -> 0, (acc, x) -> acc.thenComparing(asComparator(world, pos, x)), Comparator::thenComparing);
    }
}
