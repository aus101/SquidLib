package squidpony.squidmath;

public class WarbleNoise implements Noise.Noise3D {

    private static final double[] ADJ = {-1.11, 1.41, 1.61};

    public WarbleNoise(){
        this(0x1234567890ABCDEFL);
    }
    public WarbleNoise(long seed) {
        this.seed = seed;
        workSeed[0] = workSeed[3] = DiverRNG.determine(seed);
        workSeed[1] = workSeed[4] = DiverRNG.determine(seed + 1L);
        workSeed[2] = workSeed[5] = DiverRNG.determine(seed + 2L);
    }
    protected long seed;
    /**
     * Where the extra outputs of {@link #getNoise(double, double, double)} and
     * {@link #getNoiseWithSeed(double, double, double, long)} are stored; for 3D noise, items 0 through 2 store valid
     * results, and for higher-dimensional noise up to 6 outputs can be requested.
     */
    public final double[] results = new double[6];
    private final double[] working = new double[18];
    private final long[] workSeed = new long[12];

    @Override
    public double getNoise(double x, double y, double z) {
        working[0] = working[3] = working[6] = x;//+ NumberTools.swayRandomized(workSeed[0], z - y + 0.41) * 0.25;
        working[1] = working[4] = working[7] = y;//+ NumberTools.swayRandomized(workSeed[1], x - z + 0.78) * 0.25;
        working[2] = working[5] = working[8] = z;//+ NumberTools.swayRandomized(workSeed[2], y - x + 0.1) * 0.25;
        warble(3);
        warble(3);
        warble(3);
        for (int i = 0; i < 9; i++) {
            working[i] *= Math.PI;
        }
        for (int i = 0; i < 3; i++) {
            results[i] = sway(i, 0, 3.0);
        }
        return results[0];
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        workSeed[0] = workSeed[3] = DiverRNG.determine(seed);
        workSeed[1] = workSeed[4] = DiverRNG.determine(seed + 1L);
        workSeed[2] = workSeed[5] = DiverRNG.determine(seed + 2L);
        return getNoise(x, y, z);
    }
    private double sway(int element, int workingOffset, double seedChange) {
        return NumberTools.swayRandomized(workSeed[element], working[element + 2 + workingOffset] - seedChange
                + NumberTools.swayRandomized(workSeed[element + 2], working[element + 1 + workingOffset] + seedChange)
                + NumberTools.swayRandomized(workSeed[element + 1], working[element + workingOffset] + seedChange + seedChange));
    }
//    private double sway(int element, int workingOffset, double seedChange) {
//        return NumberTools.sin(workSeed[element] + seedChange + working[element + 2 + workingOffset]
//                - NumberTools.sin(workSeed[element + 2] + seedChange + working[element + 1 + workingOffset])
//                + NumberTools.sin(workSeed[element + 1] + seedChange + working[element + workingOffset]));
//    }
    private void warble(final int size){
        System.arraycopy(working, 0, results, 0, size);
        for (int i = 0; i < size; i++) {
            results[i] += sway(i, 1, 1.1) * 0.3;
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
        for (int i = 0; i < size; i++) {
            results[i] += sway(i, 2, 1.7) * 0.3;
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
        for (int i = 0; i < size; i++) {
            results[i] = (results[i] + sway(i, 0, 2.3)) * 0.3;
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
    }

    public double getPowerNoise(double x, double y, double z) {
        working[0] = working[3] = working[6] = x;
        working[1] = working[4] = working[7] = y;
        working[2] = working[5] = working[8] = z;
        results[0] = z + sway(0, 2, 0.0);
        results[1] = x + sway(1, 2, 0.0);
        results[2] = y + sway(2, 2, 0.0);
        System.arraycopy(results, 0, working, 0, 3);
        System.arraycopy(results, 0, working, 3, 3);
        System.arraycopy(results, 0, working, 6, 3);
        warble(3);
        warble(3);
        warble(3);
        for (int i = 0; i < 9; i++) {
            working[i] *= Math.PI;
        }
        for (int i = 0; i < 3; i++) {
            results[i] = sway(i, 0, 3.0);
        }
        return results[0];
    }
    public double getPowerNoiseWithSeed(double x, double y, double z, long seed) {
        workSeed[0] = workSeed[3] = DiverRNG.determine(seed);
        workSeed[1] = workSeed[4] = DiverRNG.determine(seed + 1L);
        workSeed[2] = workSeed[5] = DiverRNG.determine(seed + 2L);
        return getPowerNoise(x, y, z);
    }

    /*
const float SEED = 42.0;
const vec3 COEFFS = fract((SEED + 23.4567) * vec3(0.8191725133961645, 0.6710436067037893, 0.5497004779019703)) + 0.5;

vec3 swayRandomized(vec3 seed, vec3 value)
{
    return sin(seed.xyz + value.zxy - cos(seed.zxy + value.yzx) + cos(seed.yzx + value.xyz));
}

vec3 cosmic(vec3 c, vec3 con)
{
    con += swayRandomized(c, con.yzx);
    con += swayRandomized(c + 1.0, con.xyz);
    con += swayRandomized(c + 2.0, con.zxy);
    return con * 0.25;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy * 64.0 + swayRandomized(COEFFS.zxy, (iTime * 0.1875) * COEFFS.yzx).xy * 32.0;
    // aTime, s, and c could be uniforms in some engines.
    float aTime = iTime * 0.0625;
    vec3 adj = vec3(-1.11, 1.41, 1.61);
    vec3 s = (swayRandomized(vec3(34.0, 76.0, 59.0), aTime + adj)) * 0.25;
    vec3 c = (swayRandomized(vec3(27.0, 67.0, 45.0), aTime - adj)) * 0.25;
    vec3 con = vec3(0.0004375, 0.0005625, 0.0008125) * aTime + c * uv.x + s * uv.y;

    con = cosmic(COEFFS, con);
    con = cosmic(COEFFS, con);
    con = cosmic(COEFFS, con);

    fragColor = vec4(swayRandomized(COEFFS + 3.0, con * (3.14159265)) * 0.5 + 0.5,1.0);
}
     */
}
