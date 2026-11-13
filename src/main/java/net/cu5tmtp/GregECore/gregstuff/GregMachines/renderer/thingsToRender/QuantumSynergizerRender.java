package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.QuantumSynergizer;
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
public class QuantumSynergizerRender extends DynamicRender<QuantumSynergizer, QuantumSynergizerRender> {

    public static final Codec<QuantumSynergizerRender> CODEC = Codec.unit(new QuantumSynergizerRender());
    public static final DynamicRenderType<QuantumSynergizer, QuantumSynergizerRender> TYPE = new DynamicRenderType<>(QuantumSynergizerRender.CODEC);

    public QuantumSynergizerRender() {}

    @Override
    public DynamicRenderType<QuantumSynergizer, QuantumSynergizerRender> getType() {
        return TYPE;
    }

    private static TextureAtlasSprite WHITE_SPRITE_CACHE;
    private static TextureAtlasSprite CORE_SPRITE_CACHE;

    @Override
    public boolean shouldRender(QuantumSynergizer machine, Vec3 cameraPos) {
        return machine.getRecipeLogic().isWorking();
    }

    @Override
    public void render(QuantumSynergizer machine, float partialTick,
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
        var right = RelativeDirection.RIGHT.getRelative(front, upwards, flipped);

        int fullBright = 15728880;
        long animationTick = machine.getOffsetTimer() / 3;
        int combo = machine.getComboCount();

        float backOffset = 4.0F;
        float orbUpOffset = 4.0F;

        float groundX = (back.getStepX() * backOffset) + 0.5F;
        float groundY = (back.getStepY() * backOffset) + 0.5F;
        float groundZ = (back.getStepZ() * backOffset) + 0.5F;

        float rightOffset = 8.0F;
        float rX = right.getStepX() * rightOffset;
        float rY = right.getStepY() * rightOffset;
        float rZ = right.getStepZ() * rightOffset;

        float uX = up.getStepX();
        float uY = up.getStepY();
        float uZ = up.getStepZ();

        float cOrbX = groundX + uX * orbUpOffset;
        float cOrbY = groundY + uY * orbUpOffset;
        float cOrbZ = groundZ + uZ * orbUpOffset;

        float lOrbX = cOrbX - rX;
        float lOrbY = cOrbY - rY;
        float lOrbZ = cOrbZ - rZ;

        float rOrbX = cOrbX + rX;
        float rOrbY = cOrbY + rY;
        float rOrbZ = cOrbZ + rZ;

        if (combo >= 1) {
            renderOrbAndLightning(poseStack, translucentBuffer, lOrbX, lOrbY, lOrbZ, fullBright, animationTick, 100);
        }
        if (combo >= 2) {
            renderOrbAndLightning(poseStack, translucentBuffer, cOrbX, cOrbY, cOrbZ, fullBright, animationTick, 0);
        }
        if (combo >= 3) {
            renderOrbAndLightning(poseStack, translucentBuffer, rOrbX, rOrbY, rOrbZ, fullBright, animationTick, 200);
        }

        if (combo > 0) {
            float bottomUp = 7.0F;
            float topUp = 14.0F;

            float cTopX = groundX + uX * topUp;
            float cTopY = groundY + uY * topUp;
            float cTopZ = groundZ + uZ * topUp;

            float cBotX = groundX + uX * bottomUp;
            float cBotY = groundY + uY * bottomUp;
            float cBotZ = groundZ + uZ * bottomUp;

            float lBotX = cBotX - rX;
            float lBotY = cBotY - rY;
            float lBotZ = cBotZ - rZ;

            float lTopX = cTopX - rX;
            float lTopY = cTopY - rY;
            float lTopZ = cTopZ - rZ;

            float rBotX = cBotX + rX;
            float rBotY = cBotY + rY;
            float rBotZ = cBotZ + rZ;

            float rTopX = cTopX + rX;
            float rTopY = cTopY + rY;
            float rTopZ = cTopZ + rZ;

            for (int i = 0; i < combo; i++) {
                int seedOff = i * 10;

                renderPrismLightning(poseStack, translucentBuffer, new Vec3(lBotX, lBotY, lBotZ), new Vec3(lTopX, lTopY, lTopZ), animationTick, seedOff + 1, 1.0f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);
                renderPrismLightning(poseStack, translucentBuffer, new Vec3(lTopX, lTopY, lTopZ), new Vec3(cTopX, cTopY, cTopZ), animationTick, seedOff + 2, 0.8f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);

                if (combo >= 2) {
                    renderPrismLightning(poseStack, translucentBuffer, new Vec3(cBotX, cBotY, cBotZ), new Vec3(cTopX, cTopY, cTopZ), animationTick, seedOff, 1.0f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);
                }

                if (combo >= 3) {
                    renderPrismLightning(poseStack, translucentBuffer, new Vec3(rBotX, rBotY, rBotZ), new Vec3(rTopX, rTopY, rTopZ), animationTick, seedOff + 3, 1.0f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);
                    renderPrismLightning(poseStack, translucentBuffer, new Vec3(rTopX, rTopY, rTopZ), new Vec3(cTopX, cTopY, cTopZ), animationTick, seedOff + 4, 0.8f, 1.0f, 1.0f, fullBright, WHITE_SPRITE_CACHE);
                }
            }
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
    public boolean shouldRenderOffScreen(QuantumSynergizer machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(QuantumSynergizer machine) {
        return new AABB(machine.getPos()).inflate(64.0D);
    }

    public static void init() {}
}