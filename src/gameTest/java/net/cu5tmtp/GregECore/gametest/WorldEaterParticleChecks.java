package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.cu5tmtp.GregECore.client.particle.WorldEaterScanParticle;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Observes real server scan packets without replacing the production particle provider. */
final class WorldEaterParticleChecks {
    private static final ConcurrentLinkedQueue<ClientboundLevelParticlesPacket> packets = new ConcurrentLinkedQueue<>();
    private static final String HANDLER = "world-eater-particle-checks";
    private static BlockPos controllerPos;
    private static BlockPos sourcePos;
    private static int frames;
    private static int receivedAt = -1;
    static boolean done;

    static void begin(BlockPos pos) {
        controllerPos = pos;
        sourcePos = pos.west(6).above(3);
        var client = Minecraft.getInstance();
        client.setScreen(null);
        client.getConnection().getConnection().channel().pipeline().addBefore("packet_handler", HANDLER, new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                if (message instanceof ClientboundLevelParticlesPacket packet && packet.getParticle() == ModParticles.WORLD_EATER_SCAN.get())
                    packets.add(packet);
                super.channelRead(context, message);
            }
        });
        client.getSingleplayerServer().execute(() -> {
            var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
            var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, pos);
            var logic = (WorldEaterTamerRecipeLogic) controller.getRecipeLogic();
            logic.resetRecipeLogic();
            CompoundTag tag = new CompoundTag(), custom = new CompoundTag();
            custom.putInt("Version", 1); custom.putString("State", "DORMANT");
            tag.put("WorldEaterTamer", custom); logic.loadCustomPersistedData(tag);
            int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
            for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) level.getChunk(x, z);
            level.setBlockAndUpdate(sourcePos, GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
            level.setBlockAndUpdate(sourcePos.west(), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].defaultBlockState());
            level.setBlockAndUpdate(sourcePos.east(), Blocks.CHEST.defaultBlockState());
            var input = (ItemBusPartMachine) MetaMachine.getMachine(level, pos.west(2));
            input.getInventory().setStackInSlot(0, GTItems.SENSOR_IV.asStack());
            var player = level.getServer().getPlayerList().getPlayer(client.player.getUUID());
            player.teleportTo(pos.getX() - 2, pos.getY() + 2, pos.getZ() - 8);
            player.setYRot(0); player.setXRot(0);
            logic.setWorkingEnabled(true);
            logic.setupRecipe(WorldEaterRunTests.recipe());
            for (int tick = 0; tick < 2400 && logic.getCounts().getOrDefault(GTMachines.MACERATOR[GTValues.LV].getId(), 0L) == 0; tick++)
                logic.serverTick();
            logic.setWorkingEnabled(false);
            if (logic.getCounts().values().stream().mapToLong(Long::longValue).sum() != 1)
                WorldEaterNativeUIChecks.failures.add("Particle fixture counted something other than the one standalone machine");
            System.out.println("WORLD_EATER_PARTICLE_SCAN count=" + logic.getCounts().values().stream().mapToLong(Long::longValue).sum());
        });
    }

    static void tick() {
        var client = Minecraft.getInstance();
        frames++;
        if (receivedAt == -1 && !packets.isEmpty()) {
            if (packets.size() != 1) WorldEaterNativeUIChecks.failures.add("Expected exactly one scan particle packet, got " + packets.size());
            var packet = packets.peek();
            Vec3 source = new Vec3(packet.getX(), packet.getY(), packet.getZ());
            Vec3 displacement = new Vec3(packet.getXDist(), packet.getYDist(), packet.getZDist());
            Vec3 target = Vec3.atCenterOf(controllerPos.south(2).above(2));
            if (!source.equals(Vec3.atCenterOf(sourcePos)) || !source.add(displacement).equals(target)
                    || packet.getCount() != 0 || packet.getMaxSpeed() != 1)
                WorldEaterNativeUIChecks.failures.add("Scan packet does not travel from found block to geometric structure center");
            var particle = client.particleEngine.createParticle(packet.getParticle(), source.x, source.y, source.z,
                    displacement.x, displacement.y, displacement.z);
            if (!(particle instanceof WorldEaterScanParticle) || !particle.getPos().equals(source)) {
                WorldEaterNativeUIChecks.failures.add("Production particle provider did not create a particle at the found block");
            } else {
                for (int i = 0; i < 20; i++) particle.tick();
                if (particle.getPos().distanceTo(source.add(displacement.scale(0.5))) > 0.00001)
                    WorldEaterNativeUIChecks.failures.add("Particle did not reach midpoint after 20 ticks");
                for (int i = 0; i < 20; i++) particle.tick();
                if (particle.getPos().distanceTo(target) > 0.00001)
                    WorldEaterNativeUIChecks.failures.add("Particle did not reach structure center after 40 ticks");
                particle.tick();
                if (particle.isAlive()) WorldEaterNativeUIChecks.failures.add("Particle did not expire after arrival");
            }
            // Also exercise a full-height, multi-chunk displacement and a source at the center itself.
            for (Vec3 offset : new Vec3[]{new Vec3(128, -300, -128), Vec3.ZERO}) {
                var distant = client.particleEngine.createParticle(ModParticles.WORLD_EATER_SCAN.get(), target.x, target.y, target.z,
                        offset.x, offset.y, offset.z);
                if (distant == null) WorldEaterNativeUIChecks.failures.add("Missing scan particle provider");
                else {
                    for (int i = 0; i < 40; i++) distant.tick();
                    if (distant.getPos().distanceTo(target.add(offset)) > 0.00001)
                        WorldEaterNativeUIChecks.failures.add("Long/zero displacement particle endpoint mismatch");
                    distant.remove();
                }
            }
            receivedAt = frames;
            System.out.println("WORLD_EATER_PARTICLE_VERIFIED packets=" + packets.size() + " source=" + source + " target=" + target);
        }
        if (frames > 1200 && receivedAt == -1) {
            WorldEaterNativeUIChecks.failures.add("No real scan particle packet received");
            client.getConnection().getConnection().channel().pipeline().remove(HANDLER);
            done = true;
        }
    }

    static void renderedFrame() {
        if (receivedAt < 0 || frames - receivedAt < 10 || done) return;
        var client = Minecraft.getInstance();
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), "scan-particle.png",
                client.getMainRenderTarget(), message -> System.out.println("WORLD_EATER_SCREENSHOT " + message.getString()));
        client.getConnection().getConnection().channel().pipeline().remove(HANDLER);
        done = true;
    }
}
