package dev.kir.sync.client.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class TextureGenerators {
    public static final TextureGenerator PlayerEntityPartiallyTexturedTextureGenerator = new PlayerEntityPartiallyTexturedTextureGenerator();

    private static class PlayerEntityPartiallyTexturedTextureGenerator implements TextureGenerator {
        private static final int TEXTURE_SIZE = 64;
        private static final int BLOCKS = 32;
        private static final int TRANSPARENT = NativeImage.packColor(0, 0, 0, 0);
        private static final int WHITE = NativeImage.packColor(255, 255, 255, 255);
        @SuppressWarnings("unchecked")
        private static final Pair<Pair<Integer, Integer>, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>[] REGIONS = new Pair[]
        {
            // top
            new Pair<>(new Pair<>(0, 0), new Pair<>(new Pair<>(8, 0), new Pair<>(8, 8))),

            // face
            new Pair<>(new Pair<>(1, 8), new Pair<>(new Pair<>(0, 8), new Pair<>(32, 8))),

            // neck
            new Pair<>(new Pair<>(9, 9), new Pair<>(new Pair<>(16, 0), new Pair<>(8, 8))),
            new Pair<>(new Pair<>(9, 9), new Pair<>(new Pair<>(20, 16), new Pair<>(8, 4))),

            // shoulders
            new Pair<>(new Pair<>(9, 9), new Pair<>(new Pair<>(44, 16), new Pair<>(4, 4))),
            new Pair<>(new Pair<>(9, 9), new Pair<>(new Pair<>(36, 48), new Pair<>(4, 4))),

            // body
            new Pair<>(new Pair<>(9, 20), new Pair<>(new Pair<>(16, 20), new Pair<>(24, 12))),

            // arms
            new Pair<>(new Pair<>(9, 20), new Pair<>(new Pair<>(40, 20), new Pair<>(16, 12))),
            new Pair<>(new Pair<>(9, 20), new Pair<>(new Pair<>(32, 52), new Pair<>(16, 12))),

            // body bottom
            new Pair<>(new Pair<>(21, 21), new Pair<>(new Pair<>(28, 16), new Pair<>(8, 4))),

            // fists
            new Pair<>(new Pair<>(21, 21), new Pair<>(new Pair<>(48, 16), new Pair<>(4, 4))),
            new Pair<>(new Pair<>(21, 21), new Pair<>(new Pair<>(40, 48), new Pair<>(4, 4))),

            // legs' top
            new Pair<>(new Pair<>(21, 21), new Pair<>(new Pair<>(4, 16), new Pair<>(4, 4))),
            new Pair<>(new Pair<>(21, 21), new Pair<>(new Pair<>(20, 48), new Pair<>(4, 4))),

            // legs
            new Pair<>(new Pair<>(21, 32), new Pair<>(new Pair<>(0, 20), new Pair<>(16, 12))),
            new Pair<>(new Pair<>(21, 32), new Pair<>(new Pair<>(16, 52), new Pair<>(16, 12))),
        };

        private final int multiplier;

        public PlayerEntityPartiallyTexturedTextureGenerator() {
            this(1);
        }

        public PlayerEntityPartiallyTexturedTextureGenerator(int multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public Stream<AbstractTexture> generateTextures() {
            int textureSize = TEXTURE_SIZE * multiplier;
            int textureCount = 32 * multiplier;

            List<AbstractTexture> textures = new ArrayList<>(textureCount + 1);
            for (int i = 0; i <= textureCount; ++i) {
                textures.add(generateTexture(textureSize, i / (float)textureCount));
            }

            return textures.stream();
        }

        private static AbstractTexture generateTexture(int textureSize, float emptiness) {
            NativeImage img = new NativeImage(textureSize, textureSize, false);
            int multiplier = textureSize / TEXTURE_SIZE;
            float lastBlock = BLOCKS * multiplier * emptiness;
            img.fillRect(0, 0, textureSize, textureSize, TRANSPARENT);

            for (int i = REGIONS.length - 1; i >= 0; --i) {
                Pair<Integer, Integer> limit = REGIONS[i].getLeft();
                if (lastBlock > limit.getRight()) {
                    break;
                }

                Pair<Integer, Integer> pos = REGIONS[i].getRight().getLeft();
                Pair<Integer, Integer> size = REGIONS[i].getRight().getRight();
                if (lastBlock > limit.getLeft()) {
                    int oldY = pos.getRight() * multiplier;
                    int y = (int)((pos.getRight() + lastBlock - limit.getLeft()) * multiplier);
                    int height = size.getRight() * multiplier - (y - oldY);
                    img.fillRect(pos.getLeft() * multiplier, y, size.getLeft() * multiplier, height, WHITE);
                } else {
                    img.fillRect(pos.getLeft() * multiplier, pos.getRight() * multiplier, size.getLeft() * multiplier, size.getRight() * multiplier, WHITE);
                }
            }

            return new NativeImageBackedTexture(img);
        }
    }
}
