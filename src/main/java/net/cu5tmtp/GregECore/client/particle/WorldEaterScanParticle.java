package net.cu5tmtp.GregECore.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/** The vanilla particle packet's velocity fields carry the displacement to the structure center. */
public class WorldEaterScanParticle extends TextureSheetParticle {
    private final Vec3 source;
    private final Vec3 travel;

    private WorldEaterScanParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
        super(level, x, y, z);
        source = new Vec3(x, y, z);
        travel = new Vec3(dx, dy, dz);
        lifetime = 40;
        hasPhysics = false;
        quadSize = 0.2F;
        setColor(0.75F, 0.2F, 1.0F);
    }

    @Override
    public void tick() {
        xo = x; yo = y; zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        Vec3 position = source.add(travel.scale((double) age / lifetime));
        setPos(position.x, position.y, position.z);
        setAlpha(Math.min(1.0F, (lifetime - age) / 10.0F));
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    @Override
    protected int getLightColor(float partialTick) { return 0xF000F0; }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            var particle = new WorldEaterScanParticle(level, x, y, z, dx, dy, dz);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
