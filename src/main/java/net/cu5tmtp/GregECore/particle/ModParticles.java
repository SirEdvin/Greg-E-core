package net.cu5tmtp.GregECore.particle;

import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GregECore.MOD_ID);

    public static final RegistryObject<SimpleParticleType> WORLD_EATER_SCAN =
            PARTICLES.register("world_eater_scan", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) { PARTICLES.register(eventBus); }
}
