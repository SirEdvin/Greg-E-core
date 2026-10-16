package net.cu5tmtp.GregECore.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Samples actual native baked UVs, so correct sprite IDs cannot hide broken pixels. */
final class WorldEaterCasingPixelChecks {
    static List<String> verify(Minecraft client) {
        List<String> errors = new ArrayList<>();
        Map<ResourceLocation, NativeImage> images = new HashMap<>();
        try {
            for (int color = 0; color < 4; color++) for (Direction face : Direction.values()) {
                BlockPos root = WorldEaterCasingConnectionChecks.cube(color);
                BlockPos first = cell(root, face, 0, 0);
                var state = client.level.getBlockState(first);
                var model = client.getBlockRenderer().getBlockModel(state);
                var fallback = model.getQuads(state, face, RandomSource.create());
                for (int frame = 0; frame < 8; frame++) {
                    int[] reference = raster(client, fallback, face, frame, images);
                    int[] panel = new int[48 * 48];
                    for (int u = 0; u < 3; u++) for (int v = 0; v < 3; v++) {
                        int[] pixels = raster(client, WorldEaterCasingConnectionChecks.quads(client,
                                cell(root, face, u, v), face), face, frame, images);
                        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
                            panel[((2 - v) * 16 + y) * 48 + u * 16 + x] = pixels[y * 16 + x];
                    }
                    boolean correct = true;
                    for (int edge : new int[]{0, 1, 2, 45, 46, 47}) for (int along = 3; along < 45; along++) {
                        correct &= panel[edge * 48 + along] == reference[(edge % 16) * 16 + 8];
                        correct &= panel[along * 48 + edge] == reference[8 * 16 + edge % 16];
                    }
                    if (!correct) errors.add("Broken sampled perimeter pixels: color=" + color + ", face=" + face + ", frame=" + frame);
                }
            }
        } catch (Exception error) {
            errors.add("Native perimeter sampling failed: " + error);
        } finally {
            images.values().forEach(NativeImage::close);
        }
        return errors;
    }

    private static BlockPos cell(BlockPos root, Direction face, int u, int v) {
        int plane = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 2 : 0;
        return switch (face.getAxis()) {
            case X -> root.offset(plane, v, u);
            case Y -> root.offset(u, plane, v);
            case Z -> root.offset(u, v, plane);
        };
    }

    private static float position(int[] data, int vertex, int component, Direction face) {
        int axis = component == 0 ? (face.getAxis() == Direction.Axis.X ? 2 : 0)
                : (face.getAxis() == Direction.Axis.Y ? 2 : 1);
        float value = Float.intBitsToFloat(data[vertex * (data.length / 4) + axis]);
        return component == 0 ? value : 1 - value;
    }

    private static float uv(int[] data, int vertex, int component) {
        return Float.intBitsToFloat(data[vertex * (data.length / 4) + 4 + component]);
    }

    private static int[] raster(Minecraft client, List<BakedQuad> quads, Direction face, int frame,
                                Map<ResourceLocation, NativeImage> images) throws IOException {
        int[] pixels = new int[16 * 16];
        boolean[] covered = new boolean[pixels.length];
        for (var quad : quads) {
            var sprite = quad.getSprite();
            var id = sprite.contents().name();
            if (id.getPath().contains("_emissive")) continue;
            NativeImage image = images.get(id);
            if (image == null) {
                var resource = new ResourceLocation(id.getNamespace(), "textures/" + id.getPath() + ".png");
                try (var stream = client.getResourceManager().open(resource)) {
                    image = NativeImage.read(stream);
                }
                images.put(id, image);
            }
            var data = quad.getVertices();
            float ax = position(data, 0, 0, face), ay = position(data, 0, 1, face);
            float bx = position(data, 1, 0, face) - ax, by = position(data, 1, 1, face) - ay;
            float cx = position(data, 3, 0, face) - ax, cy = position(data, 3, 1, face) - ay;
            float determinant = bx * cy - by * cx;
            if (Math.abs(determinant) < .0001f) throw new IllegalStateException("Degenerate casing quad");
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                float rx = (x + .5f) / 16 - ax, ry = (y + .5f) / 16 - ay;
                float s = (rx * cy - ry * cx) / determinant, t = (bx * ry - by * rx) / determinant;
                if (s < 0 || s > 1 || t < 0 || t > 1) continue;
                int[] texel = new int[2];
                for (int component = 0; component < 2; component++) {
                    float value = uv(data, 0, component) + s * (uv(data, 1, component) - uv(data, 0, component))
                            + t * (uv(data, 3, component) - uv(data, 0, component));
                    float min = component == 0 ? sprite.getU0() : sprite.getV0();
                    float max = component == 0 ? sprite.getU1() : sprite.getV1();
                    texel[component] = Math.max(0, Math.min(image.getWidth() - 1, (int) ((value - min) / (max - min) * image.getWidth())));
                }
                pixels[y * 16 + x] = image.getPixelRGBA(texel[0], frame * image.getWidth() + texel[1]);
                covered[y * 16 + x] = true;
            }
        }
        for (boolean pixel : covered) if (!pixel) throw new IllegalStateException("Native quad left an unsampled pixel");
        return pixels;
    }
}
