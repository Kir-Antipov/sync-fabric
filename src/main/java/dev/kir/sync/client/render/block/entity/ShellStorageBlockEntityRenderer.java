package dev.kir.sync.client.render.block.entity;

import dev.kir.sync.Sync;
import dev.kir.sync.api.shell.ShellState;
import dev.kir.sync.block.AbstractShellContainerBlock;
import dev.kir.sync.block.SyncBlocks;
import dev.kir.sync.block.entity.ShellStorageBlockEntity;
import dev.kir.sync.client.model.AbstractShellContainerModel;
import dev.kir.sync.client.model.ShellStorageModel;
import dev.kir.sync.entity.ShellEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ShellStorageBlockEntityRenderer extends AbstractShellContainerBlockEntityRenderer<ShellStorageBlockEntity> {
    private static final Identifier SHELL_STORAGE_TEXTURE_ID = Sync.locate("textures/block/shell_storage.png");
    private static final BlockState DEFAULT_STATE = SyncBlocks.SHELL_STORAGE.getDefaultState().with(AbstractShellContainerBlock.HALF, DoubleBlockHalf.LOWER).with(AbstractShellContainerBlock.FACING, Direction.SOUTH).with(AbstractShellContainerBlock.OPEN, false);

    private final ShellStorageModel model;

    public ShellStorageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new ShellStorageModel();
    }

    @Override
    protected AbstractShellContainerModel getShellContainerModel(ShellStorageBlockEntity blockEntity, BlockState blockState, float tickDelta) {
        this.model.ledColor = blockEntity.getIndicatorColor();
        this.model.connectorProgress = blockEntity.getConnectorProgress(tickDelta);
        return this.model;
    }

    @Override
    protected ShellEntity createEntity(ShellState shellState, ShellStorageBlockEntity blockEntity, float tickDelta) {
        ShellEntity entity = shellState.asEntity();
        entity.isActive = shellState.getProgress() >= ShellState.PROGRESS_DONE;
        entity.pitchProgress = entity.isActive ? blockEntity.getConnectorProgress(tickDelta) : 0;
        return entity;
    }

    @Override
    protected BlockState getDefaultState() {
        return DEFAULT_STATE;
    }

    @Override
    protected Identifier getTextureId() {
        return SHELL_STORAGE_TEXTURE_ID;
    }
}
