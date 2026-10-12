package net.cu5tmtp.GregECore.gametest;

import com.google.gson.Gson;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterReport;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Opt-in native client font and book-screen verification; excluded from production. */
@Mod.EventBusSubscriber(modid = GregECore.MOD_ID, value = Dist.CLIENT)
public class WorldEaterClientChecks {
    private static final List<String> failures = new ArrayList<>();
    private static final List<ItemStack> books = new ArrayList<>();
    private static int frame;
    private static int current = -1;
    private static int pages;
    private static boolean uiStarted;
    private static int waiting;

    @SubscribeEvent
    public static void renderedFrame(TickEvent.RenderTickEvent event) {
        if (Boolean.getBoolean("gregecore.worldEaterClientVerification") && event.phase == TickEvent.Phase.END)
            WorldEaterParticleChecks.renderedFrame();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) throws Exception {
        if (!Boolean.getBoolean("gregecore.worldEaterClientVerification") || event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        boolean nativeWorld = Boolean.getBoolean("gregecore.worldEaterClientWorld");
        if (nativeWorld && (client.level == null || client.player == null)) {
            if (++waiting == 100) {
                System.out.println("WORLD_EATER_WORLD_WAIT screen=" + (client.screen == null ? "none" : client.screen.getClass().getSimpleName()));
                Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), "world-wait.png", client.getMainRenderTarget(), message -> {});
                // Only the explicit opt-in, prefix-guarded disposable world reaches this branch.
                if (client.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen) {
                    for (var child : client.screen.children()) {
                        if (child instanceof net.minecraft.client.gui.components.Button button
                                && button.getMessage().getString().equals("Proceed")) {
                            button.onPress();
                            break;
                        }
                    }
                }
            }
            return;
        }
        if (nativeWorld && current == books.size() && uiStarted) {
            WorldEaterNativeUIChecks.tick();
            if (WorldEaterNativeUIChecks.done) {
                failures.addAll(WorldEaterNativeUIChecks.failures);
                finish(client);
            }
            return;
        }
        if (current == -1) {
            if ((!nativeWorld && client.screen == null) || client.getOverlay() != null || client.font == null
                    || client.getResourceManager().getResource(GregECore.id("models/item/incomplete_world_eater_tamer.json")).isEmpty()) return;
            System.out.println("WORLD_EATER_CLIENT_START");
            books.add(WorldEaterReport.create(Map.of(), true, 0, 0));
            books.add(WorldEaterReport.create(Map.of(GregECore.id("incomplete_world_eater_tamer"), 501L), true, 0, 0));
            books.add(WorldEaterReport.create(Map.of(), false, -2, 3));
            Map<ResourceLocation, Long> longNames = new TreeMap<>();
            for (int i = 0; i < 50; i++) longNames.put(GregECore.id("long_" + i + "_" + "w".repeat(300)), (long) i + 1);
            books.add(WorldEaterReport.create(longNames, true, 0, 0));
            for (int book = 0; book < books.size(); book++) {
                var tags = books.get(book).getTag().getList("pages", Tag.TAG_STRING);
                pages += tags.size();
                for (int page = 0; page < tags.size(); page++) {
                    Component text = Component.Serializer.fromJson(tags.getString(page));
                    int lines = client.font.split(text, 114).size();
                    if (lines > 128 / 9) failures.add("Book " + book + " page " + page + " has " + lines + " lines; native view renders only 14");
                }
            }
            for (String path : new String[]{"models/block/machine/incomplete_world_eater_tamer.json", "models/item/incomplete_world_eater_tamer.json"}) {
                if (client.getResourceManager().getResource(GregECore.id(path)).isEmpty()) failures.add("Missing model " + path);
            }
            current = 0;
            client.setScreen(new BookViewScreen(BookViewScreen.BookAccess.fromItem(books.get(current))));
        }
        frame++;
        Path output = Path.of(System.getenv("TMPDIR"), "world-eater-client");
        Files.createDirectories(output);
        if (frame % 40 == 20) {
            Screenshot.grab(output.toFile(), "book-" + current + ".png", client.getMainRenderTarget(), message -> System.out.println("WORLD_EATER_SCREENSHOT " + message.getString()));
        }
        if (frame % 40 == 0) {
            if (++current < books.size()) {
                client.setScreen(new BookViewScreen(BookViewScreen.BookAccess.fromItem(books.get(current))));
            } else {
                if (nativeWorld) {
                    uiStarted = true;
                    WorldEaterNativeUIChecks.begin();
                } else finish(client);
            }
        }
    }

    private static void finish(Minecraft client) throws Exception {
        Files.writeString(Path.of(System.getenv("TMPDIR"), "world-eater-client", "result.json"),
                new Gson().toJson(Map.of("passed", failures.isEmpty(), "pagesChecked", pages, "nativeUI", uiStarted, "failures", failures)));
        System.out.println("WORLD_EATER_CLIENT_VERIFIED passed=" + failures.isEmpty() + " pages=" + pages + " nativeUI=" + uiStarted);
        client.stop();
    }
}
