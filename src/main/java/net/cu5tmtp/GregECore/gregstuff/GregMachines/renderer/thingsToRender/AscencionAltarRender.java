package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.AscencionAltar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("removal")
public class AscencionAltarRender extends DynamicRender<AscencionAltar, AscencionAltarRender> {

    public static final Codec<AscencionAltarRender> CODEC = Codec.unit(new AscencionAltarRender());
    public static final DynamicRenderType<AscencionAltar, AscencionAltarRender> TYPE = new DynamicRenderType<>(AscencionAltarRender.CODEC);

    private static TextureAtlasSprite LAVA_SPRITE_CACHE;
    private static TextureAtlasSprite WHITE_SPRITE_CACHE;
    private static TextureAtlasSprite BLACK_SPRITE_CACHE;

    private static final Map<BlockPos, Float> SMOOTH_PROGRESS = new HashMap<>();

    private static final Vec3 BLOCK_CENTER = new Vec3(0.5, 0.5, 0.5);

    private static final float[][] FINGER_POSITIONS = {
            {0.45f, 0.0f, 0.32f},
            {0.15f, 0.0f, 0.45f},
            {0.0f, 0.0f, 0.48f},
            {-0.15f, 0.0f, 0.45f},
            {-0.35f, 0.0f, 0.35f}
    };

    private final Random rand = new Random();

    public AscencionAltarRender() {}

    @Override
    public DynamicRenderType<AscencionAltar, AscencionAltarRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(AscencionAltar machine, Vec3 cameraPos) {
        boolean isWorking = machine.getRecipeLogic().isWorking();
        float progress = SMOOTH_PROGRESS.getOrDefault(machine.getPos(), 0.0f);

        if (!isWorking && progress <= 0.0f) {
            SMOOTH_PROGRESS.remove(machine.getPos());
            return false;
        }
        return true;
    }

