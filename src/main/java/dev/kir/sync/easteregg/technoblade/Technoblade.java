package dev.kir.sync.easteregg.technoblade;

import com.mojang.authlib.GameProfile;
import dev.kir.sync.Sync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class Technoblade extends AbstractClientPlayerEntity {
    private static final Identifier TECHNOBLADE_SKIN = Sync.locate("textures/entity/technoblade.png");
    private static final GameProfile TECHNOBLADE_GAME_PROFILE = new GameProfile(UUID.fromString("b876ec32-e396-476b-a115-8438d83c67d4"), "Technoblade");
    private static final PlayerListEntry TECHNOBLADE_PLAYER_LIST_ENTRY = new PlayerListEntry(new PlayerListS2CPacket.Entry(TECHNOBLADE_GAME_PROFILE, 0, GameMode.CREATIVE, null));
    // Feel free to add new quotes to this list if you are reading this
    private static final List<Text> TECHNOBLADE_QUOTES = Stream.of(
            "so long nerds",
            "Officer, I drop kicked that child in self defense",
            "PEE VEE PEE",
            "Not even close, baby",
            "Technoblade never dies",
            "\"The opportunity of defeating the enemy is provided by the enemy himself.\" - Sun Tzu, The Art of War",
            "All part of the master plan",
            "i stab children for coins"
        ).map(Text::of).toList();

    private int ticksSinceLastQuote = 0;

    private Technoblade(ClientWorld world) {
        super(world, TECHNOBLADE_GAME_PROFILE);
    }

    public static Technoblade from(LivingEntity entity) {
        if (!(entity.world.isClient)) {
            return null;
        }

        Technoblade Technoblade = new Technoblade((ClientWorld)entity.world);
        Technoblade.copyPose(entity);
        return Technoblade;
    }

    @Override
    protected PlayerListEntry getPlayerListEntry() {
        return TECHNOBLADE_PLAYER_LIST_ENTRY;
    }

    @Override
    public Identifier getSkinTexture() {
        return TECHNOBLADE_SKIN;
    }

    @Override
    public String getModel() {
        return "default";
    }

    @Override
    public boolean isPartVisible(PlayerModelPart modelPart) {
        if (modelPart == PlayerModelPart.CAPE) {
            return Sync.getConfig().renderTechnobladeCape();
        }
        return true;
    }

    @Override
    public SoundEvent getDeathSound() {
        return super.getDeathSound();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
        return super.getHurtSound(source);
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
    }

    public Text getRandomQuote() {
        return TECHNOBLADE_QUOTES.get(this.random.nextInt(TECHNOBLADE_QUOTES.size()));
    }

    public void speak() {
        Text quote = this.getRandomQuote();
        MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.CHAT, new TranslatableText("chat.type.text", this.getDisplayName(), quote.asString()), this.uuid);
    }

    @Override
    public void tick() {
        super.tick();
        if (Sync.getConfig().allowTechnobladeQuotes()) {
            this.tickSpeaking();
        }
    }

    private void tickSpeaking() {
        if (this.ticksSinceLastQuote++ < Sync.getConfig().TechnobladeQuoteDelay()) {
            return;
        }

        if (this.random.nextFloat() < 0.05f) {
            this.speak();
            this.ticksSinceLastQuote = 0;
        }
    }

    public void copyPose(LivingEntity entity) {
        this.limbDistance = entity.limbDistance;
        this.lastLimbDistance = entity.lastLimbDistance;
        this.limbAngle = entity.limbAngle;

        this.copyPositionAndRotation(entity);
        this.lastRenderX = entity.lastRenderX;
        this.lastRenderY = entity.lastRenderY;
        this.lastRenderZ = entity.lastRenderZ;

        this.headYaw = entity.headYaw;
        this.prevHeadYaw = entity.prevHeadYaw;

        this.bodyYaw = entity.bodyYaw;
        this.prevBodyYaw = entity.prevBodyYaw;

        this.setYaw(entity.getYaw());
        this.prevYaw = entity.prevYaw;

        this.dead = entity.isDead();
        this.deathTime = entity.deathTime;
    }
}
