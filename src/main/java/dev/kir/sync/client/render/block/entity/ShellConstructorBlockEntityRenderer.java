package dev.kir.sync.client.render.block.entity;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.block.SyncBlocks;
import dev.kir.sync.block.entity.ShellConstructorBlockEntity;
import dev.kir.sync.client.model.AbstractShellContainerModel;
import dev.kir.sync.client.model.ShellConstructorModel;
import dev.kir.sync.entity.ShellEntity;
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