    @Override
    public void render(AscencionAltar machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {

        if (LAVA_SPRITE_CACHE == null) {
            IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(Fluids.LAVA);
            LAVA_SPRITE_CACHE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(props.getStillTexture());
        }
        if (WHITE_SPRITE_CACHE == null) {
            WHITE_SPRITE_CACHE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft:block/white_concrete"));
        }
        if (BLACK_SPRITE_CACHE == null) {
            BLACK_SPRITE_CACHE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft:block/black_concrete"));
        }

        VertexConsumer solidBuffer = buffer.getBuffer(RenderType.cutout());
        int fullBright = 15728880;

        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();

        var dirUp = RelativeDirection.UP.getRelative(front, upwards, flipped);
        var dirLeft = RelativeDirection.LEFT.getRelative(front, upwards, flipped);
        var dirBack = RelativeDirection.BACK.getRelative(front, upwards, flipped);

        Vec3 origUp = new Vec3(dirUp.getStepX(), dirUp.getStepY(), dirUp.getStepZ());
        Vec3 origLeft = new Vec3(dirLeft.getStepX(), dirLeft.getStepY(), dirLeft.getStepZ());
        Vec3 origBack = new Vec3(dirBack.getStepX(), dirBack.getStepY(), dirBack.getStepZ());
        Vec3 origFront = origBack.scale(-1);

        Vec3 portalCenter = BLOCK_CENTER.add(origUp.scale(7.0)).add(origFront.scale(5.0));
        float maxOffset = 2.25f;

        float time = machine.getOffsetTimer() + partialTick;
        BlockPos pos = machine.getPos();
        var recipeLogic = machine.getRecipeLogic();
        boolean isWorking = recipeLogic.isWorking();

        float currentProgress = SMOOTH_PROGRESS.getOrDefault(pos, 0.0F);

        if (isWorking && recipeLogic.getMaxProgress() > 0) {
            float serverProgress = (float) recipeLogic.getProgress() / (float) recipeLogic.getMaxProgress();
            float tickIncrement = 1.0F / (float) recipeLogic.getMaxProgress();

            float targetProgress = Mth.clamp(serverProgress + (partialTick * tickIncrement), 0.0F, 1.0F);

            if (Math.abs(currentProgress - targetProgress) > 0.8F) {
                currentProgress = targetProgress;
            } else {
                currentProgress = currentProgress + (targetProgress - currentProgress) * 0.15F;
            }
            SMOOTH_PROGRESS.put(pos, currentProgress);
        } else {
            if (currentProgress > 0.0F) {
                currentProgress -= 0.05F * partialTick;
                if (currentProgress < 0.0F) {
                    currentProgress = 0.0F;
                    SMOOTH_PROGRESS.remove(pos);
                } else {
                    SMOOTH_PROGRESS.put(pos, currentProgress);
                }
            } else {
                SMOOTH_PROGRESS.remove(pos);
            }
        }

        float p = currentProgress;
        float portalScale = 0.0f;
        float handOffset = 0.0f;
        boolean fireLasers = false;

        if (p < 0.2f) {
            portalScale = p / 0.2f;
        } else if (p < 0.4f) {
            portalScale = 1.0f;
            handOffset = ((p - 0.2f) / 0.2f) * maxOffset;
        } else if (p < 0.6f) {
            portalScale = 1.0f;
            handOffset = maxOffset;
            fireLasers = true;
        } else if (p < 0.8f) {
            portalScale = 1.0f;
            handOffset = maxOffset * (1.0f - ((p - 0.6f) / 0.2f));
        } else {
            portalScale = Math.max(0.0f, 1.0f - ((p - 0.8f) / 0.2f));
        }

        if (portalScale > 0) {
            poseStack.pushPose();
            poseStack.translate(portalCenter.x, portalCenter.y, portalCenter.z);
            poseStack.scale(portalScale * 1.5f, portalScale * 1.5f, portalScale * 1.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(time * 3.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * 1.5F));
            renderTexturedSphere(poseStack, solidBuffer, 0, 0, 0, 1.0f, 24, 24, 0.1f, 0.1f, 0.1f, 1.0f, BLACK_SPRITE_CACHE, fullBright);
            poseStack.popPose();
        }

        Vec3 handPos = portalCenter.add(origBack.scale(handOffset));
        if (handOffset > 0.01f) {
            renderHand(poseStack, solidBuffer, handPos, origBack, time, fullBright);
        }

        if (fireLasers) {
            Vec3 laserTarget = handPos;
            Vec3[] laserSources = new Vec3[]{
                    BLOCK_CENTER.add(origBack.scale(6)).add(origUp.scale(5)),
                    BLOCK_CENTER.add(origLeft.scale(-2).add(origBack.scale(5)).add(origUp.scale(4))),
                    BLOCK_CENTER.add(origLeft.scale(2).add(origBack.scale(6)).add(origUp.scale(4))),
                    BLOCK_CENTER.add(origLeft.scale(3).add(origBack.scale(3)).add(origUp.scale(4))),
                    BLOCK_CENTER.add(origLeft.scale(5).add(origFront.scale(1).add(origUp.scale(2))))
            };

            VertexConsumer beamBuffer = buffer.getBuffer(RenderType.lightning());
            int index = 0;

            for (Vec3 source : laserSources) {
                Vec3 laserVec = laserTarget.subtract(source);
                float length = (float) laserVec.length();
                if (length < 0.01f) continue;

                renderPrismLightning(poseStack, beamBuffer, source, laserTarget, time, index++, 1.0f, 0.2f, 0.2f);
            }
        }
    }

    private void renderHand(PoseStack poseStack, VertexConsumer buffer, Vec3 pos, Vec3 vForward, float time, int light) {
        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y, pos.z);

        float yaw = (float) Mth.atan2(vForward.x, vForward.z);
        poseStack.mulPose(Axis.YP.rotation(yaw));

        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.15f, 0.5f);
        renderTexturedSphere(poseStack, buffer, 0, 0, 0, 1.0f, 12, 12, 1.0f, 1.0f, 1.0f, 1.0f, WHITE_SPRITE_CACHE, light);
        poseStack.popPose();

        float fingerMovement = Mth.sin(time * 0.15f) * 12.0f;

