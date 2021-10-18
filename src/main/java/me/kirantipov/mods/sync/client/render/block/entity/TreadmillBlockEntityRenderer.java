package me.kirantipov.mods.sync.client.render.block.entity;

import me.kirantipov.mods.sync.Sync;
import me.kirantipov.mods.sync.block.SyncBlocks;
import me.kirantipov.mods.sync.block.TreadmillBlock;
import me.kirantipov.mods.sync.block.entity.TreadmillBlockEntity;
import me.kirantipov.mods.sync.client.model.DoubleBlockModel;
import me.kirantipov.mods.sync.client.model.TreadmillModel;
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
