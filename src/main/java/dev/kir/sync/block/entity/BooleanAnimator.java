package dev.kir.sync.block.entity;

import net.minecraft.util.math.MathHelper;

public class BooleanAnimator {
    private boolean value;
    private float progress;
    private float lastProgress;
    private final float stepDelta;

    public BooleanAnimator(boolean value) {
        this(value, 0.1F);
    }

    public BooleanAnimator(boolean value, float stepDelta) {
        this.value = value;
        this.stepDelta = stepDelta;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public float getProgress(float delta) {
        return MathHelper.lerp(delta, this.lastProgress, this.progress);
    }

    public void step() {
        this.lastProgress = this.progress;
        if (!this.value && this.progress > 0F) {
            this.progress = Math.max(this.progress - stepDelta, 0F);
        } else if (this.value && this.progress < 1F) {
            this.progress = Math.min(this.progress + stepDelta, 1F);
        }
    }
}