        for (int i = 0; i < 5; i++) {
            poseStack.pushPose();
            poseStack.translate(FINGER_POSITIONS[i][0], FINGER_POSITIONS[i][1], FINGER_POSITIONS[i][2]);

            float baseRot = (i == 0) ? 45 : 25;
            poseStack.mulPose(Axis.XP.rotationDegrees(baseRot + fingerMovement));

            poseStack.pushPose();
            poseStack.scale(0.07f, 0.09f, 0.22f);
            poseStack.translate(0, 0, 0.5);
            renderTexturedSphere(poseStack, buffer, 0, 0, 0, 1.0f, 8, 8, 1.0f, 1.0f, 1.0f, 1.0f, WHITE_SPRITE_CACHE, light);
            poseStack.popPose();

            poseStack.translate(0, 0, 0.25);
            poseStack.mulPose(Axis.XP.rotationDegrees(20 + fingerMovement * 0.5f));
            poseStack.pushPose();
            poseStack.scale(0.05f, 0.07f, 0.18f);
            poseStack.translate(0, 0, 0.5);
            renderTexturedSphere(poseStack, buffer, 0, 0, 0, 1.0f, 8, 8, 0.1f, 0.1f, 0.1f, 1.0f, BLACK_SPRITE_CACHE, light);
            poseStack.popPose();

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderTexturedSphere(PoseStack poseStack, VertexConsumer buffer, float x, float y, float z, float radius, int lngs, int lats, float r, float g, float b, float a, TextureAtlasSprite sprite, int light) {
        Matrix4f mat = poseStack.last().pose();
        Matrix3f normalMat = poseStack.last().normal();
        for (int i = 0; i < lats; i++) {
            float fL0 = (float) i / lats, fL1 = (float) (i + 1) / lats;
            float aL0 = (float) Math.PI * (-0.5f + fL0), aL1 = (float) Math.PI * (-0.5f + fL1);
            for (int j = 0; j < lngs; j++) {
                float fG0 = (float) j / lngs, fG1 = (float) (j + 1) / lngs;
                float g0 = (float) (2.0 * Math.PI * fG0), g1 = (float) (2.0 * Math.PI * fG1);
                addVertex(buffer, mat, normalMat, x, y, z, radius, g0, Mth.sin(aL0), Mth.cos(aL0), sprite.getU(fG0*16), sprite.getV(fL0*16), r, g, b, a, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, g0, Mth.sin(aL1), Mth.cos(aL1), sprite.getU(fG0*16), sprite.getV(fL1*16), r, g, b, a, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, g1, Mth.sin(aL1), Mth.cos(aL1), sprite.getU(fG1*16), sprite.getV(fL1*16), r, g, b, a, light);
                addVertex(buffer, mat, normalMat, x, y, z, radius, g1, Mth.sin(aL0), Mth.cos(aL0), sprite.getU(fG1*16), sprite.getV(fL0*16), r, g, b, a, light);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addVertex(VertexConsumer b, Matrix4f m, Matrix3f n, float x, float y, float z, float rad, float lng, float sL, float cL, float u, float v, float r, float g, float bl, float a, int light) {
        float nx = Mth.cos(lng) * cL, ny = sL, nz = Mth.sin(lng) * cL;
        b.vertex(m, x + nx * rad, y + ny * rad, z + nz * rad).color(r, g, bl, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, nx, ny, nz).endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderPrismLightning(PoseStack poseStack, VertexConsumer buffer, Vec3 start, Vec3 end, float time, int index, float r, float g, float b) {
        Matrix4f matrix = poseStack.last().pose();
        Vec3 current = start;
        int segments = 8;

        Vec3 diff = end.subtract(start);
        double length = diff.length();
        Vec3 direction = diff.normalize();
        float segmentLength = (float) (length / segments);

        float deviation = 0.15f;
        float thickness = 0.035f;

        rand.setSeed((long) (index * 1000 + (time * 0.4F)));

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

            drawLightningPrism(buffer, matrix, current, next, thickness, r, g, b, 0.8f);
            current = next;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawLightningPrism(VertexConsumer buffer, Matrix4f matrix, Vec3 start, Vec3 end, float thickness, float r, float g, float b, float a) {
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

        addFace(buffer, matrix, s1, s2, e2, e1, r, g, b, a);
        addFace(buffer, matrix, s2, s3, e3, e2, r, g, b, a);
        addFace(buffer, matrix, s3, s4, e4, e3, r, g, b, a);
        addFace(buffer, matrix, s4, s1, e1, e4, r, g, b, a);
    }

    @OnlyIn(Dist.CLIENT)
    private void addFace(VertexConsumer buffer, Matrix4f matrix, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, float r, float g, float b, float a) {
        addVertexWithData(buffer, matrix, v1.x, v1.y, v1.z, 0, 0, r, g, b, a);
        addVertexWithData(buffer, matrix, v2.x, v2.y, v2.z, 1, 0, r, g, b, a);
        addVertexWithData(buffer, matrix, v3.x, v3.y, v3.z, 1, 1, r, g, b, a);
        addVertexWithData(buffer, matrix, v4.x, v4.y, v4.z, 0, 1, r, g, b, a);
    }

    @OnlyIn(Dist.CLIENT)
    private void addVertexWithData(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0, 1, 0)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(AscencionAltar machine) { return machine.isFormed(); }

    @Override
    public int getViewDistance() { return 128; }

    @Override
    public AABB getRenderBoundingBox(AscencionAltar m) { return new AABB(m.getPos()).inflate(16.0D); }

    public static void init() {
    }
}