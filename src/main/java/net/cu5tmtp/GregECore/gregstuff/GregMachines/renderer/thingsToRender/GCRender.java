package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.GenesisCrucible;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

@SuppressWarnings("removal")
public class GCRender extends DynamicRender<GenesisCrucible, GCRender> {

    public static final Codec<GCRender> CODEC = Codec.unit(new GCRender());
    public static final DynamicRenderType<GenesisCrucible, GCRender> TYPE = new DynamicRenderType<>(GCRender.CODEC);

    private static TextureAtlasSprite WHITE_SPRITE_CACHE;
    private static TextureAtlasSprite CORE_SPRITE_CACHE;

    private static final float SPEED = 0.1f;
    private static final float MAX_DISTANCE = 10.0f;
    private static final float START_OFFSET = -3.0f;
    private static final int RING_COUNT = 3;
    private static final float FADE_ZONE = 1.0f;
    private static final float RING_RADIUS = 2.65f;
    private static final float RING_THICKNESS = 0.15f;

    private record CaseConfig(String name, int forward, int above, int right, float r, float g, float b) {}
    private static final CaseConfig[] CASE_CONFIGS = new CaseConfig[] {
            new CaseConfig("genesiscruciblecaseone", 4, 17, 10, 0.05f, 0.05f, 0.05f),
            new CaseConfig("genesiscruciblecasetwo", 4, 17, -10, 1.0f, 1.0f, 0.0f),
            new CaseConfig("genesiscruciblecasethree", -16, 17, -10, 1.0f, 1.0f, 0.0f),
            new CaseConfig("genesiscruciblecasefour", -16, 17, 10, 0.2f, 1.0f, 0.2f)
    };

    public GCRender() {}

    @Override
    public DynamicRenderType<GenesisCrucible, GCRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(GenesisCrucible machine, Vec3 cameraPos) {
        return machine.getRecipeLogic().isWorking();
    }

    @Override
    public void render(GenesisCrucible machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        if (WHITE_SPRITE_CACHE == null) {
            WHITE_SPRITE_CACHE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft:block/white_concrete"));
        }
        if (CORE_SPRITE_CACHE == null) {
            CORE_SPRITE_CACHE = WHITE_SPRITE_CACHE;
        }

        VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderType.translucent());

        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();

