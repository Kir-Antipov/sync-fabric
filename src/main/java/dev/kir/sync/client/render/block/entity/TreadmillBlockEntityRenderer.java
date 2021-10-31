package dev.kir.sync.client.render.block.entity;

import dev.kir.sync.Sync;
import dev.kir.sync.block.SyncBlocks;
import dev.kir.sync.block.TreadmillBlock;
import dev.kir.sync.block.entity.TreadmillBlockEntity;
import dev.kir.sync.client.model.DoubleBlockModel;
import dev.kir.sync.client.model.TreadmillModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class TreadmillBlockEntityRenderer extends DoubleBlockEntityRenderer<TreadmillBlockEntity> {
    private static final Identifier TREADMILL_TEXTURE_ID = Sync.locate("textures/block/treadmill.png");
    private static final BlockState DEFAULT_STATE = SyncBlocks.TREADMILL.getDefaultState().with(TreadmillBlock.PART, TreadmillBlock.Part.FRONT).with(TreadmillBlock.FACING, Direction.SOUTH);

    private final DoubleBlockModel model;

    public TreadmillBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new TreadmillModel();
    }

    @Override
    protected DoubleBlockModel getModel(TreadmillBlockEntity blockEntity, BlockState blockState, float tickDelta) {
        return this.model;
    }

    @Override
    protected BlockState getDefaultState() {
        return DEFAULT_STATE;
    }

    @Override
    protected Identifier getTextureId() {
        return TREADMILL_TEXTURE_ID;
    }
}
