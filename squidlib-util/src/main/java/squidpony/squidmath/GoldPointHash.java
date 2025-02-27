package squidpony.squidmath;

/**
 * A relatively simple {@link IPointHash} that multiplies each of the x, y, etc. components and the state by a different
 * constant for each (a "harmonious number" related to the golden ratio), sums them, and keeps only the upper bits.
 * This benefits from dot-product optimizations performed by recent JVMs (since at least Java 8). It finishes each hash
 * with an XLCG step to improve the upper bits, then it right-shifts the whole long so it fits in the desired output
 * range (such as 8 bits for {@link #hash256(int, int, int)}), casts it to int and returns that.
 * <br>
 * This hash is slightly faster for some noise applications than {@link IntPointHash}, and has comparable quality, but
 * does require a lot of math on long values, and that can be quite slow on GWT. This should probably be used primarily
 * on desktop targets, and/or maybe mobile.
 */
public class GoldPointHash extends IPointHash.IntImpl {

    public static final GoldPointHash INSTANCE = new GoldPointHash();

    public GoldPointHash() {
        super();
    }

    public GoldPointHash(int state) {
        super(state);
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return hashAll(x, y, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return hashAll(x, y, z, w, u, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return hashAll(x, y, z, w, u, v, state);
    }

    /**
     * A 32-bit-result point hash that uses multiplication with long constants.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y point with the given state s
     */
    public static int hashAll(int x, int y, int s) {
        return (int)((0xD1B54A32D192ED03L * x + 0xABC98388FB8FAC03L * y + -0x8CB92BA72F3D8DD7L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 32);
    }
    /**
     * A 32-bit-result point hash that uses multiplication with long constants.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z point with the given state s
     */
    public static int hashAll(int x, int y, int z, int s) {
        return (int)((0xDB4F0B9175AE2165L * x + 0xBBE0563303A4615FL * y + -0xA0F2EC75A1FE1575L * z + 0x89E182857D9ED689L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 32);
    }

    /**
     * A 32-bit-result point hash that uses multiplication with long constants.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int s) {
        return (int)((0xE19B01AA9D42C633L * x + 0xC6D1D6C8ED0C9631L * y + -0xAF36D01EF7518DBBL * z +
                0x9A69443F36F710E7L * w + -0x881403B9339BD42DL * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 32);
    }

    /**
     * A 32-bit-result point hash that uses multiplication with long constants.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int s) {
        return (int)((0xE60E2B722B53AEEBL * x + 0xCEBD76D9EDB6A8EFL * y + -0xB9C9AA3A51D00B65L * z +
                0xA6F5777F6F88983FL * w + -0x9609C71EB7D03F7BL * u + 0x86D516E50B04AB1BL * s ^
                0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 32);
    }

    /**
     * A 32-bit-result point hash that uses multiplication with long constants.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
        return (int)((0xE95E1DD17D35800DL * x + 0xD4BC74E13F3C782FL * y + -0xC1EDBC5B5C68AC25L * z +
                0xB0C8AC50F0EDEF5DL * w + -0xA127A31C56D1CDB5L * u + 0x92E852C80D153DB3L * v +
                -0x85EB75C3024385C3L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 32);
    }

    /**
     * A 8-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y point with the given state s
     */
    public static int hash256(int x, int y, int s) {
        return (int)((0xD1B54A32D192ED03L * x + 0xABC98388FB8FAC03L * y + -0x8CB92BA72F3D8DD7L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 56);
    }
    /**
     * A 8-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z point with the given state s
     */
    public static int hash256(int x, int y, int z, int s) {
        return (int)((0xDB4F0B9175AE2165L * x + 0xBBE0563303A4615FL * y + -0xA0F2EC75A1FE1575L * z + 0x89E182857D9ED689L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 56);
    }

    /**
     * A 8-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int s) {
        return (int)((0xE19B01AA9D42C633L * x + 0xC6D1D6C8ED0C9631L * y + -0xAF36D01EF7518DBBL * z +
                0x9A69443F36F710E7L * w + -0x881403B9339BD42DL * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 56);
    }

    /**
     * A 8-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int v, int s) {
        return (int)((0xE95E1DD17D35800DL * x + 0xD4BC74E13F3C782FL * y + -0xC1EDBC5B5C68AC25L * z +
                0xB0C8AC50F0EDEF5DL * w + -0xA127A31C56D1CDB5L * u + 0x92E852C80D153DB3L * v -
                0x85EB75C3024385C3L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 56);
    }

    /**
     * A 6-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y point with the given state s
     */
    public static int hash64(int x, int y, int s) {
        return (int)((0xD1B54A32D192ED03L * x + 0xABC98388FB8FAC03L * y + -0x8CB92BA72F3D8DD7L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 58);
    }
    /**
     * A 6-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z point with the given state s
     */
    public static int hash64(int x, int y, int z, int s) {
        return (int)((0xDB4F0B9175AE2165L * x + 0xBBE0563303A4615FL * y + -0xA0F2EC75A1FE1575L * z + 0x89E182857D9ED689L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 58);
    }

    /**
     * A 6-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int s) {
        return (int)((0xE19B01AA9D42C633L * x + 0xC6D1D6C8ED0C9631L * y + -0xAF36D01EF7518DBBL * z +
                0x9A69443F36F710E7L * w + -0x881403B9339BD42DL * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 58);
    }

    /**
     * A 6-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int u, int v, int s) {
        return (int)((0xE95E1DD17D35800DL * x + 0xD4BC74E13F3C782FL * y + -0xC1EDBC5B5C68AC25L * z +
                0xB0C8AC50F0EDEF5DL * w + -0xA127A31C56D1CDB5L * u + 0x92E852C80D153DB3L * v -
                0x85EB75C3024385C3L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 58);
    }
    /**
     * A 5-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y point with the given state s
     */
    public static int hash32(int x, int y, int s) {
        return (int)((0xD1B54A32D192ED03L * x + 0xABC98388FB8FAC03L * y + -0x8CB92BA72F3D8DD7L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 59);
    }
    /**
     * A 5-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z point with the given state s
     */
    public static int hash32(int x, int y, int z, int s) {
        return (int)((0xDB4F0B9175AE2165L * x + 0xBBE0563303A4615FL * y + -0xA0F2EC75A1FE1575L * z + 0x89E182857D9ED689L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 59);
    }

    /**
     * A 5-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int s) {
        return (int)((0xE19B01AA9D42C633L * x + 0xC6D1D6C8ED0C9631L * y + -0xAF36D01EF7518DBBL * z +
                0x9A69443F36F710E7L * w + -0x881403B9339BD42DL * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 59);
    }

    /**
     * A 5-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int u, int v, int s) {
        return (int)((0xE95E1DD17D35800DL * x + 0xD4BC74E13F3C782FL * y + -0xC1EDBC5B5C68AC25L * z +
                0xB0C8AC50F0EDEF5DL * w + -0xA127A31C56D1CDB5L * u + 0x92E852C80D153DB3L * v -
                0x85EB75C3024385C3L * s ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L >>> 59);
    }


}
