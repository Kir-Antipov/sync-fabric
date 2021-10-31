package dev.kir.sync.client.model;

import dev.kir.sync.util.math.Radians;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class TreadmillModel extends DoubleBlockModel {
    private static final DoubleBlockProperties.Type BACK = DoubleBlockProperties.Type.FIRST;
    private static final DoubleBlockProperties.Type FRONT = DoubleBlockProperties.Type.SECOND;

    public final ModelPart runningBeltF;
    public final ModelPart runningBeltB;
    public final ModelPart console;
    public final ModelPart consoleMastL;
    public final ModelPart consoleMastR;
    public final ModelPart liftArmF;
    public final ModelPart liftArmL;
    public final ModelPart liftArmR;
    public final ModelPart sideReinLF;
    public final ModelPart sideReinLB;
    public final ModelPart sideReinRF;
    public final ModelPart sideReinRB;
    public final ModelPart sideGuardSupportRFT;
    public final ModelPart sideGuardSupportRBT;
    public final ModelPart sideGuardSupportLFT;
    public final ModelPart sideGuardSupportLBT;
    public final ModelPart sideGuardSupportRF;
    public final ModelPart sideGuardSupportRB;
    public final ModelPart sideGuardSupportLF;
    public final ModelPart sideGuardSupportLB;
    public final ModelPart sideGuardLF;
    public final ModelPart sideGuardLB;
    public final ModelPart sideGuardRF;
    public final ModelPart sideGuardRB;

    public TreadmillModel() {
        super(256, 128);

        ModelPart pitched = this.createRotationTemplate(Radians.R_PI / 72F, 0F, 0F);
        ModelPart mirrored = this.createRotationTemplate(0, 0, Radians.R_PI);
        ModelPart consoleTemplate = this.createRotationTemplate(1.134464F, Radians.R_PI, 0);

        this.runningBeltF = this.addCuboid(FRONT, 31, 0, -9, 17, -16, 18, 4, 31, pitched);
        this.runningBeltB = this.addCuboid(BACK, 31, 31, -9, 18.3F, -14, 18, 4, 31, pitched);

        this.console = this.addCuboid(FRONT, 0, 3, 13, 6, 16, 26, 10, 2, consoleTemplate);
        this.consoleMastL = this.addCuboid(FRONT, 0, 66, 12.5F, 7, 10, 1, 15, 2);
        this.consoleMastR = this.addCuboid(FRONT, 0, 66, -13.5F, 7, 10, 1, 15, 2);

        this.liftArmF = this.addCuboid(FRONT, 0, 0, 12.5F, 24, 15, 25, 2, 1, mirrored);
        this.liftArmL = this.addCuboid(FRONT, 0, 66, 13.5F, 24, -18, 1, 2, 34, mirrored);
        this.liftArmR = this.addCuboid(FRONT, 0, 66, -12.5F, 24, -18, 1, 2, 34, mirrored);

        this.sideReinLF = this.addCuboid(FRONT, 130, 35, 9, 16.5F, -16.1F, 4, 5, 32, pitched);
        this.sideReinLB = this.addCuboid(BACK, 130, 35, 9, 17.9F, -16, 4, 5, 32, pitched);

        this.sideReinRF = this.addCuboid(FRONT, 130, 35, -13, 16.5F, -16.1F, 4, 5, 32, pitched);
        this.sideReinRB = this.addCuboid(BACK, 130, 35, -13, 17.9F, -16, 4, 5, 32, pitched);

        this.sideGuardSupportRFT = this.addCuboid(FRONT, 86, 88, 12.5F, 6.5F, -15, 1, 1, 16, pitched);
        this.sideGuardSupportRBT = this.addCuboid(BACK, 86, 88, 12.5F, 7.2F, 1.05F, 1, 1, 16, pitched);

        this.sideGuardSupportLFT = this.addCuboid(FRONT, 86, 88, -13.5F, 6.5F, -15, 1, 1, 16, pitched);
        this.sideGuardSupportLBT = this.addCuboid(BACK, 86, 88, -13.5F, 7.2F, 1.05F, 1, 1, 16, pitched);

        this.sideGuardSupportRF = this.addCuboid(FRONT, 146, 0, 12.5F, 6, 0, 1, 11, 1, pitched);
        this.sideGuardSupportRB = this.addCuboid(BACK, 146, 0, 12.5F, 7.2F, 1.05F, 1, 11, 1, pitched);

        this.sideGuardSupportLF = this.addCuboid(FRONT, 146, 0, -13.5F, 6, 0, 1, 11, 1, pitched);
        this.sideGuardSupportLB = this.addCuboid(BACK, 146, 0, -13.5F, 7.2F, 1.05F, 1, 11, 1, pitched);

        this.sideGuardLF = this.addCuboid(FRONT, 210, 15, -13.1F, 7.35F, -15, 0, 10, 15, pitched);
        this.sideGuardLB = this.addCuboid(BACK, 180, 15, -13.1F, 8, 2, 0, 10, 15, pitched);

        this.sideGuardRF = this.addCuboid(FRONT, 210, 15, 13.1F, 7.35F, -15, 0, 10, 15, pitched);
        this.sideGuardRB = this.addCuboid(BACK, 180, 15, 13.1F, 8, 2, 0, 10, 15, pitched);
    }

    @Override
    protected void translate(MatrixStack matrices) {
        matrices.translate(0, 0, 2F);
    }
}
