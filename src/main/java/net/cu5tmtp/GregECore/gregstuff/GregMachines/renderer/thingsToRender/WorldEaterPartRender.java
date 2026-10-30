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
    public boolean isBlockEntityRenderer() { return false; }

    @Override
    public boolean shouldRender(IncompleteWorldEaterTamer machine, Vec3 cameraPos) { return false; }

    @Override
    public void render(IncompleteWorldEaterTamer machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

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
