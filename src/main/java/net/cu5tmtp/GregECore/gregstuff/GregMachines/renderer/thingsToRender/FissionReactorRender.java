package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.FissionReactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@SuppressWarnings("all")
public class FissionReactorRender extends DynamicRender<FissionReactor, FissionReactorRender> {

    public static final Codec<FissionReactorRender> CODEC = Codec.unit(FissionReactorRender::new);
    public static final DynamicRenderType<FissionReactor, FissionReactorRender> TYPE = new DynamicRenderType<>(FissionReactorRender.CODEC);

    private static TextureAtlasSprite WHITE_SPRITE;
    private static TextureAtlasSprite CONTROL_ROD_SPRITE;
    private static TextureAtlasSprite URANIUM_SPRITE;
    private static TextureAtlasSprite IRON_SPRITE;

    private float heatLevel;

    public FissionReactorRender() {}

    @Override
    public DynamicRenderType<FissionReactor, FissionReactorRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FissionReactor machine, Vec3 cameraPos) {
        return machine.isFormed();
    }

    @Override
    public void render(FissionReactor machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {

        if (WHITE_SPRITE == null) {
            WHITE_SPRITE = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                    .getBlockModel(Blocks.WHITE_CONCRETE.defaultBlockState()).getParticleIcon();
        }

        if (CONTROL_ROD_SPRITE == null) {
            CONTROL_ROD_SPRITE = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                    .getBlockModel(Blocks.STONE.defaultBlockState()).getParticleIcon();
        }

        if (URANIUM_SPRITE == null) {
            URANIUM_SPRITE = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                    .getBlockModel(Blocks.EMERALD_BLOCK.defaultBlockState()).getParticleIcon();
        }

        if (IRON_SPRITE == null) {
            IRON_SPRITE = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                    .getBlockModel(Blocks.IRON_BLOCK.defaultBlockState()).getParticleIcon();
        }

        float insertionLevel = 0f;

        if(machine.getRecipeLogic().isWorking()){
            heatLevel = LightTexture.FULL_BRIGHT;
        } else {
            heatLevel = 0.2F;
        }

        renderRodsAnim(machine, insertionLevel, heatLevel, poseStack, buffer, packedLight);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRodsAnim(FissionReactor machine, float insertionLevel, float heatLevel, PoseStack stack,
                                MultiBufferSource buffer, int light) {
        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();

        Direction back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        Direction up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        Direction right = RelativeDirection.RIGHT.getRelative(front, upwards, flipped);

        float basePosX = (back.getStepX() * 2) + (up.getStepX()) + 0.5F;
        float basePosY = (back.getStepY() * 2) + (up.getStepY());
        float basePosZ = (back.getStepZ() * 2) + (up.getStepZ()) + 0.5F;

        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        Matrix4f pose = stack.last().pose();
        Matrix3f normalMat = stack.last().normal();

        float fuelRodWidth = 0.20f;
        float fuelRodHeight = 5.0f;
        float controlRodWidth = 0.10f;
        float controlRodHeight = 8.0f;

        float[] blockOffsets = {-1.0f, 0.0f, 1.0f};
        float[] rodOffsets = {-0.25f, 0.25f};

        int magicalLight = LightTexture.FULL_BRIGHT;

        int skyLight = (light >> 16) & 0xFFFF;
        int currentBlockLight = light & 0xFFFF;
        int glowBlockLight = Math.max(currentBlockLight, (int)(15.0f * Math.min(1.0f, heatLevel)) << 4);
        int dynamicGlowLight = (skyLight << 16) | glowBlockLight;

        for (float bx : blockOffsets) {
            for (float bz : blockOffsets) {
                float blockCenterX = basePosX + right.getStepX() * bx + back.getStepX() * bz;
                float blockCenterY = basePosY + right.getStepY() * bx + back.getStepY() * bz;
                float blockCenterZ = basePosZ + right.getStepZ() * bx + back.getStepZ() * bz;

                for (float rx : rodOffsets) {
                    for (float rz : rodOffsets) {
                        float rodCenterX = blockCenterX + right.getStepX() * rx + back.getStepX() * rz;
                        float rodCenterY = blockCenterY + right.getStepY() * rx + back.getStepY() * rz;
                        float rodCenterZ = blockCenterZ + right.getStepZ() * rx + back.getStepZ() * rz;

                        renderOrientedBox(builder, pose, normalMat, rodCenterX, rodCenterY, rodCenterZ,
                                fuelRodWidth - 0.02f, fuelRodHeight, fuelRodWidth - 0.02f, 0.0f,
                                right, up, back, 255, 255, 255, 255, dynamicGlowLight, URANIUM_SPRITE, 0);

                        renderCasing(builder, pose, normalMat, rodCenterX, rodCenterY, rodCenterZ,
                                fuelRodWidth, fuelRodHeight, fuelRodWidth, 0.0f,
                                right, up, back, light, IRON_SPRITE);

                        int variant = Math.abs((int)(rodCenterX * 1000 + rodCenterZ * 1000));
                        renderOrientedBox(builder, pose, normalMat, rodCenterX, rodCenterY, rodCenterZ,
                                controlRodWidth, controlRodHeight, controlRodWidth, insertionLevel,
                                right, up, back, 180, 180, 180, 255, magicalLight, CONTROL_ROD_SPRITE, variant);
                    }
                }
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    private void renderCasing(VertexConsumer builder, Matrix4f pose, Matrix3f normalMat,
                              float cx, float cy, float cz,
                              float w, float h, float d, float lyOffset,
                              Direction right, Direction up, Direction back, int light, TextureAtlasSprite sprite) {
        float strutThick = 0.04f;
        float w2 = w / 2.0f;
        float d2 = d / 2.0f;

        float edgeH = h + 0.02f;
        float edgeYOff = lyOffset - 0.01f;

        renderOrientedBox(builder, pose, normalMat, cx + w2, cy, cz + d2, strutThick, edgeH, strutThick, edgeYOff, right, up, back, 255, 255, 255, 255, light, sprite, 0);
        renderOrientedBox(builder, pose, normalMat, cx - w2, cy, cz + d2, strutThick, edgeH, strutThick, edgeYOff, right, up, back, 255, 255, 255, 255, light, sprite, 0);
        renderOrientedBox(builder, pose, normalMat, cx + w2, cy, cz - d2, strutThick, edgeH, strutThick, edgeYOff, right, up, back, 255, 255, 255, 255, light, sprite, 0);
        renderOrientedBox(builder, pose, normalMat, cx - w2, cy, cz - d2, strutThick, edgeH, strutThick, edgeYOff, right, up, back, 255, 255, 255, 255, light, sprite, 0);

        int segments = (int) Math.ceil(h);
        for (int i = 0; i <= segments; i++) {
            float ringY;
            if (i == 0) {
                ringY = lyOffset - 0.001f;
            } else if (i == segments) {
                ringY = lyOffset + h - strutThick + 0.001f;
            } else {
                ringY = lyOffset + (i * (h / segments)) - (strutThick / 2.0f);
            }

            renderOrientedBox(builder, pose, normalMat, cx, cy, cz, w + 0.004f, strutThick, d + 0.004f, ringY, right, up, back, 255, 255, 255, 255, light, sprite, 0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderOrientedBox(VertexConsumer builder, Matrix4f pose, Matrix3f normalMat,
                                   float cx, float cy, float cz,
                                   float w, float h, float d,
                                   float lyOffset,
                                   Direction right, Direction up, Direction back,
                                   int r, int g, int b, int a, int light, TextureAtlasSprite sprite, int variant) {
        float w2 = w / 2.0f;
        float d2 = d / 2.0f;

        boolean flipU = (variant & 1) != 0;
        boolean flipV = (variant & 2) != 0;

        float baseU = flipU ? sprite.getU1() : sprite.getU0();
        float baseV = flipV ? sprite.getV1() : sprite.getV0();

        float du = flipU ? (sprite.getU0() - sprite.getU1()) : (sprite.getU1() - sprite.getU0());
        float dv = flipV ? (sprite.getV0() - sprite.getV1()) : (sprite.getV1() - sprite.getV0());

        float maxU_w = baseU + du * Math.min(1.0f, w);
        float maxU_d = baseU + du * Math.min(1.0f, d);
        float maxV_d = baseV + dv * Math.min(1.0f, d);

        addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                -w2, lyOffset, -d2, baseU, baseV, w2, lyOffset, -d2, maxU_w, baseV, w2, lyOffset, d2, maxU_w, maxV_d, -w2, lyOffset, d2, baseU, maxV_d,
                0, -1, 0, r, g, b, a, light);

        addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                -w2, h + lyOffset, -d2, baseU, baseV, -w2, h + lyOffset, d2, baseU, maxV_d, w2, h + lyOffset, d2, maxU_w, maxV_d, w2, h + lyOffset, -d2, maxU_w, baseV,
                up.getStepX(), up.getStepY(), up.getStepZ(), r, g, b, a, light);

        float currentY = 0;
        while (currentY < h) {
            float segmentH = Math.min(1.0f, h - currentY);
            float startY = lyOffset + currentY;
            float endY = startY + segmentH;

            float maxV_h = baseV + dv * segmentH;

            addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                    w2, startY, -d2, maxU_w, maxV_h, -w2, startY, -d2, baseU, maxV_h, -w2, endY, -d2, baseU, baseV, w2, endY, -d2, maxU_w, baseV,
                    -back.getStepX(), -back.getStepY(), -back.getStepZ(), r, g, b, a, light);

            addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                    -w2, startY, d2, maxU_w, maxV_h, w2, startY, d2, baseU, maxV_h, w2, endY, d2, baseU, baseV, -w2, endY, d2, maxU_w, baseV,
                    back.getStepX(), back.getStepY(), back.getStepZ(), r, g, b, a, light);

            addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                    -w2, startY, -d2, baseU, maxV_h, -w2, startY, d2, maxU_d, maxV_h, -w2, endY, d2, maxU_d, baseV, -w2, endY, -d2, baseU, baseV,
                    -right.getStepX(), -right.getStepY(), -right.getStepZ(), r, g, b, a, light);

            addFace(builder, pose, normalMat, cx, cy, cz, right, up, back,
                    w2, startY, d2, baseU, maxV_h, w2, startY, -d2, maxU_d, maxV_h, w2, endY, -d2, maxU_d, baseV, w2, endY, d2, baseU, baseV,
                    right.getStepX(), right.getStepY(), right.getStepZ(), r, g, b, a, light);

            currentY += segmentH;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addFace(VertexConsumer builder, Matrix4f pose, Matrix3f normalMat,
                         float cx, float cy, float cz,
                         Direction right, Direction up, Direction back,
                         float lx1, float ly1, float lz1, float u1, float v1,
                         float lx2, float ly2, float lz2, float u2, float v2,
                         float lx3, float ly3, float lz3, float u3, float v3,
                         float lx4, float ly4, float lz4, float u4, float v4,
                         float nx, float ny, float nz,
                         int r, int g, int b, int a, int light) {

        addTransformed(builder, pose, normalMat, cx, cy, cz, right, up, back, lx1, ly1, lz1, nx, ny, nz, r, g, b, a, light, u1, v1);
        addTransformed(builder, pose, normalMat, cx, cy, cz, right, up, back, lx2, ly2, lz2, nx, ny, nz, r, g, b, a, light, u2, v2);
        addTransformed(builder, pose, normalMat, cx, cy, cz, right, up, back, lx3, ly3, lz3, nx, ny, nz, r, g, b, a, light, u3, v3);
        addTransformed(builder, pose, normalMat, cx, cy, cz, right, up, back, lx4, ly4, lz4, nx, ny, nz, r, g, b, a, light, u4, v4);
    }

    @OnlyIn(Dist.CLIENT)
    private void addTransformed(VertexConsumer builder, Matrix4f pose, Matrix3f normalMat,
                                float cx, float cy, float cz,
                                Direction right, Direction up, Direction back,
                                float lx, float ly, float lz,
                                float nx, float ny, float nz,
                                int r, int g, int b, int a, int light, float u, float v) {

        float x = cx + right.getStepX() * lx + up.getStepX() * ly + back.getStepX() * lz;
        float y = cy + right.getStepY() * lx + up.getStepY() * ly + back.getStepY() * lz;
        float z = cz + right.getStepZ() * lx + up.getStepZ() * ly + back.getStepZ() * lz;

        builder.vertex(pose, x, y, z).color(r, g, b, a).uv(u, v).uv2(light).normal(normalMat, nx, ny, nz).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(FissionReactor machine) {
        return machine.isFormed();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(FissionReactor machine) {
        return new AABB(machine.getPos()).inflate(12.0D);
    }

    public static void init(){}
}