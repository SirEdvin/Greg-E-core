package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

/** Fixed chunk-major order, then local X, local Z, and Y (fastest). */
public record WorldEaterScanDomain(int centerX, int centerZ, int minY, int maxY) {
    public static final int SCAN_TICKS = 2400;
    public static final int WIDTH = 9;

    public WorldEaterScanDomain {
        if (maxY <= minY || minY < -2032 || maxY > 2032
                || Math.abs((long) centerX) > 1874995 || Math.abs((long) centerZ) > 1874995) {
            throw new IllegalArgumentException("Invalid scan domain");
        }
    }

    public long volume() {
        return 81L * 256 * ((long) maxY - minY);
    }

    public long boundary(int tick) {
        if (tick < 0 || tick > SCAN_TICKS) throw new IllegalArgumentException("Invalid scanning tick");
        return volume() * tick / SCAN_TICKS;
    }

    public long batchBudget() {
        return (volume() + SCAN_TICKS - 1) / SCAN_TICKS;
    }

    public int chunkIndex(long index) {
        return (int) (index / (256L * (maxY - minY)));
    }

    public int chunkX(long index) {
        return centerX - 4 + chunkIndex(index) / WIDTH;
    }

    public int chunkZ(long index) {
        return centerZ - 4 + chunkIndex(index) % WIDTH;
    }

    public int x(long index) {
        return chunkX(index) * 16 + (int) (index / (maxY - minY) % 256 / 16);
    }

    public int z(long index) {
        return chunkZ(index) * 16 + (int) (index / (maxY - minY) % 16);
    }

    public int y(long index) {
        return minY + (int) (index % (maxY - minY));
    }
}
