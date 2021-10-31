package me.kirantipov.mods.sync.client.render.block.entity;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.api.shell.ShellState;
import me.kirantipov.mods.sync.block.AbstractShellContainerBlock;
import me.kirantipov.mods.sync.block.SyncBlocks;
import me.kirantipov.mods.sync.block.entity.ShellConstructorBlockEntity;
import me.kirantipov.mods.sync.client.model.AbstractShellContainerModel;
import me.kirantipov.mods.sync.client.model.ShellConstructorModel;
import me.kirantipov.mods.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ShellConstructorBlockEntityRenderer extends AbstractShellContainerBlockEntityRenderer<ShellConstructorBlockEntity> {
    private static final Identifier SHELL_CONSTRUCTOR_TEXTURE_ID = Sync.locate("textures/block/shell_constructor.png");
    private static final BlockState DEFAULT_STATE = SyncBlocks.SHELL_CONSTRUCTOR.getDefaultState().with(AbstractShellContainerBlock.HALF, DoubleBlockHalf.LOWER).with(AbstractShellContainerBlock.FACING, Direction.SOUTH);

    private final ShellConstructorModel model;

    public ShellConstructorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new ShellConstructorModel();
    }

    @Override
    protected AbstractShellContainerModel getShellContainerModel(ShellConstructorBlockEntity blockEntity, BlockState blockState, float tickDelta) {
        this.model.buildProgress = blockEntity.getShellState() == null ? 0F : blockEntity.getShellState().getProgress();
        this.model.showInnerParts = blockEntity.hasWorld();
        return this.model;
    }

    @Override
    protected ShellEntity createEntity(ShellState shellState, ShellConstructorBlockEntity blockEntity, float tickDelta) {
        ShellEntity entity = shellState.asEntity();
        entity.isActive = false;
        entity.pitchProgress = 0;
        return entity;
    }

    @Override
    protected BlockState getDefaultState() {
        return DEFAULT_STATE;
    }

    @Override
    protected Identifier getTextureId() {
        return SHELL_CONSTRUCTOR_TEXTURE_ID;
    }
}
