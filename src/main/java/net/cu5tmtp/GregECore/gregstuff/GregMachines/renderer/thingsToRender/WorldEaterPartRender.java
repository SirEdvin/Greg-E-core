package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public class WorldEaterPartRender extends DynamicRender<IncompleteWorldEaterTamer, WorldEaterPartRender>
        implements IControllerModelRenderer {
    public static final Codec<WorldEaterPartRender> CODEC = Codec.unit(WorldEaterPartRender::new);
    public static final DynamicRenderType<IncompleteWorldEaterTamer, WorldEaterPartRender> TYPE = new DynamicRenderType<>(CODEC);

    @Override
    public DynamicRenderType<IncompleteWorldEaterTamer, WorldEaterPartRender> getType() { return TYPE; }

    @Override
    public boolean isBlockEntityRenderer() { return true; }

    @Override
    public boolean shouldRender(IncompleteWorldEaterTamer machine, Vec3 cameraPos) {
        return machine.isFormed() && !((WorldEaterTamerRecipeLogic) machine.getRecipeLogic()).getDemand().isEmpty();
    }

    @Override
    public AABB getRenderBoundingBox(IncompleteWorldEaterTamer machine) {
        return new AABB(machine.getStructureCenter(), machine.getStructureCenter()).inflate(2.5);
    }

    @Override
    public void render(IncompleteWorldEaterTamer machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var demand = ((WorldEaterTamerRecipeLogic) machine.getRecipeLogic()).getDemand();
        if (demand.isEmpty()) return;
        var client = Minecraft.getInstance();
        var center = machine.getStructureCenter().subtract(Vec3.atLowerCornerOf(machine.getPos()));
        poseStack.pushPose();
        poseStack.translate(center.x, center.y + 0.25, center.z);
        poseStack.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        client.getItemRenderer().renderStatic(demand, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                packedOverlay, poseStack, buffer, machine.getLevel(), 0);
        poseStack.translate(0, -0.7, 0);
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        var label = net.minecraft.network.chat.Component.literal(demand.getCount() + " x ").append(demand.getHoverName());
        var lines = client.font.split(label, 112);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            client.font.drawInBatch(line, -client.font.width(line) / 2f, i * client.font.lineHeight,
                    0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL,
                    0xB0000000, LightTexture.FULL_BRIGHT);
        }
        poseStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController controller, IMultiPart part,
                                Direction frontFacing, Direction side, RandomSource rand,
                                ModelData modelData, RenderType renderType) {
        var state = controller.getPartAppearance(part, side, null, null);
        if (state == null) return;
        var model = ModelUtils.getModelForState(state);
        if (model == null) return;
        var data = model.getModelData(part.self().getLevel(), part.self().getPos(), state, modelData);
        quads.addAll(model.getQuads(state, side, rand, data, renderType));
    }
}
