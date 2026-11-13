package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson.DysonSwarmLauncher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;

public class DysonSwarmLauncherRender extends DynamicRender<DysonSwarmLauncher, DysonSwarmLauncherRender> {

    public static final Codec<DysonSwarmLauncherRender> CODEC = Codec.unit(DysonSwarmLauncherRender::new);
    public static final DynamicRenderType<DysonSwarmLauncher, DysonSwarmLauncherRender> TYPE = new DynamicRenderType<>(DysonSwarmLauncherRender.CODEC);

    private static final float COLOR_R = 0.53f;
    private static final float COLOR_G = 0.81f;
    private static final float COLOR_B = 0.92f;

    private static final float SPEED = 0.2f;
    private static final float MAX_DISTANCE = 15.0f;
    private static final float START_OFFSET = 5.0f;
    private static final int RING_COUNT = 3;
    private static final float FADE_ZONE = 2.0f;

    private static final float RING_RADIUS = 2.5f;
    private static final float RING_THICKNESS = 0.2f;

    public DysonSwarmLauncherRender() {}

    @Override
    public DynamicRenderType<DysonSwarmLauncher, DysonSwarmLauncherRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(DysonSwarmLauncher machine, Vec3 cameraPos) {
        return machine.getRecipeLogic().isWorking();
    }

    @Override
    public void render(DysonSwarmLauncher machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.getRecipeLogic().isWorking()) {
            return;
        }

        renderLightRings(machine, partialTick, poseStack, buffer);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRings(DysonSwarmLauncher machine, float partialTicks, PoseStack stack,
                                  MultiBufferSource bufferSource) {

        float baseAlpha = 1.0f;

        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();

        var up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        var left = RelativeDirection.LEFT.getRelative(front, upwards, flipped);
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);

        var axis = up.getAxis();

        float time = machine.getOffsetTimer() + partialTicks;

        for (int i = 0; i < RING_COUNT; i++) {
            float currentDist = (time * SPEED + i * (MAX_DISTANCE / RING_COUNT)) % MAX_DISTANCE;

            float ringAlpha = baseAlpha;
            if (currentDist < FADE_ZONE) {
                ringAlpha *= (currentDist / FADE_ZONE);
            } else if (currentDist > MAX_DISTANCE - FADE_ZONE) {
                ringAlpha *= ((MAX_DISTANCE - currentDist) / FADE_ZONE);
            }

            if (ringAlpha <= 0.01f) continue;

            float totalVerticalOffset = START_OFFSET + currentDist;

            float posX = 0.5F + (up.getStepX() * totalVerticalOffset) + (left.getStepX() * 1.0F) + (back.getStepX() * 5.0F);
            float posY = 0.5F + (up.getStepY() * totalVerticalOffset) + (left.getStepY() * 1.0F) + (back.getStepY() * 5.0F);
            float posZ = 0.5F + (up.getStepZ() * totalVerticalOffset) + (left.getStepZ() * 1.0F) + (back.getStepZ() * 5.0F);

            stack.pushPose();
            stack.translate(posX, posY, posZ);

            VertexConsumer ringBuffer = bufferSource.getBuffer(GTRenderTypes.getLightRing());

            RenderBufferHelper.renderRing(stack, ringBuffer,
                    0.0f, 0.0f, 0.0f,
                    RING_RADIUS, RING_THICKNESS, 10, 20,
                    COLOR_R, COLOR_G, COLOR_B, ringAlpha, axis);

            stack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(DysonSwarmLauncher machine) {
        return machine.getRecipeLogic().isWorking();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public AABB getRenderBoundingBox(DysonSwarmLauncher machine) {
        return new AABB(machine.getPos()).inflate(32.0D);
    }

    public static void init() {}
}