        var up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);

        int fullBright = 15728880;
        long animationTick = machine.getOffsetTimer() / 3;

        float upOffset = 19.0F;
        float backOffset = 6.0F;

        float p1X = (up.getStepX() * upOffset) + (back.getStepX() * backOffset) + 0.5F;
        float p1Y = (up.getStepY() * upOffset) + (back.getStepY() * backOffset) + 0.5F;
        float p1Z = (up.getStepZ() * upOffset) + (back.getStepZ() * backOffset) + 0.5F;

        renderOrbAndLightning(poseStack, translucentBuffer, p1X, p1Y, p1Z, fullBright, animationTick, 0);

        Direction forwardDir = machine.getFrontFacing();
        Direction rightDir = forwardDir.getClockWise();

        int mask = machine.activeCasesMask;

        for (int i = 0; i < CASE_CONFIGS.length; i++) {
            CaseConfig config = CASE_CONFIGS[i];

            boolean isCaseActive = (mask & (1 << i)) != 0;

            if (isCaseActive) {
                renderCaseRings(poseStack, bufferSource, config, forwardDir, rightDir, partialTick, machine.getOffsetTimer());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderCaseRings(PoseStack stack, MultiBufferSource bufferSource, CaseConfig config,
                                 Direction forward, Direction right, float partialTicks, long offsetTimer) {
        float time = offsetTimer + partialTicks;
        Direction.Axis axis = Direction.Axis.Y;

        for (int i = 0; i < RING_COUNT; i++) {
            float currentDist = (time * SPEED + i * (MAX_DISTANCE / RING_COUNT)) % MAX_DISTANCE;

            float ringAlpha = 1.0f;
            if (currentDist < FADE_ZONE) {
                ringAlpha *= (currentDist / FADE_ZONE);
            } else if (currentDist > MAX_DISTANCE - FADE_ZONE) {
                ringAlpha *= ((MAX_DISTANCE - currentDist) / FADE_ZONE);
            }

            if (ringAlpha <= 0.01f) continue;

            float upOffset = config.above() + START_OFFSET + currentDist;

            float posX = 0.5F + (forward.getStepX() * config.forward()) + (right.getStepX() * config.right());
            float posY = 0.5F + upOffset;
            float posZ = 0.5F + (forward.getStepZ() * config.forward()) + (right.getStepZ() * config.right());

            stack.pushPose();
            stack.translate(posX, posY, posZ);

            VertexConsumer ringBuffer = bufferSource.getBuffer(GTRenderTypes.getLightRing());
            RenderBufferHelper.renderRing(stack, ringBuffer,
                    0.0f, 0.0f, 0.0f,
                    RING_RADIUS, RING_THICKNESS, 10, 20,
                    config.r(), config.g(), config.b(), ringAlpha, axis);

            stack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderOrbAndLightning(PoseStack poseStack, VertexConsumer buffer, float x, float y, float z, int fullBright, long animationTick, int seedOffset) {
        renderTexturedSphere(poseStack, buffer, x, y, z, 0.1F, 24, 24,
                1.0f, 1.0f, 1.0f, 0.9f, CORE_SPRITE_CACHE, fullBright);

        int boltCount = 5;

        for (int i = 0; i < boltCount; i++) {
            long seed = animationTick + (i * 1337L) + seedOffset;
            Random rand = new Random(seed);

            float length = 1.0F + rand.nextFloat();

            float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
            float pitch = (rand.nextFloat() - 0.5F) * (float) Math.PI;

            float endX = x + Mth.cos(yaw) * Mth.cos(pitch) * length;
            float endY = y + Mth.sin(pitch) * length;
            float endZ = z + Mth.sin(yaw) * Mth.cos(pitch) * length;

            Vec3 start = new Vec3(x, y, z);
            Vec3 end = new Vec3(endX, endY, endZ);

            renderPrismLightning(poseStack, buffer, start, end, animationTick, i + seedOffset,
                    1.0f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderPrismLightning(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, float time, int index, float r, float g, float b, int light, TextureAtlasSprite sprite) {
        Matrix4f matrix = poseStack.last().pose();
        Vec3 current = start;
        int segments = 8;

        Vec3 diff = end.subtract(start);
        double length = diff.length();
        Vec3 direction = diff.normalize();
        float segmentLength = (float) (length / segments);

        float deviation = 0.15f;
        float thickness = 0.035f;

        Random rand = new Random();
        rand.setSeed((long) (index * 1000L + (time * 0.4F)));

        for (int i = 0; i < segments; i++) {
            Vec3 nextBase = start.add(direction.scale((i + 1) * segmentLength));
            Vec3 next;
            if (i < segments - 1) {
                next = nextBase.add(
                        (rand.nextFloat() - 0.5f) * deviation,
                        (rand.nextFloat() - 0.5f) * deviation,
                        (rand.nextFloat() - 0.5f) * deviation
                );
            } else {
                next = nextBase;
            }

            drawLightningPrism(buffer, matrix, current, next, thickness, r, g, b, 0.8f, light, sprite);
            current = next;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawLightningPrism(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end, float thickness, float r, float g, float b, float a, int light, TextureAtlasSprite sprite) {
        Vector3f dir = new Vector3f((float)(end.x - start.x), (float)(end.y - start.y), (float)(end.z - start.z));

        Vector3f up = new Vector3f(0, 1, 0);
        up.cross(dir);
        if (up.lengthSquared() < 0.001f) up.set(1, 0, 0);
        up.normalize().mul(thickness);

        Vector3f side = new Vector3f(up);
        side.cross(dir);
        side.normalize().mul(thickness);

        Vector3f s1 = new Vector3f((float)start.x, (float)start.y, (float)start.z).add(up).add(side);
        Vector3f s2 = new Vector3f((float)start.x, (float)start.y, (float)start.z).add(up).sub(side);
        Vector3f s3 = new Vector3f((float)start.x, (float)start.y, (float)start.z).sub(up).sub(side);
        Vector3f s4 = new Vector3f((float)start.x, (float)start.y, (float)start.z).sub(up).add(side);

        Vector3f e1 = new Vector3f((float)end.x, (float)end.y, (float)end.z).add(up).add(side);
        Vector3f e2 = new Vector3f((float)end.x, (float)end.y, (float)end.z).add(up).sub(side);
        Vector3f e3 = new Vector3f((float)end.x, (float)end.y, (float)end.z).sub(up).sub(side);
        Vector3f e4 = new Vector3f((float)end.x, (float)end.y, (float)end.z).sub(up).add(side);

        addFace(buffer, matrix, s1, s2, e2, e1, r, g, b, a, light, sprite);
        addFace(buffer, matrix, s2, s3, e3, e2, r, g, b, a, light, sprite);
        addFace(buffer, matrix, s3, s4, e4, e3, r, g, b, a, light, sprite);
        addFace(buffer, matrix, s4, s1, e1, e4, r, g, b, a, light, sprite);
    }

    @OnlyIn(Dist.CLIENT)
    private void addFace(VertexConsumer buffer, Matrix4f mat, Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float r, float g, float b, float a, int light, TextureAtlasSprite sprite) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        float nx = 0, ny = 1, nz = 0;

        buffer.vertex(mat, p1.x, p1.y, p1.z).color(r, g, b, a).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        buffer.vertex(mat, p2.x, p2.y, p2.z).color(r, g, b, a).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        buffer.vertex(mat, p3.x, p3.y, p3.z).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        buffer.vertex(mat, p4.x, p4.y, p4.z).color(r, g, b, a).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderTexturedSphere(PoseStack poseStack, VertexConsumer buffer, float x, float y, float z,
                                      float radius, int longitudes, int latitudes,
                                      float red, float green, float blue, float alpha,
                                      TextureAtlasSprite sprite, int light) {
        Matrix4f mat = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();

        for (int i = 0; i < latitudes; i++) {
            float fLat0 = (float) i / latitudes;
            float fLat1 = (float) (i + 1) / latitudes;
            float angleLat0 = (float) Math.PI * (-0.5f + fLat0);
            float angleLat1 = (float) Math.PI * (-0.5f + fLat1);

            float sinLat0 = Mth.sin(angleLat0);
            float cosLat0 = Mth.cos(angleLat0);
            float sinLat1 = Mth.sin(angleLat1);
            float cosLat1 = Mth.cos(angleLat1);

            float v0 = sprite.getV(fLat0 * 16.0f);
            float v1 = sprite.getV(fLat1 * 16.0f);

            for (int j = 0; j < longitudes; j++) {
                float fLng0 = (float) j / longitudes;
                float fLng1 = (float) (j + 1) / longitudes;
                float lng0 = (float) (2.0 * Math.PI * fLng0);
                float lng1 = (float) (2.0 * Math.PI * fLng1);

                float u0 = sprite.getU(fLng0 * 16.0f);
                float u1 = sprite.getU(fLng1 * 16.0f);

                addVertex(buffer, mat, normalMat, x, y, z, radius, lng0, sinLat0, cosLat0, u0, v0, red, green, blue, alpha, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, lng0, sinLat1, cosLat1, u0, v1, red, green, blue, alpha, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, lng1, sinLat1, cosLat1, u1, v1, red, green, blue, alpha, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, lng1, sinLat0, cosLat0, u1, v0, red, green, blue, alpha, light);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addVertex(VertexConsumer buffer, Matrix4f mat, Matrix3f normalMat,
                           float x, float y, float z, float radius,
                           float lng, float sinLat, float cosLat,
                           float u, float v, float r, float g, float b, float a, int light) {
        float nx = Mth.cos(lng) * cosLat;
        float ny = sinLat;
        float nz = Mth.sin(lng) * cosLat;

        buffer.vertex(mat, x + nx * radius, y + ny * radius, z + nz * radius)
                .color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normalMat, nx, ny, nz).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(GenesisCrucible machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(GenesisCrucible machine) {
        return new AABB(machine.getPos()).inflate(40.0D);
    }

    public static void init(){}
}