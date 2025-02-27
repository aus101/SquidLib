package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A very fast generator on 64-bit systems that allows choosing any of 2 to the 63 odd-number streams. Each stream
 * changes the set of possible outputs this can produce (amending the main flaw of ThrustAltRNG). This does well in
 * PractRand quality tests, passing 32TB with one minor anomaly (it shares a lot of structure with ThrustAltRNG,
 * which does very well in PractRand's testing as well as TestU01's BigCrush). It also outperforms just about everything
 * in BumbleBench benchmarks, making it arguably the fastest random number generator algorithm here that
 * can produce all long values (it just needs multiple generator objects to do so, all seeded differently).
 * <br>
 * Because this can produce multiple occurrences of any number in its sequence (except 0, which it should always produce
 * once over its period of 2 to the 64), it can be considered as passing the "birthday problem" test; after running
 * <a href="http://www.pcg-random.org/posts/birthday-test.html">this test provided by Melissa E. O'Neill</a> on Tangle,
 * it correctly has 9 repeats compared to an expected 10, using the Skipping adapter to check one out of every 65536
 * outputs for duplicates. A generator that failed that test would have 0 repeats or more than 20, so Tangle passes.
 * ThrustAltRNG probably also passes (or its structure allows it to potentially do so), but LightRNG, LinnormRNG,
 * MizuchiRNG, and even ThrustRNG will fail it by never repeating an output. Truncating the output bits of any of these
 * generators will allow them to pass this test, at the cost of reducing the size of the output to an int instead of a
 * long (less than ideal). Notably, an individual Tangle generator tends to be able to produce about 2/3 of all possible
 * long outputs, with roughly 1/3 of all outputs not possible to produce and another 1/3 produced exactly once. These
 * are approximations and will vary between instances. The test that gave the "roughly 2/3 possible" result gave similar
 * results with 8-bit stateA and stateB and with 16-bit stateA and stateB, though it could change with the 64-bit states
 * Tangle actually uses.
 * <br>
 * The name "Tangle" comes from how the two states of this generator are "tied up in each other," with synchronized
 * periods of 2 to the 64 (stateA) and 2 to the 63 (stateB) that repeat as a whole every 2 to the 64 outputs. Contrary
 * to the name, Tangle isn't slowed down at all by this, but the period length of the generator is less than the maximum
 * possible (which OrbitRNG has, though that one is slowed down).
 * <br>
 * See also {@link OrbitRNG}, which gives up more speed but moves through all 2 to the 64 long values as streams over
 * its full period, which is 2 to the 128 (with one stream) instead of the 2 to the 64 (with 2 to the 63 streams) here.
 * There's also {@link SilkRNG}, which is like OrbitRNG but uses 32-bit math and is GWT-optimized.
 * <br>
 * This added a small extra step to the generation on March 4, 2020; the extra step reduces correlation between streams
 * by further randomizing the output. Without this step (an additional right-xorshift by 6), nearby stateA or stateB
 * values would often produce visibly similar sequences; with the step they are much less similar and more random. The
 * additional step slows down TangleRNG by a very small amount. This edit also added {@link #getStream()}, which may
 * have a use somewhere (maybe diagnosing possible problems?).
 * <br>
 * Created by Tommy Ettinger on 7/9/2018.
 */
public class TangleRNG implements RandomnessSource, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 5L;
    /**
     * Can be any long value.
     */
    private long stateA;

    /**
     * Must be odd.
     */
    private long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public TangleRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public TangleRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25) | 1L;
    }

    public TangleRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB | 1L;
    }

    /**
     * Get the "A" part of the internal state as a long.
     *
     * @return the current internal "A" state of this object.
     */
    public long getStateA() {
        return stateA;
    }

    /**
     * Set the "A" part of the internal state with a long.
     *
     * @param stateA a 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    /**
     * Get the "B" part of the internal state as a long.
     *
     * @return the current internal "B" state of this object.
     */
    public long getStateB() {
        return stateB;
    }

    /**
     * Set the "B" part of the internal state with a long; the least significant bit is ignored (will always be odd).
     *
     * @param stateB a 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB | 1L;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public int next(final int bits) {
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
        return (int)(z ^ z >>> 26 ^ z >>> 6) >>> (32 - bits);
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public long nextLong() {
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
        return z ^ z >>> 26 ^ z >>> 6;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public TangleRNG copy() {
        return new TangleRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "TangleRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TangleRNG tangleRNG = (TangleRNG) o;

        return stateA == tangleRNG.stateA && stateB == tangleRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }

    /**
     * Advances or rolls back the SkippingRandomness' state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to {@link #nextLong()},
     * and returns the random number produced at that step. Negative numbers can be used to step backward, or 0 can be
     * given to get the most-recently-generated long from {@link #nextLong()}.
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public long skip(long advance) {
        final long s = (stateA += 0xC6BC279692B5C323L * advance);
        final long z = (s ^ s >>> 31) * (stateB += 0x9E3779B97F4A7C16L * advance);
        return z ^ z >>> 26 ^ z >>> 6;
    }
    /**
     * Gets a long that identifies which stream of numbers this generator is producing; this stream identifier is always
     * an odd long and won't change by generating numbers. It is determined at construction and will usually (not
     * always) change if {@link #setStateA(long)} or {@link #setStateB(long)} are called. Each stream is a
     * probably-unique sequence of 2 to the 64 longs, where approximately 1/3 of all possible longs will not ever occur
     * (while others occur twice or more), but this set of results is different for every stream. There are 2 to the 63
     * possible streams, one for every odd long.
     * @return an odd long that identifies which stream this TangleRNG is generating from
     */
    public long getStream()
    {
        return stateB - (stateA * 0x1743CE5C6E1B848BL) * 0x9E3779B97F4A7C16L;
    }

}
