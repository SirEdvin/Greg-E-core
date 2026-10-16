package net.cu5tmtp.GregECore.gametest;

import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Native rendered-model assertions, not just checks that CTM metadata exists. */
final class WorldEaterCasingConnectionChecks {
    private static final BlockPos ORIGIN = new BlockPos(12288, 80, 12288);

    static BlockPos panel(int color) { return ORIGIN.offset(-8 + color * 5, 1, 0); }
    private static BlockPos cube(int color) { return ORIGIN.offset(20 + color * 4, 0, 0); }

    static void setup(ServerLevel level) {
        Block[] blocks = WorldEaterCasingTests.casings();
        for (int color = 0; color < blocks.length; color++) {
            var state = blocks[color].defaultBlockState();
            BlockPos p = panel(color);
            // Visible filled panel, horizontal and vertical strips, and concave L.
            for (int x = 0; x < 3; x++) for (int y = 0; y < 3; y++)
                level.setBlockAndUpdate(p.offset(x, y, 0), state);
            for (int i = 0; i < 3; i++) {
                level.setBlockAndUpdate(p.offset(i, -1, 3), state);
                level.setBlockAndUpdate(p.offset(0, i - 1, 5), state);
            }
            level.setBlockAndUpdate(p.offset(0, 0, 12), state);
            level.setBlockAndUpdate(p.offset(1, 0, 12), state);
            level.setBlockAndUpdate(p.offset(0, 1, 12), state);
            level.setBlockAndUpdate(p.offset(1, 1, 12), Blocks.AIR.defaultBlockState());
            // A cube provides complete coplanar neighbours for every face normal.
            for (int x = 0; x < 3; x++) for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++)
                level.setBlockAndUpdate(cube(color).offset(x, y, z), state);
        }
        // Unlike variants must retain the boundary between them.
        level.setBlockAndUpdate(ORIGIN.offset(-4, 0, -3), blocks[1].defaultBlockState());
        level.setBlockAndUpdate(ORIGIN.offset(-3, 0, -3), blocks[2].defaultBlockState());
    }

    static List<String> verify(Minecraft client) {
        List<String> errors = new ArrayList<>();
        for (int color = 0; color < 4; color++) {
            BlockPos p = panel(color);
            var center = p.offset(1, 1, 0);
            var state = client.level.getBlockState(center);
            if (!state.is(WorldEaterCasingTests.casings()[color])) {
                errors.add("Missing synchronized casing panel " + color);
                continue;
            }
            var quads = quads(client, center, Direction.NORTH);
            if (quads.size() != 8 || quads.stream().anyMatch(q -> !isCtm(q)))
                errors.add("Filled panel did not use four CTM quadrants per base/emissive layer: " + color);
            if (quads.stream().filter(q -> q.getSprite().contents().name().getPath().contains("emissive_ctm")
                    && q.getVertices()[6] == 0x00f000f0).count() != 4)
                errors.add("Connected highlight lost fullbright emission: " + color);
            for (Direction face : Direction.values()) {
                BlockPos c = cube(color).offset(1, 1, 1);
                var faces = quads(client, c, face);
                if (faces.size() != 8 || faces.stream().anyMatch(q -> !isCtm(q)))
                    errors.add("CTM failed on face " + face + " for " + color);
            }
            var horizontal = Connections.checkConnections(client.level, p.offset(1, -1, 3), state, Direction.NORTH);
            if (!Arrays.equals(horizontal.getSubmapIndices(), new int[]{12, 13, 9, 8}))
                errors.add("Horizontal strip connection mapping differs: " + color);
            var vertical = Connections.checkConnections(client.level, p.offset(0, 0, 5), state, Direction.NORTH);
            if (!Arrays.equals(vertical.getSubmapIndices(), new int[]{6, 7, 3, 2}))
                errors.add("Vertical strip connection mapping differs: " + color);
            var concave = Connections.checkConnections(client.level, p.offset(0, 0, 12), state, Direction.NORTH);
            if (Arrays.stream(concave.getSubmapIndices()).filter(i -> i >= 16).count() != 1
                    || Arrays.stream(concave.getSubmapIndices()).filter(i -> i == 10 || i == 11 || i == 14 || i == 15).count() != 1)
                errors.add("Concave L must retain its missing-diagonal corner: " + color);
            // The lower-left panel corner has two isolated outer quadrants; these
            // must continue sampling the original framed source texture.
            if (quads(client, p, Direction.NORTH).stream().noneMatch(q -> !isCtm(q)))
                errors.add("Exterior panel border disappeared: " + color);
        }
        var left = ORIGIN.offset(-4, 0, -3);
        var right = left.east();
        if (ICTMPredicate.DEFAULT.isConnected(client.level, client.level.getBlockState(left), left,
                client.level.getBlockState(right), right, Direction.NORTH)) errors.add("Different colors connected");
        // Black controller and formed hatches use the native block appearance.
        BlockPos controller = ORIGIN.south(6);
        for (var pos : new BlockPos[]{controller, controller.west(1), controller.west(2), controller.east(2)}) {
            if (quads(client, pos, Direction.NORTH).stream().noneMatch(WorldEaterCasingConnectionChecks::isCtm))
                errors.add("Black machine/formed-part surface did not connect: " + pos);
        }
        return errors;
    }

    private static boolean isCtm(BakedQuad quad) {
        return quad.getSprite().contents().name().getPath().endsWith("_ctm");
    }

    private static List<BakedQuad> quads(Minecraft client, BlockPos pos, Direction face) {
        var state = client.level.getBlockState(pos);
        var model = client.getBlockRenderer().getBlockModel(state);
        var data = model.getModelData(client.level, pos, state, ModelData.EMPTY);
        return model.getQuads(state, face, RandomSource.create(), data, null);
    }
}
