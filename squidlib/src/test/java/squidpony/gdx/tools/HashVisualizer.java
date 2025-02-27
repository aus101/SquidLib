package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static squidpony.squidgrid.gui.gdx.SColor.*;
import static squidpony.squidmath.NumberTools.swayTight;

/**
 * Demo to help with visualizing hash/noise functions and RNG types. Most of the hashes work simply by treating the x,y
 * point of a pixel in the window as either one or two ints and running the hash on an array of those ints. This helps
 * find symmetry issues and cases where the range of numbers produced is smaller than it should be. When the term
 * "visual testing" is used in {@link CrossHash}, this class is what it refers to.
 * <br>
 * <b>INSTRUCTIONS</b>
 * <br>
 * Press enter to go to the next test, s once or twice to demo a group of hash functions (hitting s while you're already
 * demoing hashes will change to a different group), n to demo noise functions, r for RNG varieties, and a for artistic
 * interpretations of hashes with limited color palettes. On anything that changes over time, you can hit alt-c to pause
 * or resume (noise, RNG, and artistic demos all allow this), and if paused you can press c to advance one frame.
 * <br>
 * Some points of interest:
 * <li>
 *     <ul>Most of SquidLib's hashes produce what they should, colorful static. If patterns appear in the static, such
 *     as bands of predictable color at specific intervals, that marks a problem.</ul>
 *     <ul>The FNV-1a algorithm, which is what you get if you use CrossHash with no nested class specified, has
 *     significant visual flaws, in addition to being the slowest hash here. With the mapping of ints to colors used in
 *     this class, it generates 3 hues with much greater frequency, and also produces either rhombus or linear patterns,
 *     depending on how the x,y point is encoded into the array that the demo hashes.</ul>
 *     <ul>Java's built-in {@link Arrays#hashCode(int[])} function has abysmal visual quality, mostly looking like the
 *     exact opposite of the colorful static that the hashes should produce.</ul>
 *     <ul>RNG algorithms should mostly produce a lot of frames per second (determined by the efficiency of the
 *     RandomnessSource), without showing repeats in some way (determined by the lowest period of any random bit).</ul>
 *     <ul>Noise functions should have their own individual "flavor" that determines what uses they may be suited for.
 *     While PerlinNoise is a tried-and-true standby for continuous noise, you may instead want a more chaotic/blocky
 *     appearance for the noise, which MerlinNoise accomplishes somewhat. WhirlingNoise is faster variant on PerlinNoise
 *     that tries to avoid certain patterns, particularly in 2D; judge for yourself if it succeeds. There's two colorful
 *     versions of noise here. One samples 3D noise using the current x and y for a point at 3 different z values based
 *     on the number of frames rendered, and uses those 3 numbers as the red, green, and blue channels. Another samples
 *     3D noise only once, and interprets the single value as a 24-bit int representing a color. The first looks good!
 *     The second doesn't look good at all, but may be handy for spotting quirks.</ul>
 *     <ul>There are also "artistic interpretations" of the otherwise-chaotic hashes. Nice for getting ideas, they're a
 *     sort of Rorschach-test-like concept.</ul>
 * </li>
 * <br>
 * Created by Tommy Ettinger on 8/20/2016.
 */
public class HashVisualizer extends ApplicationAdapter {
    // 0 commonly used hashes
    // 1 variants on Mist and other hashes
    // 3 artistic visualizations of hash functions and misc. other
    // 4 noise
    // 5 RNG results
    private int testType = 5;
    private static final int NOISE_LIMIT = 152;
    private static final int RNG_LIMIT = 52;
    private int hashMode, rngMode = 4, noiseMode = 36, otherMode = 17;//142

    /**
     * If you're editing the source of HashVisualizer, you can comment out one line and uncomment another to change
     * how this shows values in the 0-1 range, typically for noise.
     * @param brightness a float that must be between 0.0f and 1.0f, inclusive.
     * @return a packed float color
     */
    public float getGray(float brightness) {
        //// black or white, threshold at 50%
//        return grayscaleF[-(int)(brightness * 1.999) & 255];
        //// colorful; purple is darkest, green middle, red brightest
//        return gradientF[(int)(brightness * 255.999)];
        //// retro green-scale, with the darkest colors in the middle
//        return bumpF[(int)(brightness * 255.999)];
        //// grayscale, dark to light
        return grayscaleF[(int)(brightness * 255.999)];
    }

    private FilterBatch batch;
    
    private TextCellFactory tcf;
    private static final int width = 512, height = 512;
//    private static final int width = 256, height = 256;
    private static final int almost = width - 1, half = width >> 1;
    private static final float[][] back = new float[width][height];

    private SquidInput input;
    
    private Viewport view;
    
    private CrossHash.Yolk yolkA, yolkA_;
    private CrossHash.Mist mist, mistA, mistB, mistC;
    private NLFSR nlfsr = new NLFSR(1);
    private LFSR lfsr = new LFSR(1);
    private VanDerCorputQRNG vdc = new VanDerCorputQRNG(2, 1);
    private IsaacRNG isaac = new IsaacRNG(1L);
    private OrbitRNG orbit = new OrbitRNG(1L);
    private LightRNG light = new LightRNG(1L);
    private LongPeriodRNG longPeriod = new LongPeriodRNG(1L);
    private PermutedRNG permuted = new PermutedRNG(1L);
    private DiverRNG diver = new DiverRNG(1L);
    private XoshiroStarPhi32RNG xosh = new XoshiroStarPhi32RNG(1L);
    private MiniMover64RNG mm64 = new MiniMover64RNG(1L);
    private XoRoRNG xoRo = new XoRoRNG(1L);
    private SilkRNG silk = new SilkRNG(1, 1);
    private BasicRandom64 basic64 = new BasicRandom64(1L);
    private LinnormRNG linnorm = new LinnormRNG(1L);
    private ThrustAltRNG ta = new ThrustAltRNG(1L);
    private Starfish32RNG starfish = new Starfish32RNG(1L);
    
    private TangleRNG tangle = new TangleRNG(1);
    
    private RandomnessSource[][] randomGrid = new RandomnessSource[width][height];
//    private RandomXS128[][] randomGrid = new RandomXS128[width][height];


    private final int[] coordinates = new int[2];
    private final int[] coordinate = new int[1];
    private final double[] doubleCoordinates = new double[2], doubleCoordinate = new double[1];
    private final double[][][][] seamless = new double[3][64][64][64];
    private final SeededNoise seeded = new SeededNoise(0xDEADD00D);
    private final Noise.Layered2D layered2D = new Noise.Layered2D(WhirlingNoise.instance, 3);
    private final Noise.Layered3D layered3D = new Noise.Layered3D(WhirlingNoise.instance, 3);
    private final Noise.Layered4D layered4D = new Noise.Layered4D(WhirlingNoise.instance, 3);
    private final FastNoise fn = new FastNoise(0xDEADBEEF);
    private final Noise.InverseLayered2D invLayered2D = new Noise.InverseLayered2D(WhirlingNoise.instance, 3);
    private final Noise.InverseLayered3D invLayered3D = new Noise.InverseLayered3D(WhirlingNoise.instance, 3);
    private final Noise.InverseLayered4D invLayered4D = new Noise.InverseLayered4D(WhirlingNoise.instance, 3);
    //private final Noise.Noise6D layered6D = new Noise.Layered6D(WhirlingNoise.instance, 3, 1.75);
    private final Noise.Noise2D foam2D_1 = new Noise.Layered2D(FoamNoise.instance, 1, 0x1p-0);
    private final Noise.Noise2D foam2D_2 = new Noise.Layered2D(FoamNoise.instance, 2, 0x1p-0);
    private final Noise.Noise3D foam3D_1 = new Noise.Layered3D(FoamNoise.instance, 1, 0x1p-0);
    private final Noise.Noise3D foam3D_2 = new Noise.Layered3D(FoamNoise.instance, 2, 0x1p-0);
    private final Noise.Noise4D foam4D_1 = new Noise.Layered4D(FoamNoise.instance, 1, 0x1p-0);
    private final Noise.Noise4D foam4D_2 = new Noise.Layered4D(FoamNoise.instance, 2, 0x1p-0);
    private final Noise.Noise6D foam6D_1 = new Noise.Layered6D(FoamNoise.instance, 1, 0x1p-0);
    private final Noise.Noise6D foam6D_2 = new Noise.Layered6D(FoamNoise.instance, 2, 0x1p-0);

    private final WarbleNoise warble3D = new WarbleNoise(0x1337BEEF);

    private final PhantomNoise phantom2D = new PhantomNoise(0x1337BEEF, 2);
    private final PhantomNoise phantom3D = new PhantomNoise(0x1337BEEF, 3);
    private final PhantomNoise phantom4D = new PhantomNoise(0x1337BEEF, 4);
    private final PhantomNoise phantom5D = new PhantomNoise(0x1337BEEF, 5);
    private final PhantomNoise phantom6D = new PhantomNoise(0x1337BEEF, 6);
    private final PhantomNoise phantom6D_A = new PhantomNoise(0xAAAAAAAAAAAAAAAAL, 6);
    private final PhantomNoise phantom6D_B = new PhantomNoise(0xBBBBBBBBBBBBBBBBL, 6);
    private final PhantomNoise phantom6D_C = new PhantomNoise(0xCCCCCCCCCCCCCCCCL, 6);
    private final PhantomNoise phantom7D = new PhantomNoise(0x1337BEEF, 7);
    private final PhantomNoise phantom7D_A = new PhantomNoise(0xAAAAAAAAAAAAAAAAL, 7);
    private final PhantomNoise phantom7D_B = new PhantomNoise(0xBBBBBBBBBBBBBBBBL, 7);
    private final PhantomNoise phantom7D_C = new PhantomNoise(0xCCCCCCCCCCCCCCCCL, 7);
//    private final Noise.Layered2D white2D = new Noise.Layered2D(WhiteNoise.instance, 2);
//    private final Noise.Layered3D white3D = new Noise.Layered3D(WhiteNoise.instance, 2);
//    private final Noise.Layered4D white4D = new Noise.Layered4D(WhiteNoise.instance, 2);
//    private final Noise.Layered6D white6D = new Noise.Layered6D(WhiteNoise.instance, 2);
    private final Noise.Scaled2D scaled2D = new Noise.Scaled2D(seeded, 1.43, 1.43);
    private final Noise.Scaled3D scaled3D = new Noise.Scaled3D(seeded, 1.43, 1.43, 1.43);
    private final Noise.Scaled4D scaled4D = new Noise.Scaled4D(seeded, 1.43, 1.43, 1.43, 1.43);
    private final Noise.Scaled6D scaled6D = new Noise.Scaled6D(seeded, 1.43, 1.43, 1.43, 1.43, 1.43, 1.43);

    private final Noise.Ridged2D ridged2D = new Noise.Ridged2D(SeededNoise.instance, 1, 1.45); // 1.45
    private final Noise.Ridged3D ridged3D = new Noise.Ridged3D(SeededNoise.instance, 1, 1.45); // 1.45
    private final Noise.Ridged4D ridged4D = new Noise.Ridged4D(SeededNoise.instance, 1, 1.45); // 1.45
    private final Noise.Ridged6D ridged6D = new Noise.Ridged6D(SeededNoise.instance, 1, 1.45); // 1.45
                                                        
    private final FastNoise thinFN = new FastNoise(1337, 1, FastNoise.SIMPLEX);
    private final FastNoise thickFN = new FastNoise(31337, 3, FastNoise.SIMPLEX_FRACTAL);
    private final FastNoise layeredFN = new FastNoise(13371337, 1, FastNoise.SIMPLEX_FRACTAL);

//    private final Noise.Warped2D turb2D = new Noise.Warped2D(thinFN, 2);
//    private final Noise.Warped3D turb3D = new Noise.Warped3D(thinFN, 2);
//    private final Noise.Warped4D turb4D = new Noise.Warped4D(thinFN, 2);
//    private final Noise.Warped6D turb6D = new Noise.Warped6D(thinFN, 2);
    private final Noise.Turbulent2D turb2D = new Noise.Turbulent2D(SeededNoise.instance, ridged2D, 3, 2);
    private final Noise.Turbulent3D turb3D = new Noise.Turbulent3D(SeededNoise.instance, ridged3D, 3, 2);
    private final Noise.Turbulent4D turb4D = new Noise.Turbulent4D(SeededNoise.instance, ridged4D, 3, 2);
    private final Noise.Turbulent6D turb6D = new Noise.Turbulent6D(SeededNoise.instance, ridged6D, 3, 2);

    private final Noise.Slick2D slick2D = new Noise.Slick2D(layeredFN, thinFN);
    private final Noise.Slick3D slick3D = new Noise.Slick3D(layeredFN, thinFN);
    private final Noise.Slick4D slick4D = new Noise.Slick4D(layeredFN, thinFN);
    private final Noise.Slick6D slick6D = new Noise.Slick6D(layeredFN, thinFN);

//    private final Noise.Ridged2D slick2D = new Noise.Ridged2D(thinFN, 2, 1.25);
//    private final Noise.Ridged3D slick3D = new Noise.Ridged3D(thinFN, 2, 1.25);
//    private final Noise.Ridged4D slick4D = new Noise.Ridged4D(thinFN, 2, 1.25);
//    private final Noise.Ridged6D slick6D = new Noise.Ridged6D(thinFN, 2, 1.25);

//    private final Noise.Noise2D slick2D = new Noise.Slick2D(SeededNoise.instance, Noise.alternate, 1);
//    private final Noise.Noise3D slick3D = new Noise.Slick3D(SeededNoise.instance, Noise.alternate, 1);
//    private final Noise.Noise4D slick4D = new Noise.Slick4D(SeededNoise.instance, Noise.alternate, 1);
//    private final Noise.Noise6D slick6D = new Noise.Slick6D(SeededNoise.instance, Noise.alternate, 1);

    private final Noise.Scaled2D stretchScaled2D = new Noise.Scaled2D(SeededNoise.instance, 0.035, 0.035);
    private final Noise.Scaled3D stretchScaled3D = new Noise.Scaled3D(SeededNoise.instance, 0.035, 0.035, 0.035);
    private final Noise.Layered2D masonLayered2D = new Noise.Layered2D(MasonNoise.instance, 3, 2.2);
    private final Noise.Layered3D layeredWhirling = new Noise.Layered3D(WhirlingNoise.instance,1);
    private final Noise.Layered3D layeredSeeded = new Noise.Layered3D(SeededNoise.instance,1);
    private final GlitchNoise glitch = GlitchNoise.instance;
    private final MitchellNoise mitchell = new MitchellNoise(0x1337BEEF, 6); 

    private final Noise.Basic1D basic1D = new Noise.Basic1D();
    private final Noise.Layered1D layered1D = new Noise.Layered1D(new Noise.Basic1D(), 30, 0x1p28);

    private final Noise.Sway1D sway1D = new Noise.Sway1D();
    private final Noise.Layered1D layeredSway1D = new Noise.Layered1D(new Noise.Sway1D(123L), 5, 5.0);

    private final Noise.QuilezNoise quilez1D = new Noise.QuilezNoise();
    private final Noise.Layered1D layeredQuilez1D = new Noise.Layered1D(new Noise.QuilezNoise(), 4, 3.0);
    private final Noise.Layered2D layeredQuilez2D = new Noise.Layered2D(new Noise.QuilezNoise(), 2, 4.0);

    private final Noise.Layered2D classic1_2D = new Noise.Layered2D(ClassicNoise.instance, 1, 0.03125f);
    private final Noise.Layered2D classic3_2D = new Noise.Layered2D(ClassicNoise.instance, 3, 0.03125f);
    
    private final FoamNoise foamUsed = new FoamNoise(12345);
    private final FastNoise fastUsed = new FastNoise(12345, 2.0f, FastNoise.FOAM_FRACTAL, 3);
    {
        fastUsed.setFractalType(FastNoise.FBM);
    }
    private final Noise.Seamless2D seamlessFoam = new Noise.Seamless2D(fastUsed, width, height);
//    private final Noise.Seamless2D seamlessFoam = new Noise.Seamless2D(new Noise.Layered4D(foamUsed, 3, 8.0), width, height);

    private final FastNoise fast1_2D = new FastNoise(1337, 0.03125f, FastNoise.SIMPLEX_FRACTAL, 1);
    private final FastNoise fastWhite_2D = new FastNoise(1337, 0.03125f, FastNoise.WHITE_NOISE, 1);
    private final FastNoise fast3_2D = new FastNoise(1337, 0.03125f, FastNoise.SIMPLEX_FRACTAL, 3);

    private final Noise.Ridged2D classic1_lf_2D = new Noise.Ridged2D(ClassicNoise.instance, 1, 0.03125f);
    private final Noise.Ridged2D classic3_lf_2D = new Noise.Ridged2D(ClassicNoise.instance, 3, 0.03125f);
//    private final Noise.Layered2D classic1_lf_2D = new Noise.Layered2D(ClassicNoise.instance, 1, 1.3f);
//    private final Noise.Layered2D classic3_lf_2D = new Noise.Layered2D(ClassicNoise.instance, 3, 1.3f);

    private final FastNoise fast1Ridged2D = new FastNoise(1337, 0.03125f, FastNoise.SIMPLEX_FRACTAL, 1);
    private final FastNoise fast3Ridged2D = new FastNoise(1337, 0.03125f, FastNoise.SIMPLEX_FRACTAL, 3);

    private final FastNoise fastPerlin1 = new FastNoise(1337, 0.03125f, FastNoise.PERLIN_FRACTAL, 1);
    private final FastNoise fastPerlin3 = new FastNoise(1337, 0.03125f, FastNoise.PERLIN_FRACTAL, 3);
    private final FastNoise fastPerlinRidged1 = new FastNoise(1337, 0.03125f, FastNoise.PERLIN_FRACTAL, 1);
    private final FastNoise fastPerlinRidged3 = new FastNoise(1337, 0.03125f, FastNoise.PERLIN_FRACTAL, 3);
    {
        fast1Ridged2D.setFractalType(FastNoise.RIDGED_MULTI);
        fast3Ridged2D.setFractalType(FastNoise.RIDGED_MULTI);
        fastPerlinRidged1.setFractalType(FastNoise.RIDGED_MULTI);
        fastPerlinRidged3.setFractalType(FastNoise.RIDGED_MULTI);
    }
//    {
//        fast2_2D.setFractalType(FastNoise.RIDGED_MULTI);
//        fast3_2D.setFractalType(FastNoise.RIDGED_MULTI);
//        fast2_lf_2D.setFractalType(FastNoise.RIDGED_MULTI);
//        fast3_lf_2D.setFractalType(FastNoise.RIDGED_MULTI);
//    }

    private final Noise.Layered3D classic1_3D = new Noise.Layered3D(ClassicNoise.instance, 1, 0.03125f);
    private final Noise.Layered3D classic3_3D = new Noise.Layered3D(ClassicNoise.instance, 3, 0.03125f);

    private final FastNoise fast1_3D = fast1_2D;
    private final FastNoise fast3_3D = fast3_2D;

    private final Noise.Ridged3D classic1_lf_3D = new Noise.Ridged3D(ClassicNoise.instance, 1, 0.03125f);
    private final Noise.Ridged3D classic3_lf_3D = new Noise.Ridged3D(ClassicNoise.instance, 3, 0.03125f);

    private final FastNoise fast1_lf_3D = fast1Ridged2D;
    private final FastNoise fast3_lf_3D = fast3Ridged2D;
    
    private final FastNoise fast1_4D = fast1_2D;
    private final FastNoise fast3_4D = fast3_2D;

    private final FastNoise fast1_lf_4D = fast1Ridged2D;
    private final FastNoise fast3_lf_4D = fast3Ridged2D;

    private final long
            seedX0 = linnorm.nextLong(), seedX1 = linnorm.nextLong(), seedX2 = linnorm.nextLong(), seedX3 = linnorm.nextLong(),
            seedY0 = linnorm.nextLong(), seedY1 = linnorm.nextLong(), seedY2 = linnorm.nextLong(), seedY3 = linnorm.nextLong(),
            seedZ0 = linnorm.nextLong(), seedZ1 = linnorm.nextLong(), seedZ2 = linnorm.nextLong(), seedZ3 = linnorm.nextLong(),
            seedW0 = linnorm.nextLong(), seedW1 = linnorm.nextLong(), seedW2 = linnorm.nextLong(), seedW3 = linnorm.nextLong(),
            seedU0 = linnorm.nextLong(), seedU1 = linnorm.nextLong(), seedU2 = linnorm.nextLong(), seedU3 = linnorm.nextLong(),
            seedV0 = linnorm.nextLong(), seedV1 = linnorm.nextLong(), seedV2 = linnorm.nextLong(), seedV3 = linnorm.nextLong();

    private final double[] turing = TuringPattern.initialize(width, height);
    private final int[][] turingActivate = TuringPattern.offsetsCircle(width, height, 4),
            turingInhibit = TuringPattern.offsetsCircle(width, height, 8);

    private final double[] connections = {0.0, 0.0, 0.0};
    private final CosmicNumbering cosmos = new CosmicNumbering(connections);
    
    private final float[][][] fillField = new float[3][width][height],
            fillField3DR = new float[1][width][height],
            fillField3DG = new float[1][width][height],
            fillField3DB = new float[1][width][height];

    private RandomnessSource fuzzy;
    private Random jreRandom = new Random(1L);
    private RandomXS128 gdxRandom = new RandomXS128(1L);
    private BasicRandom32 br32 = new BasicRandom32(1);
    private BasicRandom64 br64 = new BasicRandom64(1L);
    private CellularAutomaton ca = new CellularAutomaton(width, height);
    
    private ArrayList<Color> gradient;
    private final float[] gradientF = new float[256], bumpF = new float[256], grayscaleF = new float[256];
    
    private int ctr = -256;
    private boolean keepGoing = true;

    private double total;
    public static double toDouble(long n) {
        return NumberTools.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
        //return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
    }

    public static float toFloat(int n) {
        return (NumberTools.intBitsToFloat(0x3F800000 | n >>> 9) - 1.0f);
    }

    // not much different from Water
    public static int ion32(int[] a)
    {
        if (a == null)
            return 0;
        long result = 0x9E3779B97F4A7C15L, counter = 0x2545F4914F6CDD1DL;
        final int len = a.length;
        for (int i = 0; i < len; i++)
            result ^= (counter += 0x6C8E9CF570932BD5L * a[i]);
        return (int)(counter - (result ^ (result >>> 25)) * (result | 0xA529L));
//        return (int)((result ^ (result >>> 25)) * (result | 0xA529L));
        //return (int)(counter ^ counter >>> 22);
    }
    // not much different from Water
    public static long ion64(int[] a)
    {
        if (a == null)
            return 0;
        long result = 0x9E3779B97F4A7C15L, counter = 0x2545F4914F6CDD1DL;
        final int len = a.length;
        for (int i = 0; i < len; i++)
            result ^= (counter += 0x6C8E9CF570932BD5L * a[i]);
        return counter - (result ^ (result >>> 25)) * (result | 0xA529L);
//        return (result ^ (result >>> 25)) * (result | 0xA529L);
        //return counter ^ counter >>> 22;
    }

    public static int fnv1a_32_bad(final int x, final int y)
    {
        return ((((0x811C9DC5 ^ x) * 0x01000193) ^ y) * 0x01000193);
    }
    public static long fnv1a_64_bad(final long x, final long y)
    {
        return ((((0xCBF29CE484222325L ^ x) * 0x00000100000001B3L) ^ y) * 0x00000100000001B3L);
    }

    public static int fnv1a_32(final int x, final int y)
    {
        return ((((((((((((0x811C9DC5 ^ 
                (x & 0xFF)) * 0x01000193) ^ (x >>> 8 & 0xFF)) * 0x01000193) ^ (x >>> 16 & 0xFF)) * 0x01000193)
                ^ (y & 0xFF)) * 0x01000193)^ (y >>> 8 & 0xFF)) * 0x01000193)^ (y >>> 16 & 0xFF)) * 0x01000193);
    }
    public static long fnv1a_64(final long x, final long y)
    {
        return ((((((((((((0xCBF29CE484222325L ^
                (x & 0xFFL)) * 0x00000100000001B3L) ^ (x >>> 8 & 0xFFL)) * 0x00000100000001B3L) ^ (x >>> 16 & 0xFFL)) * 0x00000100000001B3L)
                ^ (y & 0xFFL)) * 0x00000100000001B3L)^ (y >>> 8 & 0xFFL)) * 0x00000100000001B3L)^ (y >>> 16 & 0xFFL)) * 0x00000100000001B3L);
    }
    public static int ion32(long a, long b) {
        long counter = 0x2545F4914F6CDD1DL + 0x6C8E9CF570932BD5L * a, result = 0x9E3779B97F4A7C15L ^ counter;
        result ^= (counter += 0x6C8E9CF570932BD5L * b);
        return (int)(counter - (result ^ (result >>> 25)) * (result | 0xA529L));
//        counter -= (result ^ (result >>> 25)) * (result | 0xA529L);
//        return (int)(counter ^ counter >>> 22);
    }
    public static int ion32(long a, long b, long c)
    {
        long counter = 0x2545F4914F6CDD1DL + 0x6C8E9CF570932BD5L * a, result = 0x9E3779B97F4A7C15L ^ counter;
        result ^= (counter += 0x6C8E9CF570932BD5L * b);
        result ^= (counter += 0x6C8E9CF570932BD5L * c);
        return (int)(counter - (result ^ (result >>> 25)) * (result | 0xA529L));
//        counter -= (result ^ (result >>> 25)) * (result | 0xA529L);
//        return (int)(counter ^ counter >>> 22);
        //return (int)((result ^ (result >>> 25)) * (result | 0xA529L));
    }
    public static int goldInt(int x, int y) {
//        int s = 0;
//        s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1827F5;// ^ 0x02A6328F;
//        s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
//        s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x123C21;// ^ 0xC79E7B1D;
//        return s ^ s >>> 10 ^ s >>> 15 ^ s << 7; 

//        s = (s + x + y ^ 0xD192ED03) * 0x1A36A9;
//        s ^= s << 13 ^ s >>> 12 ^ s >>> 7;
//        s += (x ^ 0xFB8FAC03) * 0x157931;
//        s ^= s << 13 ^ s >>> 12 ^ s >>> 7;
//        s += (y ^ 0x2F3D8DD7) * 0x119725;
//        return s ^ s << 13 ^ s >>> 12 ^ s >>> 7;

//        int s = 0x9E3779B9;
//        s = (s ^ s >>> 8 ^ s << 15 ^ s << 9 ^ x) * 0x1827F5;
//        s = (s ^ s >>> 8 ^ s << 15 ^ s << 9 ^ y) * 0x123C21;
//        return s ^ s << 8 ^ s >>> 15 ^ s >>> 9;
//        int state = 0x9E3779B9 ^ x * 0x1827F5 ^ y * 0x123C21;
//        return ((state = ((state = (state ^ (state << 19 | state >>> 13) ^ (state << 7 | state >>> 25) ^ 0xD1B54A33) * 0x15DE2D) ^ (state << 20 | state >>> 12) ^ (state << 8 | state >>> 24)) * 0x1B69E5) ^ state >>> 14);

        int s = 0x1827F5 * x ^ y * 0x123C21;
        return (s = ((s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17)) ^ s >>> 15;

        //// known to work
        //int s = 0x9E3779B9 ^ 0x1827F5 * (x ^ y * 0x123C21);
        //return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;

//        return ((s = (s ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
//        return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);

        //        return (s = (s ^ s >>> 11 ^ s >>> 21) * (s | 0xFFE00001) ^ x ^ y) ^ s >>> 13 ^ s >>> 19;
    }
    
    public static long water64(float fx, float fy){
        final long y = NumberTools.floatToIntBits(fy);
        long x = NumberTools.floatToIntBits(fx);
//        x = (x ^ 0xA0761D6478BD642FL) * (y ^ 0x8EBC6AF09C88C6E3L);
        x = (x ^ 0xA0761D65L) * (y ^ 0x8EBC6AF1L);
        x = ((x - (x >>> 32)) ^ 0xEB44ACCBL) * (y ^ 0x589965CDL);
        return ((x - (x >>> 32) + (x << 32)));
//        x = (x ^ 0xA0761D65L) * (y ^ 0x8EBC6AF1L);
//        x = ((x - (x >>> 32)) ^ 0xEB44ACCBL) * (y ^ 0x589965CDL);
//        return ((int)(x - (x >>> 32)));

//        x = (x ^ (y << 47 | y >>> 17) ^ 0xA0761D65L) * (y ^ (x << 23 | x >>> 41) ^ 0x8EBC6AF1L);

//        x += x >>> ((x ^ y) * 0x9E3779B97F4A7C15L >>> 58);
//        return (int)(x ^ x << 5 ^ x >>> 3);

    }
    public static long goldEdit(long x, long y)
    {
        x += (y + 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L + 0x9E3779B97F4A7C15L;
        y = x * 0x8CB92BA72F3D8DD7L;
        return y ^ y >>> 32;

//        x += y * 0xC13FA9A902A6328FL;
//        y += x * 0x91E10DA5C79E7B1CL;
//        return ((y = (y ^ y >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ y >>> 25);

//        long counter = 0x2545F4914F6CDD1DL + 0x6C8E9CF570932BD5L * a, result = 0x9E3779B97F4A7C15L ^ counter;
//        result ^= (counter += 0x6C8E9CF570932BD5L * b);
//        return counter - (result ^ (result >>> 25)) * (result | 0xA529L);

//        counter -= (result ^ (result >>> 25)) * (result | 0xA529L);
//        return counter ^ counter >>> 22;
    }

    public static long ion64(long x, long y, long s) {
        // inverse golden ratio, generalized to 3D (see PhantomNoise for all constants calculated so far)
        y += s * 0xD1B54A32D192ED03L;
        x += y * 0xABC98388FB8FAC03L;
        s += x * 0x8CB92BA72F3D8DD7L;
        // xorshift, XOR with an inverse golden ratio constant, multiply by Neely's Number (a prime), xorshift, return
        // this is like LightRNG but simpler
        return ((s = (s ^ s >> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        // SplitMix64, as used in LightRNG but without the large golden-ratio multiply at the start
//        return ((c = ((c = (c ^ c >>> 30) * 0xBF58476D1CE4E5B9L) ^ c >>> 27) * 0x94D049BB133111EBL) ^ c >>> 31);
        //(c = (c * 0x632BE59BD9B4E019L ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L)
        //c = a * 0xD1B54A32D192ED03L + b * 0xABC98388FB8FAC03L + c * 0x8CB92BA72F3D8DD7L;
//        return (c = ((c = (c ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ c >>> 27) * 0xAEF17502108EF2D9L) ^ c >>> 25;
//        return ((c = (c ^ c >>> 27 ^ 0xDB4F0B9175AE2165L) * 0xC6BC279692B5CC83L) ^ (c << 11 | c  >>> 53) ^ (c << 23 | c >>> 41));
//        long counter = 0x2545F4914F6CDD1DL + 0x6C8E9CF570932BD5L * a, result = 0x9E3779B97F4A7C15L ^ counter;
//        result ^= (counter += 0x6C8E9CF570932BD5L * b);
//        result ^= (counter += 0x6C8E9CF570932BD5L * c);
//        return counter - (result ^ (result >>> 25)) * (result | 0xA529L);

//        counter -= (result ^ (result >>> 25)) * (result | 0xA529L);
//        return counter ^ counter >>> 22;
        //return (result ^ (result >>> 25)) * (result | 0xA529L);
    }

    public static int mixHash(final int x, final int y)
    {
        int x2 = 0x9E3779B9 * x, y2 = 0x632BE5AB * y;
        return ((x2 ^ y2) >>> ((x2 & 7) + (y2 & 7))) * 0x85157AF5;
    }

    public static int oldHash(final int x, final int y)
    {
        int hash = 7;
        hash = 113 * hash + x;
        hash = 113 * hash + y;
        return hash;
    }
    private static final float[] BOLD = new float[100];
    static {
        for (int idx = 0; idx < 100; idx++) {
            BOLD[idx] = BLUE_VIOLET_SERIES[LinnormRNG.determineBounded(idx, BLUE_VIOLET_SERIES.length)].toFloatBits();
        }
    }
//{
////            FLOAT_BLACK,
//            CW_PURPLE.toFloatBits(),
//            CW_BLUE.toFloatBits(),
//            CW_CYAN.toFloatBits(),
//            CW_GREEN.toFloatBits(),
//            CW_YELLOW.toFloatBits(),
//            CW_RED.toFloatBits(),
////            FLOAT_WHITE
//    };

    public static long nitroHash(long seed, long x, long y)
    {
        return ((x = ((x = x * 0xC6BC279692B5CC85L + seed) ^ x >>> 26) * ((y *= 0x9E3779B97F4A7C15L) ^ (y + 0x9E3779B97F4A7C15L))) ^ x >>> 28);
    }

    public static int boundedNitroHash(long seed, long index, long x, long y, int bound)
    {
        return (int)((bound * (((x = ((x = x * 0x6C8E9CF570932BD5L + seed + 0x2545F4914F6CDD1DL * index) ^ x >>> 26) * ((y *= 0x9E3779B97F4A7C15L) ^ (y + 0xC6BC279692B5CC85L))) ^ x >>> 28) & 0xFFFFFFFFL)) >> 32);
    }
    public static int szudzikHash2D(int x, int y) {
        //return (y += ((x = (x ^ 0x41C64E6D) * ((y << 3 & 0xFFFF8) ^ 0x9E373)) ^ x >>> 15 ^ 0x9E3779B5) * 0xACEDB) ^ y >>> 13;
//        x *= 0x9E375;
//        y *= 0xACEDB;
//        x *= 0x3FFF;
//        y *= 0x3FFF;
//        return (x += y ^ ((x *= 0xB531A935) ^ x >>> 13) * (y * 0x41C64E6D | 1)) ^ (x << 18 | x >>> 14) ^ (x << 9 | x >>> 23);         
        //0x9E3779B97F4A7C15L
//        y *= 0x9E3779B97F4A7C15L;
//        x ^= (y << 23 | y >>> 41);
        // szudzik, rather slow, but near-optimal for positive ints
//        return (x >= y ? x * x + x + y : x + y * y);
        // szudzik with modifications to output; a little better hash quality
//        return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 11 | x >>> 21) ^ (x << 20 | x >>> 12)) * 0x13C6EF;

//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        s = 42 ^ s * 0x1827F5 ^ y * 0x123C21;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;


//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        x += ((x+y) * (x+y+1) >> 1);
//        y += x ^ x >>> 1 ^ x >>> 6;
//        return (y ^ (y << 15 | y >>> 17) ^ (y << 23 | y >>> 9)) * 0x125493 ^ 0xD1B54A35;
        
        
//        y = (int)((x * 0xC13FA9A902A6328FL + y * 0x91E10DA5C79E7B1DL) >>> 32);
//        return (y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3));

        
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        y += ((x+y) * (x+y+1) >> 1);
        y ^= y >>> 2 ^ y >>> 5 ^ y >>> 6 ^ y >>> 11;
        y = ((y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35) * 0x125493;
        return y ^ y >>> 1;

//        y + (y >= x ? y * y + y - x : x * x);
        
//        return (y ^ y >>> 1) * 0xACEDB ^ 0x9E3779BD;
        
//        //what Coord uses as of December 13, 2019
//        x ^= x >> 31;
//        y ^= y >> 31;
//        y += ((x+y) * (x+y+1) >> 1);
//        y ^= y >>> 1 ^ y >>> 6;
//        return (y ^ (y << 15 | y >>> 17) ^ (y << 23 | y >>> 9)) * 0x125493 ^ 0xD1B54A35;

        
        
//        y = 
//                (x >= y
//                ? x * x + x + y
//                : x + y * y);

//        y = (y ^ (y << 15 | y >>> 17) ^ (y << 23 | y >>> 9)) * 0x125493 ^ 0xD1B54A35;
//        return  (y ^ (y << 13 | y >>> 19) ^ (y << 25 | y >>> 7)) * 0xACEDB ^ 0xC13FA9AD;

//        y += x ^ (x << 15 | x >>> 17) ^ (x << 29 | x >>> 3);
//        x += y ^ (y << 15 | y >>> 17) ^ (y << 29 | y >>> 3);
//        x ^= x >> 31;
//        y ^= y >> 31;
//        x += ((x+y) * (x+y+1) >> 1);
////                (x >= y
////                ? x * x + x + y
////                : x + y * y);
//        return (x ^ (x << 13 | x >>> 19) ^ (x << 21 | x >>> 11)) * 0x125493 ^ 0xC13FA9AD;
//        return ((x ^ (x << 13 | x >>> 19) ^ (x << 22 | x >>> 12) ^ (x << 6 | x >>> 26) ^ (x << 29 | x >>> 3) ^ 0xC13FA9AD) * 0x125493 ^ 0x91E10DA5) * 0xACEDB ^ 0xD1B54A35;

//        x ^= x >> 31;
//        y ^= y >> 31;
//        x ^= x >>> 1;
//        y ^= y >>> 1;
//        x = ((x >= y
//                ? x * x + x + y
//                : x + y * y) ^ 0xC13FA9AD) * 0x125493;
//        x = (x ^ x >>> 7 ^ x >>> 23 ^ x >>> 18 ^ x << 16 ^ 0x91E10DA5) * 0xACEDB;
//        return x ^ x >>> 16;


//        x ^= x >> 31;
//        y ^= y >> 31;
//        x = ((x >= y
//                ? x * x + x + y
//                : x + y * y) ^ 0xC13FA9AD) * 0x125493;
//        x ^= x >>> (x >>> 28) + 4 ^ 0x91E10DA5;
//        x *= 0x125493;
//        return x ^ x >>> (x >>> 28) + 4;


        // * 0xA5CB3;
        // cantor
//        x += (((x+y) * (x+y+1) >> 1) + y) * 0xC13FA9A902A6328FL;
//        return ((x = (x ^ x >>> 25) * 0x9E3779B97F4A7C15L) ^ x >>> 22);

//        return ((x = ((x *= 0xC6BC279692B5CC85L) ^ x >>> 26) * ((y *= 0x9E3779B97F4A7C15L) ^ (y + 0x9E3779B97F4A7C15L))) ^ x >>> 28);
//        long state = (x << 16 ^ y) + 0xBEEFL;
//        long state = (y*0x41C64E6DL + x*0x9E3779B5L) + 0xBEEFL;
//        long state = (y*130207L ^ x*3053L) + 0xBEEFL;
//        return (int)((100 * (((state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22)) & 0xFFFFFFFFL)) >> 32);
////         works very well, GWT-safe
//        y ^= (x ^ 0xB531A935) * 0xC6D3;
//        x ^= (y ^ 0x41C64E6D) * 0xBCFB;         
//        y ^= x * 0xACEDB;
//        x ^= y * 0x9E375;
//        y -= (x << 19 | x >>> 13);
//        x -= (y << 14 | y >>> 18);
//        return x ^ y;

//        x += x >>> 21;
//        y += y >>> 22;
//        x += x << 8;
//        y += y << 5;

//        int z = x ^ y;
//        z = (z ^ z >>> 13 ^ 0x9E3779B5) * 0x7FFFF ^ x;
//        z = (z ^ z >>> 12) * 0x1FFFF ^ y;
//        z = (z ^ z >>> 14) * 0x1FFF ^ x;
//        z ^= z >>> 11 ^ y;
//        z = (z ^ z >>> 13 ^ 0x9E3779B5) * 0x7FFFF ^ y;
//        z = (z ^ z >>> 12) * 0x1FFFF ^ x;
//        z = (z ^ z >>> 14) * 0x1FFF ^ y;
//        z ^= z >>> 11 ^ x;
//        return z;


//        x += x >>> 21;
//        y += y >>> 22;
//        x += x << 8;
//        y += y << 5;

//        y ^= (x << 17 | x >>> 15);
//        x ^= (y << 13 | y >>> 19);
//        y ^= x * 0x89A7; // + 0xB531A935;
//        x ^= y * 0xBCFD; // + 0x41C64E6D;
//        y ^= (x << 17 | x >>> 15);
//        x ^= (y << 13 | y >>> 19);
//        return x ^ y;
//        y += x * 0x41C64E6D;
//        return (y << 18 | y >>> 14) - x;
        //return (x - y << 13) - (y << 7 | y >>> 25) ^ (y - x << 11) - (x << 5 | x >>> 27);

//        y ^= x * 0xBCFD;
//        x ^= y * 0x89A7;
//        return (y << 13 | y >>> 19) ^ (x << 17 | x >>> 15);

//        x -= (x << 14) - 0xB531A935;
//        y -= (y << 14) - 0x41C64E6D;
//        x += (y << 7) - (x >>> 8);//x += (y << 20 | y >>> 12);// + 0xB531A935;
//        y -= (x << 8) + (y >>> 7);//y -= (x << 5 | x >>> 27);// + 0x41C64E6D;
//        x += (y << 21 | y >>> 11) ^ (y << 6 | y >>> 26) ^ y;
//        y += (x << 13 | x >>> 19) ^ (x << 22 | x >>> 10) ^ x;

//        return (y << 9 | y >>> 23) ^ (x << 25 | x >>> 7);

//        return (x = ((x = x * 0xFACED + y) ^ x >>> 13) * ((y * 0x9E375 - x >> 12 | 1))) ^ (x << 21 | x >>> 11) ^ (x << 12 | x >>> 20);
//        return (int)(TangleRNG.determine(x, y));
//        y ^= 0x9E3779B5;
//        x ^= (y/* ^ 0xB531A935 */) * 0x9E373;
//        y ^= (x/* ^ 0x41C64E6D */) * 0xACEDB;
//        x ^= (y/* ^ 0xB531A935 */) * 0x9E373;
//        y ^= (x/* ^ 0x41C64E6D */) * 0xACEDB;
//        return (x ^ (y << 21 | y >>> 11));
//        x = (x << 13 | x >>> 19) ^ y ^ (y << 5); // a, b
//        y = (y << 28 | y >>> 4) + x; // c
//        return (y << 21 | y >>> 11) ^ x;
    }

    public static int coordHash(final int x, final int y)
    {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << 25 | r >>> 7);
    }

    public static int rosenbergStrongCoord(int x, int y)
    {
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        x += ((x >= y ? x * x + x + x - y : y * y + x) ^ 0xD1B54A35) * 0x9E375 + y;
        return x ^ x >>> 11 ^ x << 15;
    }
    public static int rosenbergStrongCoord2(int x, int y)
    {
        //// for Coord, since it can be as low as -3, and Rosenberg-Strong works only for positive integers
        x += 3;
        y += 3;
        //// Rosenberg-Strong pairing function; has excellent traits for keeping the hash gap-less while the
        //// inputs fit inside a square, and is still good for rectangles.
        int n = (x >= y ? x * (x + 2) - y : y * y + x);
        //// Gray code, XLCG, XLCG (ending on a XOR to stay within int range on GWT).
        //// The Gray code moves bits around just a little, but keeps the same power-of-two upper bound.
        //// the XLCGs together only really randomize the upper bits; they don't change the lower bit at all.
        //// (recall from RNG class that an XLCG is a XOR by a constant, then a multiply by a constant, where
        //// the XOR constant, mod 8, is 5, while the multiplier, mod 8, is 3.)
        //// ending on a XOR helps mostly for GWT.
        return ((n ^ n >>> 1) * 0x9E373 ^ 0xD1B54A35) * 0x125493 ^ 0x91E10DA5;
        //// Other options:
        ////boustrophedonic variant; winds in a serpentine, always-connected path
        //// see https://hbfs.wordpress.com/2018/08/07/moeud-deux/ for more
//        int n;
//        if(x >= y) {
//            if((x & 1) == 1)
//                n = x * x + y;
//            else
//                n = x * (x + 2) - y;
//        }
//        else {
//            if((y & 1) == 1)
//                n = y * (y + 2) - x;
//            else
//                n = y * y + x;
//        }

        //// Just the Rosenberg-Strong result
//        return n;
        
        //// Bijective RRLL shift, XLCG, XLCG, xor (to stay within int range on GWT)
//        return ((n ^ n >>> 11 ^ n >>> 23 ^ n << 7 ^ n << 23 ^ 0xD1B54A35) * 0x9E373 ^ 0x7F4A7C15) * 0x125493 ^ 0x91E10DA5;
        
        //// Bijective combination of bitwise shifts (only some such combinations work non-destructively) 
//        return n ^ n << 7 ^ n << 23 ^ n >>> 11 ^ n >>> 23;
    }


    /**
     * Gets the fractional component of a float. For compatibility with GLSL, which has this built in.
     * @param a any float other than NaN or an infinite value
     * @return the fractional component of a, between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float fract(final float a)
    {
        return a - (a >= 0f ? (int) a : (int) a - 1);
    }

    /**
     * Hash of 1 float input (this will tolerate most floats) with a seed between 0.0 (inclusive) and 1.0 (exclusive),
     * producing a float between 0.0 (inclusive) and 1.0 (exclusive). Designed to port well to GLSL; see
     * <a href="https://www.shadertoy.com/view/Mljczw">this ShaderToy demo</a>. This function is used to implement the
     * other overloads of floatHash().
     * @param x almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param seed a float between 0.0 (inclusive) and 1.0 (exclusive); others may reduce quality
     * @return a hash-code-like float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float floatHash(float x, float seed)
    {
        x *= 15.718281828459045f;
        x = (x + 0.5718281828459045f + seed) * ((seed + (x % 0.141592653589793f)) * 27.61803398875f + 4.718281828459045f);
        return x - (x >= 0f ? (int) x : (int) x - 1);
    }
    /**
     * Hash of 2 float inputs (this will tolerate most floats) with a seed between 0.0 (inclusive) and 1.0 (exclusive),
     * producing a float between 0.0 (inclusive) and 1.0 (exclusive). Designed to port well to GLSL; see
     * <a href="https://www.shadertoy.com/view/Mljczw">this ShaderToy demo</a>.
     * @param x almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param y almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param seed a float between 0.0 (inclusive) and 1.0 (exclusive); others may reduce quality
     * @return a hash-code-like float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float floatHash(float x, float y, float seed)
    {
        x = floatHash(x, seed);
        return floatHash(y, x);
        //x = fract((x + seed) * ((seed + (x % 0.141592653589793f)) * 23.61803398875f + 5.718281828459045f));
        //return fract((y + x) * ((x + (y % 0.141592653589793f)) * 23.61803398875f + 5.718281828459045f));
    }

    /**
     * Hash of 3 float inputs (this will tolerate most floats) with a seed between 0.0 (inclusive) and 1.0 (exclusive),
     * producing a float between 0.0 (inclusive) and 1.0 (exclusive). Designed to port well to GLSL; see
     * <a href="https://www.shadertoy.com/view/Mljczw">this ShaderToy demo</a>.
     * @param x almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param y almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param z almost any float; this will tolerate floats without a fractional component if they aren't very large
     * @param seed a float between 0.0 (inclusive) and 1.0 (exclusive); others may reduce quality
     * @return a hash-code-like float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float floatHash(float x, float y, float z, float seed)
    {
        // This code is roughly equivalent to the following GLSL code, which may be available at the ShaderToy link:
/*
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Adjust this for how you can access x,y positions.
	vec2 uv = (fragCoord.xy / iResolution.xy) * 15.718281828459045;
    // Randomness/hash "salt" is seeded here in the first three elements.
    // Seeds should be between 0 and 1, upper exclusive.
    vec3 seeds = vec3(0.123, 0.456, 0.789);
    seeds = fract((uv.x + 0.5718281828459045 + seeds) * ((seeds + mod(uv.x, 0.141592653589793)) * 27.61803398875 + 4.718281828459045));
    seeds = fract((uv.y + 0.5718281828459045 + seeds) * ((seeds + mod(uv.y, 0.141592653589793)) * 27.61803398875 + 4.718281828459045));
    // You can use some other time-like counter if you want to change the hash over time
    seeds = fract((iTime + 0.5718281828459045 + seeds) * ((seeds + mod(iTime, 0.141592653589793)) * 27.61803398875 + 4.718281828459045));
    fragColor = vec4(seeds, 1.0);
}
*/
        x = floatHash(x, seed);
        y = floatHash(y, x);
        return floatHash(z, y);
    }

    /**
     * Hash of an array of float inputs (most inputs are acceptable) with a seed between 0.0 (inclusive) and 1.0
     * (exclusive), producing a float between 0.0 (inclusive) and 1.0 (exclusive). Designed to port well to GLSL; see
     * <a href="https://www.shadertoy.com/view/Mljczw">this ShaderToy demo</a>.
     * @param inputs a non-null (and ideally non-empty) float array with input coordinates
     * @param seed a float between 0.0 (inclusive) and 1.0 (exclusive); others may reduce quality
     * @return a hash-code-like float between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static float floatHash(float[] inputs, float seed)
    {
        for (int i = 0; i < inputs.length; i++) {
            seed = floatHash(inputs[i], seed);
        }
        return seed;
    }
    //    public static class MicroRandom implements RandomnessSource {
//        public long state0, state1, inc = 0x9E3779B97F4A7C15L, mul = 0x632AE59B69B3C209L;
//
//        public MicroRandom()
//        {
//            this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
//                    ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
//        }
//
//        public MicroRandom(final long seed) {
//            state0 = seed * 0x62E2AC0DL + 0x85157AF5;
//            state1 = seed * 0x85157AF5L - 0x62E2AC0DL;
//        }
//
//        public void setState(final long seed)
//        {
//            state0 = seed * 0x62E2AC0DL + 0x85157AF5;
//            state1 = seed * 0x85157AF5L - 0x62E2AC0DL;
//        }
//
//        @Override
//        public final long nextLong() {
//            //good one
//            //return (state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
//            return (state1 += ((state0 += inc) >> 24) * mul);
//        }
//
//        @Override
//        public final int next(final int bits) {
//            return (int) (nextLong() >>> (64 - bits));
//        }
//
//        @Override
//        public MicroRandom copy() {
//            MicroRandom mr = new MicroRandom(1L);
//            mr.state0 = state0;
//            mr.state1 = state1;
//            mr.mul = mul;
//            mr.inc = inc;
//            return mr;
//        }
//    }

        /*
        public static float determine(float alpha, float beta)
        {
            //final int a = ((x * 0x92B5CC83) << 16) ^ x, b = (((y * 0xD9B4E019) << 16) ^ y) | 1;

            //final int x = (~alpha << 15) * ~beta + ((alpha >> 1) << 1) + ((alpha >> 2) << 2) + (beta >> ((alpha + ~beta) & 3)),
            //        y = (alpha + ~beta << 17) + ((beta >> 1) << 1) + ((beta >> 2) << 2) + (alpha >> (beta & 3));

            final float x = (alpha + 1.4051f) * (beta + 0.9759f),
                    y = (beta + 2.3757f) * (alpha + 0.7153f) + x * 0.7255f;

            //final float x = (alpha + 1.875371971f) * (beta + 0.875716533f),
            //        y = (beta + 3.875716533f) * (alpha + 0.6298371981f);

                    //a = (((x >> 1) * y + x) ^ (~((x >> 1) * y + x) << 15)) + y,
                    //b = ((y + (y >> 1) * a) ^ (~(y + (y >> 1) * a) << 14)) + x;
            //return toFloat(x ^ (0xCC83 * ((x + y & (y + 0xCD7FE75E)) >> 6)));
            //return toFloat(x * y);
            return ((x % 0.29f) + (y % 0.3f) + alpha * 11.138421537629f % 0.22f + beta * 9.3751649568f % 0.21f); // & 8388607
        }
    */
    public static int rawNoise0(int alpha, int beta) {
        // int x = a * 0x1B + b * 0xB9, y = (a * 0x6F ^ b * 0x53), z = x * 0x2D + y * 0xE5, w = (z ^ x) + y * 0xF1,
        // x = a * 0x1B + b * 0x29, y = (a * 0x2F ^ b * 0x13), z = x * 0x3D + y * 0x45, w = (z ^ x) + y * 0x37,
        // near = (x * 0xB9 ^ y * 0x1B) + (x * 0x57 ^ z * 0x6F) + (y * 0x57 ^ z * 0xB9 ) + (x * 0x2D ^ w * 0xE5) + (y  * 0xA7 ^ w * 0xF1);
        // x = a * 11 + b * 10, y = (a * 13 ^ b * 14), z = x * 4 + y * 5, w = (z * 8 ^ x * 9) + y * 7,
        // out = (x ^ y) + (x ^ z) + (y ^ z) + (x ^ w) + (y ^ w);
        final int a = alpha + ((alpha >> 1) << 2) + (beta >> 1),// + ((alpha >> 2) << 4) + (beta >> 2),
                b = beta + ((beta >> 1) << 2) + (alpha >> 1),// + ((beta >> 2) << 4) + (alpha >> 2),
                a2 = a * 31 ^ a - b, b2 = b * 29 ^ b - a,
                x = a2 + b2, y = (a2 ^ b2), z = x + y, w = (z ^ x) + y,
                out = (x + y + z + w) ^ (a2 + b) * b2 ^ (b2 + a) * a2;
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }

    public static int discreteNoise(int x, int y) {
        //int n = rawNoise(x, y), t = n << 4;
        return ((rawNoise(x, y) << 2) +
                rawNoise(x + 1, y) + rawNoise(x - 1, y) + rawNoise(x, y + 1) + rawNoise(x, y - 1)
        ) >> 3;
    }

    public static float discreteNoise(int x, int y, float zoom) {
        //int n = rawNoise(x, y), t = n << 4;
        final float alef = x / zoom, bet = y / zoom;
        final int alpha = (int) (alef), beta = (int) (bet),
                north = rawNoise(alpha, beta - 1), south = rawNoise(alpha, beta + 1),
                east = rawNoise(alpha + 1, beta), west = rawNoise(alpha - 1, beta),
                center = rawNoise(alpha, beta);
        final float aBias = (alef - alpha), bBias = (bet - beta);
        return (((aBias - 0.75f) < 0 ? west : center) * 0.6f + ((aBias + 0.75f) >= 1 ? east : center) * 0.6f +
                ((aBias - 0.25f) < 0 ? west : center) * 0.4f + ((aBias + 0.2f) >= 1 ? east : center) * 0.4f +
                ((bBias - 0.75f) < 0 ? north : center) * 0.6f + ((bBias + 0.75f) >= 1 ? south : center) * 0.6f +
                ((bBias - 0.25f) < 0 ? north : center) * 0.4f + ((bBias + 0.25f) >= 1 ? south : center) * 0.4f
        ) * 0.0009765625f;
    }
        /*
        ((aBias - 0.75f) < 1 ? west : center) + ((aBias + 0.75f) >= 2 ? east : center) +
                ((aBias - 0.25f) < 1 ? west : center) + ((aBias + 0.25f) >= 2 ? east : center) +
                ((bBias - 0.75f) < 1 ? north : center) + ((bBias + 0.75f) >= 2 ? south : center) +
                ((bBias - 0.25f) < 1 ? north : center) + ((bBias + 0.25f) >= 2 ? south : center)
         */

        //midBias = (2f - Math.abs(1f - aBias) - Math.abs(1f - bBias)), //(rawNoise(alpha, beta) << 2) +
        /*
                rawNoise(alpha + 1, beta) * aBias + rawNoise(alpha - 1, beta) * (1 - aBias) +
                rawNoise(alpha, beta + 1) * bBias + rawNoise(alpha, beta - 1) * (1 - bBias) +

                rawNoise(alpha + 1, beta+1) * aBias * bBias + rawNoise(alpha - 1, beta-1) * (1 - aBias) * (1 - bBias) +
                rawNoise(alpha-1, beta + 1) * (1 - aBias) * bBias + rawNoise(alpha+1, beta - 1) * aBias * (1 - bBias)/* +
                 + rawNoise(x + 1, y+1) + rawNoise(x - 1, y-1) + rawNoise(x-1, y + 1) + rawNoise(x+1, y - 1)*/
                 /* >> 1) +
                rawNoise(x + 2, y) + rawNoise(x - 2, y) + rawNoise(x, y + 2) + rawNoise(x, y - 2) +
                rawNoise(x + 2, y+2) + rawNoise(x - 2, y-2) + rawNoise(x-2, y + 2) + rawNoise(x+2, y - 2) +
                rawNoise(x + 2, y+1) + rawNoise(x - 2, y+1) + rawNoise(x+1, y + 2) + rawNoise(x+1, y - 2) +
                rawNoise(x + 2, y-1) + rawNoise(x - 2, y-1) + rawNoise(x-1, y + 2) + rawNoise(x-1, y - 2)*/
        //0.00078125f;//;//0.000244140625f;//0.001953125f; //0.0009765625f; // 0.00048828125f;


    public static int rawNoise(int x, int y) {
        //final int mx = x * 17 ^ ((x ^ 11) + (y ^ 13)), my = y * 29 ^ (7 + x + y),
        final int mx = (x * 0x9E37 ^ y * 0x7C15) + (y * 0xA47F + x * 0x79B9), my = (y * 0xA47F ^ x * 0x79B9) ^ (x * 0x9E37 + y * 0x7C15),
                //gx = mx ^ (mx >> 1), gy = my ^ (my >> 1),
                out = ((mx + my + (mx * my)) >>> 4 & 0x1ff); //((Integer.bitCount(gx) + Integer.bitCount(gy) & 63) << 3) ^
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }

    public static double trigNoise(final double x, final double y, final int seed)
    {
        return NumberTools.bounce(Math.sin(x * 3 - y + seed) + Math.sin(y * 3 - x + seed) + Math.cos(x + y + seed));
    }
    public static double trigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double
                x = (NumberTools.randomFloat(seed - 0x9E3779B9) * 0.5 + 0.75) * ox,
                y = (NumberTools.randomFloat(seed + 0x632BE5AB) * 0.5 + 0.75) * oy,
                z = (NumberTools.randomFloat(seed + 0x9E3779B9) * 0.5 + 0.75) * oz,
                sx = (Math.sin(x) + 1) * 0.5, ix = 1.0 - sx,
                sy = (Math.sin(y) + 1) * 0.5, iy = 1.0 - sy,
                sz = (Math.sin(z) + 1) * 0.5, iz = 1.0 - sz,
                a0 = Math.cos(Math.sin(x * sz + y * iz + z) + Math.sin(y * sz + x * iz - z) + Math.sin((x - y) * 0.5)),
                a1 = Math.cos(Math.sin(x * sy + z * iy + y) + Math.sin(z * sy + x * iy - y) + Math.sin((z - x) * 0.5)),
                a2 = Math.cos(Math.sin(z * sx + y * ix + x) + Math.sin(y * sx + z * ix - x) + Math.sin((y - z) * 0.5));
        return (Math.sin((a0 + a1 + a2) * 7.1));
    }

    public static double veryPatternedTrigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double r = NumberTools.randomFloat(seed) + 1.75,
                x = r * ox,
                y = r * oy,
                z = r * oz;
        return NumberTools.bounce(5
                + (Math.sin(x) * Math.sin(y))
                + (Math.sin(y) * Math.sin(z))
                + (Math.sin(z) * Math.sin(x))
                + Math.cos(x + y + z + seed));
    }
    public static double wavetrigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double r = Math.sin(NumberTools.randomDouble(seed - 0x9E3779B9) * ox
                + NumberTools.randomDouble(seed) * oy
                + NumberTools.randomDouble(seed + 0x9E3779B9) * oz) * 0.2,
                x = (r + 0.5) * ox + oy + r * oz,
                y = (r + 0.55) * oy + oz + r * ox,
                z = (r + 0.6) * oz + ox + r * oy,
                sx = Math.sin(x * r * 3.1 * z + y * r * 2.3 + z * r),
                sy = Math.sin(y * r * 3.1 * x + z * r * 2.3 + x * r),
                sz = Math.sin(z * r * 3.1 * y + x * r * 2.3 + y * r);
        return (Math.sin((sx + sy + sz) * 7.1));
    }
    public static double paternedTrigNoise(final double x, final double y, final double z, final int seed)
    {
        //0x9E3779B9  0x632BE5AB
        final double r = NumberTools.randomFloat(seed) + 1.67,
                //ra = NumberTools.randomSignedFloat(seed - 0x9E3779B9) + 0.85,
                //rb = NumberTools.randomSignedFloat(seed + 0x9E3779B9) + 0.85,
                //rc = NumberTools.randomSignedFloat(seed + 0x632BE5AB) + 0.85,
                sx = Math.sin(x), sy = Math.sin(y), sz = Math.sin(z),
                a0 = Math.cos((y * 3 + z) * r) * (sx + r) * 0.03,
                a1 = Math.cos((z * 3 + x) * r) * (sy + r) * 0.03,
                a2 = Math.cos((x - y) * 2 * r) * (sz + r) * 0.03;
        return //NumberTools.bounce(5 +
                Math.sin((Math.cos(sx * 2 + sy) + Math.cos(sy * 2 + sz) + Math.cos(sz * 2 + sx)
                        + Math.sin(Math.sin(x * a0) * (a1 * 3 - a2) * 5 + Math.sin(y * a1) * (a2 * 3 - a0) * 3 - Math.sin(z * a2) * (a0 * 3 - a1) * 2)
                        + Math.sin(Math.sin(y * a0) * (a1 * 3 - a2) * 5 + Math.sin(z * a1) * (a2 * 3 - a0) * 3 - Math.sin(x * a2) * (a0 * 3 - a1) * 2)
                        + Math.sin(Math.sin(z * a0) * (a1 * 3 - a2) * 5 + Math.sin(x * a1) * (a2 * 3 - a0) * 3 - Math.sin(y * a2) * (a0 * 3 - a1) * 2)) * 8.0
                        //+ Math.atan2(Math.sin(y * rr + x * r), Math.cos(r * y - rr * z))
                        //+ Math.atan2(Math.sin(z * rr + y * r), Math.cos(r * z - rr * x))
                        /*+ Math.sin(y * rr + x * r - z)
                        + Math.sin(z * rr + y * r - x)*/
                );
    }
    public static double bendyTrigNoise(final double x, final double y, final double z, final int seed)
    {
        final double t0 = Math.cos(x + z - y) * 1.6, r0 = Math.cos(t0 + x - z) + 2.9,
                t1 = Math.cos(y + x - z) * 1.75, r1 = Math.cos(t1 + y - x) + 2.6,
                t2 = Math.cos(z + y - x) * 1.9, r2 = Math.cos(t2 + z - y) + 2.3;
        return //NumberTools.bounce(5 +
                Math.sin(
                        //Math.cos(x + y + z)
                        + Math.sin(x * r0 + z * r1 - y * r2 + seed)
                                + Math.sin(y * r0 + x * r1 - z * r2 + seed)
                                + Math.sin(z * r0 + y * r1 - x * r2 + seed)
                );
    }

    public static double swayRandomized(final long seed, final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), sm = s << ((s >>> 52 & 0x7FFL) - 0x400L),
                flip = -((sm & 0x8000000000000L)>>51), floor = Noise.longFloor(value) + seed, sb = (s >> 63) ^ flip;
        double a = (Double.longBitsToDouble(((sm ^ flip) & 0xfffffffffffffL)
                | 0x4000000000000000L) - 2.0);
        final double start = NumberTools.randomSignedDouble(floor), end = NumberTools.randomSignedDouble(floor + 1L);
        a = a * a * (3.0 - 2.0 * a) * (sb | 1L) - sb;
        return (1.0 - a) * start + a * end;
    }
    /**
     * A mix of the smooth transitions of {@link NumberTools#sway(float)} with (seeded) random peaks and valleys between
     * 0f and 1f (both exclusive). The pattern this will produces will be completely different if the seed changes, and
     * it is suitable for 1D noise. Uses a simple method of cubic interpolation between random values, where a random
     * value is used without modification when given an integer for {@code value}. Note that this uses a different type
     * of interpolation than {@link NumberTools#sway(float)}, which uses quintic (this causes swayRandomizedTight() to
     * produce more outputs in the mid-range and less at extremes; it is also slightly faster and simpler).
     * <br>
     * Performance note: HotSpot seems to be much more able to optimize swayRandomized(long, float) than
     * swayRandomized(long, double), with the float version almost twice as fast after JIT warms up. On GWT, the
     * reverse should be expected because floats must be emulated there.
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 1.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float swayRandomizedTight(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        final float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >> 25) * (seed * 0x369DEA0F31A53F85L >>> 39))) * 0x0.ffffffp-63f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >> 25) * (seed * 0x369DEA0F31A53F85L >>> 39))) * 0x0.ffffffp-63f;
//        System.out.printf("start %f, end %f, seed %016X\n", start, end, seed);
//        final float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) & 0xffffffL) * 0x1p-24f,
//                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) & 0xffffffL) * 0x1p-24f;
        value -= floor;
        value *= value * (3f - 2f * value);
        return (1f - value) * start + value * end;
    }
    public static float ectoNoise(float xin, float yin, final long seed)
    {
        return NumberTools.swayRandomized(seed ^ 0x9E3779B97F4A7C15L,
                ((
                        NumberTools.swayRandomized(seed, yin + NumberTools.cos(xin)) -
                        NumberTools.swayRandomized(~seed, xin - NumberTools.sin(yin))
//                        NumberTools.swayRandomized(seed, yin + 0.3f * NumberTools.swayRandomized(seed ^ 0x9E3779B97F4A7C15L, 1.3f * xin)),
//                        NumberTools.swayRandomized(~seed, xin - 0.35f * NumberTools.swayRandomized(seed ^ 0x6C8E9CF570932BD5L, 1.2f * yin))
                ) + 2.5f) * (3.456789f + NumberTools.swayRandomized(seed ^ 0x6C8E9CF570932BD5L, xin - yin)));// + (yin + xin)
    }

    public static double weavingNoise(double x, double y, int seed){
        double sx = NumberTools.swayRandomized(seed++, x);
        double sy = NumberTools.swayRandomized(seed++, y);
        return NumberTools.swayRandomized(seed++ ^ 0x9E3779B9, 4.0 * NumberTools.sin_(
               (NumberTools.swayRandomized(~seed++, x + sy)// + y * 0.5
              + NumberTools.swayRandomized(~seed,   y + sx)// - x * 0.5
              ) * 0.25));
    }

    public static double weavingNoise(double x, double y, double z, int seed){
        double sx = NumberTools.swayRandomized(seed++, x);
        double sy = NumberTools.swayRandomized(seed++, y);
        double sz = NumberTools.swayRandomized(seed++, z);
        return NumberTools.swayRandomized(seed++ ^ 0x9E3779B9, 4.0 * NumberTools.sin_(
               (NumberTools.swayRandomized(~seed++, x + sy)
              + NumberTools.swayRandomized(~seed++, y + sz)
              + NumberTools.swayRandomized(~seed,   z + sx)
              ) * (0.16666666666666666)));
    }
//    public static float beachNoise(int seed, float xin, float yin)
//    {
//        final float a = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin) * 0.5f, yin);
//        seed = (seed ^ 0x9E3779BD) * 0xDAB;
//        seed ^= seed >>> 14;
//        final float b = valueNoise(seed, yin + NumberTools.swayRandomized(~seed, xin - a) * 0.5f, xin + a);
//        final float result = (a + b) * 0.5f;
//        return result * result * (3f - 2f * result);
//    }
    
    public static float valueNoiseSway(int seed, float xin, float yin)
    {
//        float x = xin + NumberTools.swayRandomized(seed, yin) * 0.625f;
        float x = xin;
        float y = yin;
        int xfloor = x >= 0f ? (int) x : (int) x - 1;
        x -= xfloor;
        x *= x * (3 - 2 * x);
        int yfloor = y >= 0f ? (int) y : (int) y - 1;
        y -= yfloor;
        y *= y * (3 - 2 * y);
        int stream = seed - yfloor * 0x9E3;
        int s = (stream + xfloor);
        float start = (((s = (s ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f,
                end = (((s = (stream + xfloor + 1 ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
        float alpha = (1 - x) * start + x * end;
        stream -= 0x9E3;
        s = stream + xfloor;
        start = (((s = (s ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
        end = (((s = (stream + xfloor + 1 ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
        float omega = (1 - x) * start + x * end;
        final float result0 = (1 - y) * alpha + y * omega;
        
        return result0 * 0.5f + 0.5f;

//        seed = (seed ^ 0x9E3779BD) * 0xDAB;
//        seed ^= seed >>> 14;
//        x = xin + 1.618f;
//        y = yin + NumberTools.swayRandomized(seed, xin) * 0.625f - 1.618f;
//        xfloor = x >= 0f ? (int) x : (int) x - 1;
//        x -= xfloor;
//        x *= x * (3 - 2 * x);
//        yfloor = y >= 0f ? (int) y : (int) y - 1;
//        y -= yfloor;
//        y *= y * (3 - 2 * y);
//        stream = seed - xfloor * 0xAB3;
//        s = (stream + yfloor);
//        start = (((s = (s ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
//        end = (((s = (stream + yfloor + 1 ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
//        alpha = (1 - y) * start + y * end;
//        stream -= 0xAB3;
//        s = stream + yfloor;
//        start = (((s = (s ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
//        end = (((s = (stream + yfloor + 1 ^ 0xD1B54A35) * 0x102473) ^ (s << 11 | s >>> 21) ^ (s << 19 | s >>> 13)) * ((s ^ s >>> 15) | 0xFFE00001) ^ s) * 0x0.ffffffp-31f;
//        omega = (1 - y) * start + y * end;
//        final float result1 = (1 - x) * alpha + x * omega;

//        float result = (result1) * 0.5f + 0.5f;

//        float result = (result0 + result1) * 0.25f + 0.5f;
//        return result * result * (3 - 2 * result);
        
//        final float angle = NumberTools.swayAngleRandomized(seed, xin + 0.2f) - NumberTools.swayAngleRandomized(~seed, yin - 0.2f);
//        return (NumberTools.swayRandomized(0x9E3779B97F4A7C15L - seed, (xin * NumberTools.cos(angle) + yin * NumberTools.sin(angle)) * 0.125f));
        
        
        
        
                //+ NumberTools.swayRandomized(seed + 0xD0E89D2D311E289FL, yin * NumberTools.sin(angle))) * 0.5f;
//        return NumberTools.swayRandomized(seed, (NumberTools.swayRandomized(seed + 0x9E3779B97F4A7C15L, xin + NumberTools.swayRandomized(0xD0E89D2D311E289FL + seed, yin + xin * 0.375f)) + NumberTools.swayRandomized(seed - 0x9E3779B97F4A7C15L, yin + NumberTools.swayRandomized(seed - 0xD0E89D2D311E289FL, xin + yin * 0.375f))) * 2.25f);

        
//        final long floorX = Noise.longFloor(xin), floorY = Noise.longFloor(yin);
//        final float
//                xSway = Noise.cerp(NumberTools.swayRandomized(seed + floorY, xin), NumberTools.swayRandomized(seed + floorY + 1, xin), yin - floorY) * 1.875f,
//                ySway = Noise.cerp(NumberTools.swayRandomized(floorX - seed, yin), NumberTools.swayRandomized(floorX + 1 - seed, yin), xin - floorX) * 1.875f;
//        return (NumberTools.swayRandomized(seed ^ 0x9E3779B97F4A7C15L, ySway + xSway + xin + yin) +
//                NumberTools.swayRandomized(seed + 0x9E3779B97F4A7C15L, xin - yin - ySway - xSway + 0.375f)) * 0.5f;

//        final float
//                x = xin + NumberTools.swayRandomized(seed + 9999L, yin * 0.75f) * 0.375f,
//                y = yin + NumberTools.swayRandomized(seed + 999999L, xin * 0.75f) * 0.375f;
//        final long floorx = Noise.longFloor(x), floory = Noise.longFloor(y);
//        final float x0y0 = (hashAll(floorx, floory, seed)) * 0x0.ffffffbp-63f,
//                x1y0 = (hashAll(floorx + 1L, floory, seed)) * 0x0.ffffffbp-63f,
//                x0y1 = (hashAll(floorx, floory + 1, seed)) * 0x0.ffffffbp-63f,
//                x1y1 = (hashAll(floorx + 1L, floory + 1L, seed)) * 0x0.ffffffbp-63f;
//        float ax = x - floorx, ay = y - floory;
//        ax *= ax * (3f - 2f * ax);
//        ay *= ay * (3f - 2f * ay);
////        ax *= ax * ax * (ax * (ax * 6.0 - 15.0) + 10.0);
////        ay *= ay * ay * (ay * (ay * 6.0 - 15.0) + 10.0);
//        return ((1f - ay) * ((1f - ax) * x0y0 + ax * x1y0) + ay * ((1f - ax) * x0y1 + ax * x1y1));
    }

    public static float foamNoise(int seed, final float x, final float y) {
        float xin = x * 0.540302f + y * 0.841471f; // sin and cos of 1
        float yin = x * -0.841471f + y * 0.540302f;
        final float a = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin) * 0.5f, yin);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * -0.989992f + y * 0.141120f; // sin and cos of 3
        yin = x * -0.141120f + y * -0.989992f;
        final float b = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin - a) * 0.5f, yin + a);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * 0.283662f + y * -0.958924f; // sin and cos of 5
        yin = x * 0.958924f + y * 0.283662f;
        final float c = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin + b) * 0.5f, yin - b);
        final float result = (a + b) * 0.3125f + c * 0.375f;
        return result * result * (3f - 2f * result);
    }

    public static float valueNoise(int seed, float x, float y)
    {
        int xFloor = x >= 0f ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = y >= 0f ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * (3 - 2 * y);
        xFloor *= 0xD1B55;
        yFloor *= 0xABC99;
        return ((1f - y) * ((1f - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, seed))
                + y * ((1f - x) * hashPart1024(xFloor, yFloor + 0xABC99, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, seed))) * 0x1.010102p-10f;
    }

    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        s ^= s << 8;
        return s >>> 10 & 0x3FF;
    }

    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    //z should be premultiplied by 0x8CB93
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        s ^= s << 8;
        return s >>> 10 & 0x3FF;
    }

    public static float baseSway(int seed, float value)
    {
        //int fast floor
        final int floor = value >= 0f ? (int) value : (int) value - 1;
        //basic XLCG adjustment to the seed; makes small-scale wavering stronger
        seed = seed * 0x9E37B ^ 0xD1B54A35; 
        //get start and end for interpolation, each from -1 to 1. These are either "peaks" or "valleys"
        //uses another XLCG step (backwards), then gets the low 20 bits, multiplies to get them into 0-2 range, etc.
        final float start = (((seed += floor) ^ 0xD0E89D2D) * 0x1D2473 & 0xFFFFF) * 0x0.FFFFFp-19f - 1f,
                end = ((seed + 1 ^ 0xD0E89D2D) * 0x1D2473 & 0xFFFFF) * 0x0.FFFFFp-19f - 1f;
        //similar to GLSL's fract()
        value -= floor;
        //cubic interpolation
        value *= value * (3 - 2 * value);
        //interpolate between start and end, using cubic to make it curve smoothly
        return (1 - value) * start + value * end;
    }

    /**
     * Returns smooth 1D noise between -1 and 1.
     */
    public static float riverSway(int seed, float value)
    {
        return riverSway(seed, seed ^ 0x9E3779B9, seed ^ 0x7F4A7C15, seed ^ 0x6C8E9CF5, value);
    }

    public static float riverSway(int seedA, int seedB, int seedC, int seedD, float value)
    {
        final float a = baseSway(seedA, value);
        //each previous result is added to a scaled-down version of value
        final float b = baseSway(seedB, value * 0.75f + a);
        final float c = baseSway(seedC, value * 0.5f + b);
        final float d = baseSway(seedD, value * 0.25f + c);
        //the baseSway results are added with different priorities, like octaves of continuous noise (which this is)
        return a * 0.375f + b * 0.3125f + c * 0.1875f + d * 0.125f;
    }

    public static float randomWobbleTight(float value, float alter1, float alter2, float alter3, float alter4)
    {
        return (swayTight(1.125f + value * alter1) +
                swayTight(3.25f + value *  alter2) +
                swayTight(5.375f + value * alter3) +
                swayTight(7.5f + value *   alter4)) * 0.25f;
    }
    public static float randomWobble(float value, float alter1, float alter2, float alter3, float alter4)
    {
        return (NumberTools.sway(alter2 + value * alter1) +
                NumberTools.sway(alter3 + value * alter2) +
                NumberTools.sway(alter4 + value * alter3) +
                NumberTools.sway(alter1 + value * alter4)) * 0.25f;
    }



//        final long
//                sx = Double.doubleToLongBits(x + (x < 0.0 ? -2.0 : 2.0)), mx = sx << ((sx >>> 52 & 0x7FFL) - 0x400L),
//                flipx = -((mx & 0x8000000000000L)>>51), sbx = (sx >> 63) ^ flipx,
//                sy = Double.doubleToLongBits(y + (y < 0.0 ? -2.0 : 2.0)), my = sy << ((sy >>> 52 & 0x7FFL) - 0x400L),
//                flipy = -((my & 0x8000000000000L)>>51), sby = (sy >> 63) ^ flipy;
//        double ax = (Double.longBitsToDouble(((mx ^ flipx) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0),
//        ay = (Double.longBitsToDouble(((my ^ flipy) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        ax = ax * ax * (3.0 - 2.0 * ax) * (sbx | 1L) - sbx;
//        ay = ay * ay * (3.0 - 2.0 * ay) * (sby | 1L) - sby;


    public static int prepareSeed(final int seed)
    {
        return ((seed >>> 19 | seed << 13) ^ 0x13A5BA1D);
    }
//    public static float tabbyNoise(final float ox, final float oy, final float oz, final int seed) {
//        final float skew = (ox + oy + oz) / 128f,
//                c = (float) Math.cos(ox + oy + oz) * 0.125f,
//                s = (float) Math.sin(ox - oy - oz) * 0.125f,
//                x = ((ox - oz) * 1.3f + oy * 0.8f * s - oz * c * 0.5f) * skew,
//                y = ((oy - ox) * 1.3f + oz * 0.8f * s - ox * c * 0.5f) * skew,
//                z = ((oz - oy) * 1.3f + ox * 0.8f * s - oy * c * 0.5f) * skew;
//
//        final int
//                xf = SeededNoise.fastFloor(x),
//                yf = SeededNoise.fastFloor(y),
//                zf = SeededNoise.fastFloor(z);
//
//        final float
//                dx = (x - xf),
//                dy = (y - yf),
//                dz = (z - zf);
//        final int
//                mx = SeededNoise.fastFloor(dx * 3 - 1),
//                my = SeededNoise.fastFloor(dy * 3 - 1),
//                mz = SeededNoise.fastFloor(dz * 3 - 1);
//        final float
//                xrl = NumberTools.randomSignedFloat(prepareSeed(xf + seed * 65537)),
//                yrl = NumberTools.randomSignedFloat(prepareSeed(yf + seed * 31)),
//                zrl = NumberTools.randomSignedFloat(prepareSeed(zf + seed * 421)),
//                spot = NumberTools.randomSignedFloat(prepareSeed(xf + yf + zf - seed)) * 0.75f + s + c,
//                ax = (dx - 0.5f) * (dx - 0.5f) * 2f,
//                ay = (dy - 0.5f) * (dy - 0.5f) * 2f,
//                az = (dz - 0.5f) * (dz - 0.5f) * 2f;
//
//        return NumberTools.bounce(5f +
//                (xrl * (1f - ax)
//                        + yrl * (1f - ay)
//                        + zrl * (1f - az)
//                        + ((mx == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(xf + mx + seed * 65537))) * ax
//                        + ((my == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(yf + my + seed * 31   ))) * ay
//                        + ((mz == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(zf + mz + seed * 421  ))) * az
//                ));
//
//
//        /*final double spot = (Math.sin(x + y + z) - (c + s) * 6f) * 0.0625f;
//        return Math.sin(
//                +
//              ( Math.cos(ox - oy + y + z - x * spot)
//              + Math.cos(x + oy - oz + z - y * spot)
//              + Math.cos(x + oz - ox + y - z * spot)
//              ));
//              */
//    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }
    /*
     * Linearly interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float interpolate(final float start, final float end, final float a)
    {
        return (1f - a) * start + a * end;
    }

    public static float prepare(double n)
    {
        //return (float)n * 0.5f + 0.5f;
        return swayTight((float)n * 1.5f + 0.5f);
    }

    public static float prepare(float n)
    {
        //return (n * 0.5f) + 0.5f;
        return swayTight(n * 1.5f + 0.5f);
    }

    public static float prepare(double n, float multiplier)
    {
        //return (float)n * 0.5f + 0.5f;
        return swayTight((float)n * multiplier + 0.5f);
    }

    public static float prepare(float n, float multiplier)
    {
        //return (n * 0.5f) + 0.5f;
        return swayTight(n * multiplier + 0.5f);
    }

    public static float basicPrepare(double n)
    {
        return (float)n * 0.5f + 0.5f;
    }

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    public static float smoothPrepare(double n)
    {
        n = n * 0.5 + 0.5;
        return (float)(n * n * (3 - 2 * n));
    }

    public static float smoothPrepare(float n)
    {
        n = n * 0.5f + 0.5f;
        return (n * n * (3 - 2 * n));
    }

//    public static class Dunes implements Noise.Noise2D {
//        private int octaves;
//        private double xFrequency, yFrequency;
//        private double correct;
//        private Noise.Noise2D basis, other;
//
//        public Dunes() {
//            this(WhirlingNoise.instance, new Noise.Layered2D(WhirlingNoise.instance, 1, 0.25), 1, 0.25, 0.5);
//        }
//
//        public Dunes(Noise.Noise2D basis) {
//            this(basis, new Noise.Layered2D(basis, 1, 0.25), 1,  0.25, 0.5);
//        }
//
//        public Dunes(Noise.Noise2D basis, Noise.Noise2D other, int octaves, double xFrequency, double yFrequency) {
//            this.basis = basis;
//            this.other = other;
//            this.xFrequency = xFrequency;
//            this.yFrequency = yFrequency;
//            setOctaves(octaves);
//        }
//
//        public void setOctaves(int octaves)
//        {
//            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
//            for (int o = 0; o < octaves; o++) {
//                correct += Math.pow(2.0, -o);
//            }
//            correct = 2.0 / correct;
//        }
//
//
//        @Override
//        public double getNoise(double x, double y) {
//            double sum = 0, amp = 1.0;
//            x *= xFrequency;
//            y *= yFrequency;
//            for (int i = 0; i < octaves; ++i) {
//                double n = basis.getNoise(x + (i << 6), y + (i << 7));
//                n = 1.0 - Math.abs(n);
//                sum += amp * n;
//                amp *= 0.5;
//                x *= 2.0;
//                y *= 2.0;
//            }
//            return sum * correct - 1.0;
//        }
//
//        @Override
//        public double getNoiseWithSeed(double x, double y, long seed) {
//            x *= xFrequency;
//            y *= yFrequency;
//            seed = ThrustAltRNG.determine(++seed);
//            double back = (other.getNoiseWithSeed(x * 0.875, y * 0.25, ThrustAltRNG.determine(seed)) + 1.0);
//            return (1.0 - Math.abs(
//                    Noise.Basic1D.noise(y * 0.125 + (Noise.Basic1D.noise(x * 0.75 + (32.0 * back), seed) * 32.0), ThrustAltRNG.determine(seed + 1)))) * back * correct * 0.5 - 1.0;
//        }
//    }

    private static class PangolinRNG implements RandomnessSource{
        private long stateA, stateB;

        public PangolinRNG(long stateA, long stateB) {
            this.stateA = stateA;
            this.stateB = stateB | 1L;
//            this.stateB = (stateB << 16 & 0xFFFFFFFF0000L);
        }

        @Override
        public int next(int bits) {
            return (int)(nextLong() >>> 64 - bits);
        }

        @Override
        public long nextLong() {
//            long b = (stateB += 0xB69E1722EB5C42CAL);
//            long a = (stateA += 0xC6BC279692B5C323L) + b;
//            a ^= a >>> 31;
//            a *= b;
////            a *= b;
////            a ^= a >>> 1;
////            a *= 0xD1342543DE82EF95L;
//            return (a ^ a >>> 26);

//            long a = (stateA += 0xC6BC279692B5C323L);
//            a ^= a >>> 31;
//            a *= (stateB += 0x9E3779B97F4A7C16L);
//            a ^= a >>> 33;
//            a *= 0xACBD2BDCA2BFF56DL;
//            return a ^ a >>> 26;

//            long a = stateB & (stateA += 0xC6BC279692B5C323L);
//            a = (a ^ a >>> 31) * (stateA | (stateB += 0x9E3779B97F4A7C16L));
//            return (a ^ a >>> 26);
//(stateB << 1 ^ ((stateB >> 63) & 0x000000000000001BL))

            // Seems to work really well; we still need to see how it does in PractRand.
            // This does randomize the seed grid quite well, and keeps TangleRNG's strong points.
            long z = (stateA += 0xC6BC279692B5C323L) * (stateB += 0x9E3779B97F4A7C16L);
            z = (z ^ z >>> 31) * 0xACBD2BDCA2BFF56DL;
            return z ^ z >>> 26;

//            // The image shows a 512x512 grid of lousy random number generators, and each frame advances all generators
//            // by one step. stateA is seeded with x * 2 + 1, and stateB is seeded with y * 2 + 1. Only the lowest bit is
//            // shown (black for 0, white for 1). stateA updates every frame. stateB is (unintentionally) never updated.
//            long z = (stateA += 0xC6BC279692B5C323L) ^ (stateB << 1 ^ ((stateB >> 63) & 0x000000000000001BL));
////            z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L; // same as just multiplying, pretty much.
////            z *= 0xACBD2BDCA2BFF56DL; // very bad, pure-artifact checkerboard-y pattern.
//            z *= 0xAEF17502108EF2D9L;   // no idea why this constant and the 25 below it are "magic"
//            return z ^ z >>> 25;        // 26 also works, but is smaller. others don't work well, so far.

        }
//            a *= 0xACBD2BDCA2BFF56DL;

        @Override
        public RandomnessSource copy() {
            return new PangolinRNG(stateA, stateB);
        }
    }

    @Override
    public void create() {
        CoordPacker.init();
        batch = new FilterBatch();
        tcf = new TextCellFactory().includedFont().width(1).height(1).initBySize();
        //display = new SparseLayers(width, height, cellWidth, cellHeight, new TextCellFactory().includedFont());
//        IFilter<Color> filter0 = new Filters.PaletteFilter(
//                new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f},
//                new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,},
//                new float[]{0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f},
//                new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,}),
//                filter1 = new Filters.PaletteFilter(SColor.YELLOW_GREEN_SERIES),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
//                filter2 = new Filters.PaletteFilter(new SColor[]{SColor.TREE_PEONY, SColor.NAVAJO_WHITE, SColor.BELLFLOWER, SColor.CAPE_JASMINE, SColor.CELADON, SColor.DAWN, SColor.TEAL}),
//                filter3 = new Filters.GrayscaleFilter(),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
//                filter4 = new Filters.PaletteFilter(new SColor[]{SColor.NAVAJO_WHITE, SColor.CAPE_JASMINE, SColor.LEMON_CHIFFON, SColor.PEACH_YELLOW}),
//                filter5 = new Filters.PaletteFilter(new SColor[]{SColor.CORAL_RED, SColor.MEDIUM_SPRING_GREEN, SColor.PSYCHEDELIC_PURPLE, SColor.EGYPTIAN_BLUE});
        gradient = new ArrayList<Color>(256);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_DARK_MAGENTA, CW_RICH_INDIGO, 33));
        gradient.remove(32);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_RICH_INDIGO, CW_FLUSH_BLUE, 33));
        gradient.remove(64);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_FLUSH_BLUE, CW_RICH_CYAN, 33));
        gradient.remove(96);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_RICH_CYAN, CW_DARK_JADE, 33));
        gradient.remove(128);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_DARK_JADE, CW_BRIGHT_HONEYDEW, 33));
        gradient.remove(160);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_BRIGHT_HONEYDEW, CW_LIGHT_YELLOW, 33));
        gradient.remove(192);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_LIGHT_YELLOW, CW_BRIGHT_APRICOT, 33));
        gradient.remove(224);
        gradient.addAll(DefaultResources.getSCC().gradient(CW_BRIGHT_APRICOT, CW_RED, 32));

        for (int i = 0; i < 256; i++) {
            gradientF[i] = gradient.get(i).toFloatBits();
            final float cos = NumberTools.cos_(i * 0x1p-8f);
            bumpF[i] = SColor.floatGetYCbCr(cos * 0.5f + 0.5f, -0.125f - cos * 0.1f, -0.25f, 1f);
            grayscaleF[i] = Float.intBitsToFloat(i * 0x00010101 | 0xFE000000);
        }
        
        yolkA = CrossHash.Yolk.andromalius;
        yolkA_ = CrossHash.Yolk.andromalius_;
        mist = new CrossHash.Mist();
        mistA = CrossHash.Mist.alpha;
        mistB = CrossHash.Mist.beta;
        mistC = CrossHash.Mist.chi;
        fuzzy = new ThrustAltRNG(0xBEEFCAFEF00DCABAL);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                randomGrid[x][y] = new PangolinRNG(x*2+1, y*2+1);
//                randomGrid[x][y] = new XoshiroStarStar64RNG(x ^ y << 9);
//                randomGrid[x][y] = new RandomXS128(x+1, y+1);
//                randomGrid[x][y] = new RandomXS128(x*2+1, y*2+1);
//                randomGrid[x][y] = new TangleRNG(DiverRNG.randomize(x), y << 1);
            }
        }
        
        view = new ScreenViewport();

        Noise.seamless3D(seamless[0], 1337, 1);
        Noise.seamless3D(seamless[1], 123456, 1);
        Noise.seamless3D(seamless[2], -9999, 1);
        ArrayTools.fill(back, FLOAT_WHITE);
        ca.current.insert(250, 250).insert(250, 251).insert(249, 250)
                .insert(250, 249).insert(251, 249)
                .insert(125, 125).insert(125, 126).insert(124, 125)
                .insert(125, 124).insert(126, 124)
                .insert(375, 375).insert(375, 376).insert(374, 375)
                .insert(375, 374).insert(376, 374)
                .insert(125, 375).insert(125, 376).insert(124, 375)
                .insert(125, 374).insert(126, 374)
                .insert(375, 125).insert(375, 126).insert(374, 125)
                .insert(375, 124).insert(376, 124);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case '-':
                        switch (testType)
                        {
                            case 4:
                                noiseMode = (noiseMode + NOISE_LIMIT - 1) % NOISE_LIMIT;
                                break;
                            case 5:
                                rngMode = (rngMode + RNG_LIMIT - 1) % RNG_LIMIT;
                                break;
                            case 0:
                                hashMode = (hashMode + 52) % 53;
                                break;
                            case 1:
                                hashMode = (hashMode + 72) % 73;
                                break;
                            case 2:
                                hashMode = (hashMode + 27) % 28;
                                break;
                            default:
                                otherMode = (otherMode + 20) % 21;
                                break;
                        }
                        break;
                    case 'u':
                    case 'U':
                    case SquidInput.ENTER:
                        switch (testType) {
                            case 4:
                                if (key == SquidInput.ENTER) {
                                    noiseMode++;
                                    noiseMode %= NOISE_LIMIT;
                                }
                                switch (noiseMode) {
                                    case 16:
                                    case 60:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);

                                        Noise.seamless3D(seamless[0], 1337, 1);
                                        Noise.seamless3D(seamless[1], 123456, 1);
                                        Noise.seamless3D(seamless[2], -9999, 1);
                                        break;
                                    case 17:
                                    case 61:
                                        ArrayTools.fill(seamless[0], 0.0);

                                        Noise.seamless3D(seamless[0], -31337, 1);
                                        break;
                                    case 20:
                                    case 64:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1);
                                        Noise.seamless2D(seamless[1][0], 123456, 1);
                                        Noise.seamless2D(seamless[2][0], -9999, 1);
                                        break;
                                    case 21:
                                    case 65:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1);
                                        break;
                                    case 32:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, turb6D);
                                        Noise.seamless3D(seamless[1], 123456, 1, turb6D);
                                        Noise.seamless3D(seamless[2], -9999, 1, turb6D);
                                        break;
                                    case 33:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, turb6D);
                                        total = Noise.total;
                                        break;
                                    case 34:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, turb4D);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, turb4D);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, turb4D);
                                        break;
                                    case 35:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, turb4D);
                                        total = Noise.total;
                                        break;
//                                    case 40:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        ArrayTools.fill(seamless[1], 0.0);
//                                        ArrayTools.fill(seamless[2], 0.0);
//                                        Noise.seamless3D(seamless[0], 1337, 1, slick6D);
//                                        Noise.seamless3D(seamless[1], 123456, 1, slick6D);
//                                        Noise.seamless3D(seamless[2], -9999, 1, slick6D);
//                                        break;
//                                    case 41:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        Noise.seamless3D(seamless[0], -31337, 1, slick6D);
//                                        break;
//                                    case 42:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        ArrayTools.fill(seamless[1], 0.0);
//                                        ArrayTools.fill(seamless[2], 0.0);
//                                        Noise.seamless2D(seamless[0][0], 1337, 1, slick4D);
//                                        Noise.seamless2D(seamless[1][0], 123456, 1, slick4D);
//                                        Noise.seamless2D(seamless[2][0], -9999, 1, slick4D);
//                                        break;
//                                    case 43:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        Noise.seamless2D(seamless[0][0], -31337, 1, slick4D);
//                                        break;
                                    case 48:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, ridged6D);
                                        Noise.seamless3D(seamless[1], 123456, 1, ridged6D);
                                        Noise.seamless3D(seamless[2], -9999, 1, ridged6D);
                                        break;
                                    case 49:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, ridged6D);
                                        break;
                                    case 50:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, ridged4D);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, ridged4D);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, ridged4D);
                                        break;
                                    case 51:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, ridged4D);
                                        break;
                                    case 52:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, SeededNoise.instance);
                                        Noise.seamless3D(seamless[1], 123456, 1, SeededNoise.instance);
                                        Noise.seamless3D(seamless[2], -9999, 1, SeededNoise.instance);
                                        break;
                                    case 53:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, SeededNoise.instance);
                                        break;
//                                    case 56:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        ArrayTools.fill(seamless[1], 0.0);
//                                        ArrayTools.fill(seamless[2], 0.0);
//                                        Noise.seamless2D(seamless[0][0], 1337, 1, SeededNoise.instance);
//                                        Noise.seamless2D(seamless[1][0], 123456, 1, SeededNoise.instance);
//                                        Noise.seamless2D(seamless[2][0], -9999, 1, SeededNoise.instance);
//                                        break;
//                                    case 57:
//                                        ArrayTools.fill(seamless[0], 0.0);
//                                        Noise.seamless2D(seamless[0][0], -31337, 1, SeededNoise.instance);
//                                        break;
                                    case 68:
                                        TuringPattern.initializeInto(turing, ctr);
                                        break;
                                    case 69:
                                        TuringPattern.offsetsCircleInto(turingActivate, width, height, 4);
                                        TuringPattern.offsetsCircleInto(turingInhibit, width, height, 8);
                                        TuringPattern.initializeInto(turing, width, height, stretchScaled2D, ctr);
                                        break;
                                }
                                break;
                            case 5:
                                //mr.mul = 0x632AE59B69B3C209L;
                                rngMode++;
                                rngMode %= RNG_LIMIT;
                                break;
                            case 0:
                                hashMode++;
                                hashMode %= 53; // 45
                                break;
                            case 1:
                                hashMode++;
                                hashMode %= 73;
                                break;
                            case 2:
                                hashMode++;
                                hashMode %= 28;
                            default:
                                otherMode++;
                                otherMode %= 17;
                                if (otherMode == 0) {
                                    ArrayTools.fill(back, FLOAT_WHITE);
                                    //ArrayTools.fill(display.backgrounds, -0x1.fffffep126f); // white as a float
                                    ca.current.insert(250, 250).insert(250, 251).insert(249, 250)
                                            .insert(250, 249).insert(251, 249)
                                            .insert(125, 125).insert(125, 126).insert(124, 125)
                                            .insert(125, 124).insert(126, 124)
                                            .insert(375, 375).insert(375, 376).insert(374, 375)
                                            .insert(375, 374).insert(376, 374)
                                            .insert(125, 375).insert(125, 376).insert(124, 375)
                                            .insert(125, 374).insert(126, 374)
                                            .insert(375, 125).insert(375, 126).insert(374, 125)
                                            .insert(375, 124).insert(376, 124);
                                    //hashMode++;
                                    //hashMode %= 29;
                                }
                        }
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'C':
                        keepGoing = !keepGoing;
                        putMap();
                        break;
                    case 'c':
                        ctr++;
                        putMap();
                        break;
                    case 'S':
                    case 's':
                        testType = (testType + 1) & 1;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'A':
                    case 'a':
                        testType = 3;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'N':
                    case 'n':
                        testType = 4;
                        ctr = -256;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'R':
                    case 'r':
                        testType = 5;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'K': // sKip
                    case 'k':
                        ctr += 1000;
                        //mr.mul -= 2;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        input.setRepeatGap(Long.MAX_VALUE);
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        putMap();
    }

    public void putMap() {
        //display.erase();
        //overlay.erase();
        long code, extra;
        float bright, s0 = 0, c0 = 0, s1 = 0, c1 = 0, s2 = 0, c2 = 0;
        double dBright;
        int iBright;
        int xx, yy;
        switch (testType) {
            case 1: {
                switch (hashMode) {
                    case 0:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 1:
//                        extra = System.nanoTime() >>> 30 & 63;
//                        Gdx.graphics.setTitle("water64 on length 2, bit " + extra);
//                        for (int x = 0; x < width; x++) {
//                            coordinates[0] = x;
//                            for (int y = 0; y < height; y++) {
//                                coordinates[1] = y;
//                                //code = -(Arrays.hashCode(coordinates) >>> extra & 1L) | 255L;
//                                back[x][y] = (CrossHash.Water.hash64(coordinates) >>> extra & 1) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
//                                //back[x][y] = (water64(x, y) >>> extra & 1L) == 0L ? FLOAT_BLACK : FLOAT_WHITE;
//                            }
//                        }
                        extra = System.nanoTime() >>> 30 & 31;
                        Gdx.graphics.setTitle("Modified Rosenberg-Strong Hash on length 2, bit " + extra);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = (rosenbergStrongCoord(x, y) >>> extra & 1) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }

//                        Gdx.graphics.setTitle("QuadHash on index");
//                        for (int x = 0, i = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++, i++) {
//                                iBright = ((i ^ i >>> 2 ^ i >>> 5 ^ 0xD1B54A35) * 0x9E375 >>> (10 + (i & 7)) & 3);
//                                switch (iBright){
//                                    case 0: back[x][y] = -0x1.7677e8p125F;//CW_BRIGHT_RED
//                                        break;
//                                    case 1: back[x][y] = -0x1.97ae42p125F;//CW_FLUSH_JADE
//                                        break;
//                                    case 2: back[x][y] = FLOAT_BLACK;
//                                        break;
//                                    default: back[x][y] = FLOAT_WHITE;
//                                        break;
//                                }
//                            }
//                        }
                        break;
                    case 2:
                        extra = System.nanoTime() >>> 30 & 31;
                        Gdx.graphics.setTitle("Alternate Rosenberg-Strong Hash on length 2, bit " + extra);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = (rosenbergStrongCoord2(x, y) >>> extra & 1) == 0 ? FLOAT_BLACK : FLOAT_WHITE;
                            }
                        }
//                        extra = System.nanoTime() >>> 30 & 31;
//                        Gdx.graphics.setTitle("Szudzik Hash on length 2, bit " + extra);
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                back[x][y] = (szudzikHash2D(x, y) >>> extra & 1) == 0 ? FLOAT_BLACK : FLOAT_WHITE;
//                            }
//                        }
                        break;
                    case 3:
                        extra = System.nanoTime() >>> 30 & 31;
                        Gdx.graphics.setTitle("Coord.xoroHashCode() on length 2, bit " + extra);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = (Coord.xoroHashCode(x, y) >>> extra & 1) == 0 ? FLOAT_BLACK : FLOAT_WHITE;
//                                code = -(Noise.HastyPointHash.hashAll(x, y, 123) >>> extra & 1L) | 255L;
//                                code = Noise.HastyPointHash.hashAll(x, y, 123) >> 63 | 255L;
                                //code = Noise.HastyPointHash.hashAll(x, y, 123) << 8 | 255L;
//                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("PointHash on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = (PointHash.hashAll(x << 9 | y, 0L, 123L) & 1L) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("PointHash on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = Noise.PointHash.hashAll((x << 9) | y, 0L, 123L) >> 63 | 255L;
                                //code = Noise.PointHash.hashAll((x << 9) | y, 0, 123L) << 8 | 255L;
                                back[x][y] = PointHash.hashAll(x << 9 | y, 0L, 123L) < 0L ? FLOAT_WHITE : FLOAT_BLACK;//floatGet(code);
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("HastyPointHash on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = -(Noise.HastyPointHash.hashAll(x << 9 | y, 0L, 123L) & 1L) | 255L;
                                //code = Noise.HastyPointHash.hashAll(x << 9 | y, 0, 123) << 8 | 255L;
                                back[x][y] = (HastyPointHash.hashAll(x << 9 | y, 0L, 123L) & 1L) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("HastyPointHash on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = Noise.HastyPointHash.hashAll(x << 9 | y, 0, 123) >> 63 | 255L;
                                //code = Noise.HastyPointHash.hashAll(x << 9 | y, 0, 123) << 8 | 255L;
                                back[x][y] = HastyPointHash.hashAll(x << 9 | y, 0L, 123L) < 0L ? FLOAT_WHITE : FLOAT_BLACK;//floatGet(code);
                            }
                        }
                        break;
                    case 9:
                        Gdx.graphics.setTitle("HastyPointHash on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = -(Noise.HastyPointHash.hashAll(x, y, 123) & 1L) | 255L;
                                //code = Noise.HastyPointHash.hashAll(x, y, 123) << 8 | 255L;
                                back[x][y] = (HastyPointHash.hashAll(x, y, 123L) & 1L) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }
                        break;
                    case 10:
                        Gdx.graphics.setTitle("PointHash on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = -(Noise.PointHash.hashAll(x, y, 123L) & 1L) | 255L;
                                //code = Noise.PointHash.hashAll(x, y, 123L) >>> 24 | 255L;
                                back[x][y] = (PointHash.hashAll(x, y, 123L) & 1L) == 0 ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }
                        break;
                    case 11:
                        Gdx.graphics.setTitle("Mist_ (chi) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA_.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 12:
                        Gdx.graphics.setTitle("PointHash on length 3, low bits");
                        extra = System.nanoTime() >>> 30;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                //code = Noise.PointHash.hashAll(x, y, extra, 123L) >>> 24 | 255L;
                                back[x][y] = (PointHash.hashAll(x, y, extra, 123L) & 1L) == 0L ? FLOAT_BLACK : FLOAT_WHITE;//floatGet(code);
                            }
                        }
                        break;
                    case 13:
                        Gdx.graphics.setTitle("Mist_ (chi) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA_.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 14:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 15:
                        Gdx.graphics.setTitle("PointHash on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = PointHash.hashAll(x, y, 123L) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 16:
                        Gdx.graphics.setTitle("PointHash on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = PointHash.hashAll(x, y, 123L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 17:
                        Gdx.graphics.setTitle("HastyPointHash on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = HastyPointHash.hashAll(x, y, 123) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 18:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 19:
                        Gdx.graphics.setTitle("Ion 32 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = ion32(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
//                        Gdx.graphics.setTitle("Mist_ (alpha) on length 1, high bits");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                coordinate[0] = (x << 9) | y;
//                                code = Mist_A.hash(coordinate) & 0xFFFFFF00L | 255L;
//                                back[x][y] = floatGet(code);
//                            }
//                        }
                        break;
                    case 20:
                        Gdx.graphics.setTitle("PointHash on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = PointHash.hashAll((x << 9) | y, 0, 123L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 21:
                        Gdx.graphics.setTitle("HastyPointHash on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = HastyPointHash.hashAll(x << 9 | y, 0, 123) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 22:
                        Gdx.graphics.setTitle("Ion 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = ion64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
//                        Gdx.graphics.setTitle("Mist_ (alpha) 64 on length 2, high bits");
//                        for (int x = 0; x < width; x++) {
//                            coordinates[0] = x;
//                            for (int y = 0; y < height; y++) {
//                                coordinates[1] = y;
//                                code = Mist_A.hash64(coordinates) & 0xFFFFFF00L | 255L;
//                                back[x][y] = floatGet(code);
//                            }
//                        }
                        break;
                    case 23:
                        Gdx.graphics.setTitle("PointHash 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = PointHash.hashAll(x, y, 123L) >>> 32 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 24:
                        Gdx.graphics.setTitle("Mist_ (chi) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA_.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 25:
                        Gdx.graphics.setTitle("Ion 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = ion64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
//                        Gdx.graphics.setTitle("Mist_ (alpha) 64 on length 1, high bits");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                coordinate[0] = (x << 9) | y;
//                                code = Mist_A.hash64(coordinate) & 0xFFFFFF00L | 255L;
//                                back[x][y] = floatGet(code);
//                            }
//                        }
                        break;
                    case 26:
                        Gdx.graphics.setTitle("PointHash 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = PointHash.hashAll((x << 9) | y, 0, 123L) >>> 32 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 27:
                        Gdx.graphics.setTitle("Mist_ (chi) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA_.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 28:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 29:
                        Gdx.graphics.setTitle("Mist (default) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 30:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 31:
                        Gdx.graphics.setTitle("Mist (default) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 32:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 33:
                        Gdx.graphics.setTitle("Mist (default) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 34:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 35:
                        Gdx.graphics.setTitle("Mist (default) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 36:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 37:
                        Gdx.graphics.setTitle("Mist (semi-random) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Mist.predefined[HastyPointHash.hash32(x, y, 123456789L)].hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                        
                        
                        
                    case 38:
                        Gdx.graphics.setTitle("FNV-1a 64-bit, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = fnv1a_64(x, y) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 39:
                        Gdx.graphics.setTitle("Mist (alpha) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 40:
                        Gdx.graphics.setTitle("Mist (beta) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 41:
                        Gdx.graphics.setTitle("Mist (chi) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 42:
                        Gdx.graphics.setTitle("FNV-1a 32-bit, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = fnv1a_32(x, y) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 43:
                        Gdx.graphics.setTitle("Mist (alpha) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 44:
                        Gdx.graphics.setTitle("Mist (beta) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 45:
                        Gdx.graphics.setTitle("Mist (chi) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 46:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 47:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 48:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 49:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 50:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 51:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 52:
                        Gdx.graphics.setTitle("FNV-1a 64-bit, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = fnv1a_64(x, y) >>> 32 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 53:
                        Gdx.graphics.setTitle("Mist (alpha) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 54:
                        Gdx.graphics.setTitle("Mist (beta) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 55:
                        Gdx.graphics.setTitle("Mist (chi) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 56:
                        Gdx.graphics.setTitle("FNV-1a 32-bit, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = fnv1a_32(x, y) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 57:
                        Gdx.graphics.setTitle("Mist (alpha) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 58:
                        Gdx.graphics.setTitle("Mist (beta) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 59:
                        Gdx.graphics.setTitle("Mist (chi) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 60:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 61:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 62:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 63:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 64:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 65:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 66:
                        Gdx.graphics.setTitle("FloatHash on length 2");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = floatHash(x * 0.211211211f, y * 0.211211211f, 0.123456789f);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 67:
                        Gdx.graphics.setTitle("FloatHash on length 3");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = floatHash(x * 0.211211211f, y * 0.211211211f, ctr * 0.211211211f, 0.3456789f);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 68:
                        Gdx.graphics.setTitle("FloatHash (3 seeds, color) on length 2");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(
                                        floatHash(x * 0.211211211f, y * 0.211211211f, 0.39456189f),
                                        floatHash(x * 0.211211211f, y * 0.211211211f, 0.48456289f),
                                        floatHash(x * 0.211211211f, y * 0.211211211f, 0.57456389f),
                                        1f
                                );
                            }
                        }
                        break;
                    case 69:
                        Gdx.graphics.setTitle("FloatHash (3 seeds, color) on length 3");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(
                                        floatHash(x * 0.211211211f, y * 0.211211211f, ctr * 0.211211211f, 0.39456189f),
                                        floatHash(x * 0.211211211f, y * 0.211211211f, ctr * 0.211211211f, 0.48456289f),
                                        floatHash(x * 0.211211211f, y * 0.211211211f, ctr * 0.211211211f, 0.57456389f),
                                        1f
                                );
                            }
                        }
                        break;
                    case 70:
                        extra = System.nanoTime() >>> 30 & 63;
                        Gdx.graphics.setTitle("Ion 64 on length 3 (with time), bit " + extra);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(ion64(x, y, ctr) >>> extra & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 71:
                        Gdx.graphics.setTitle("Ion 64 on length 3 (with time), Hamming weights");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = ion64(x, y, ctr);
                                //bright = Long.bitCount(code) * 0x1p-6f;
                                back[x][y] = floatGet(Long.bitCount(code >>> 43) / 21f,
                                        Long.bitCount(code >>> 21 & 0x3fffffL) / 22f,
                                        Long.bitCount(code & 0x1fffffL) / 21f, 1f);
                            }
                        }
                        break;
                    case 72:
                        Gdx.graphics.setTitle("Ion 64 on length 3 (with time), high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = ion64(x, y, ctr) >>> 32 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;


                }
            }
            break;
            case 0: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 2, low bits");
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 2, low bits");
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash on length 2, low bits");
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 2, low bits");
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 1, low bits");
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 1, low bits");
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash on length 1, low bits");
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 1, low bits");
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 2, low bits");
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash64 on length 2, low bits");
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 2, low bits");
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 1, low bits");
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash64 on length 1, low bits");
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 1, low bits");
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 2, high bits");
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 2, high bits");
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash on length 2, high bits");
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 2, high bits");
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 1, high bits");
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 1, high bits");
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash on length 1, high bits");
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 1, high bits");
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 2, high bits");
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = yolkA.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash64 on length 2, high bits");
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 2, high bits");
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 1, high bits");
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = yolkA.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Mist_, hash64 on length 1, high bits");
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 1, high bits");
                        break;
                    case 28:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Hive.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash on length 2, high bits");
                        break;
                    case 29:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Hive.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash on length 1, high bits");
                        break;
                    case 30:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Hive.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash64 on length 2, high bits");
                        break;
                    case 31:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Hive.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash64 on length 1, high bits");
                        break;
                    case 32:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash on length 2, high bits");
                        break;
                    case 33:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash on length 1, high bits");
                        break;
                    case 34:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash64 on length 2, high bits");
                        break;
                    case 35:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash64 on length 1, high bits");
                        break;
                    case 36:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (mixHash(x, y) << 8) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("mixHash");
                        break;
                    case 37:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash on length 1, low bits");
                        break;
                    case 38:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash64 on length 1, low bits");
                        break;
                    case 39:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash on length 2, low bits");
                        break;
                    case 40:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water, hash64 on length 2, low bits");
                        break;
                    case 41:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Hive.hash(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash on length 2, low bits");
                        break;
                    case 42:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Hive.hash(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash on length 1, low bits");
                        break;
                    case 43:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Hive.hash64(coordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash64 on length 2, low bits");
                        break;
                    case 44:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Hive.hash64(coordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Hive, hash64 on length 1, low bits");
                        break;

                    case 45:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.hash(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash on length 2, high bits");
                        break;
                    case 46:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.hash(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash on length 1, high bits");
                        break;
                    case 47:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.hash64(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash64 on length 2, high bits");
                        break;
                    case 48:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.hash64(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash64 on length 1, high bits");
                        break;
                    case 49:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.hash(doubleCoordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash on length 1, low bits");
                        break;
                    case 50:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.hash64(doubleCoordinate) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash64 on length 1, low bits");
                        break;
                    case 51:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.hash(doubleCoordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash on length 2, low bits");
                        break;
                    case 52:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.hash64(doubleCoordinates) << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("Water Double, hash64 on length 2, low bits");
                        break;
                }
            }
            break;
            case 4: { //Noise mode
                switch (noiseMode) {
                    case 0:
                        Gdx.graphics.setTitle("Seeded Noise, 3 manual octaves at " + Gdx.graphics.getFramesPerSecond() +
                                " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (float) (
                                        //prepare((
                                        //PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                        //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                        SeededNoise.noise(xx * 0.02777777777777777, yy * 0.02777777777777777, 123456789) * 4 +
                                        SeededNoise.noise(xx * 0.05555555555555555, yy * 0.05555555555555555, 123456789) * 2 +
                                        SeededNoise.noise(xx * 0.1111111111111111, yy * 0.1111111111111111, 123456789)
                                        ///) 7f);
                                        + 7f) / 14f;
                                        //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 1:
                        Gdx.graphics.setTitle("Whirling 2D Noise, 3 normal octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = basicPrepare(layered2D.getNoise(xx * 0.125, yy * 0.125));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 2:
                        Gdx.graphics.setTitle("Whirling 2D Noise, 3 inverse octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = //(float) (
                                        basicPrepare(invLayered2D.getNoise(xx * 0.125, yy * 0.125));
                                //               + 1f) * 0.5f;
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("Whirling 3D Noise, 3 octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            //xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                //yy = y + ctr;
                                bright = basicPrepare(layered3D.getNoise(x * 0.125, y * 0.125, ctr * 0.0625));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 4:
                        Gdx.graphics.setTitle("ColorNoise 2D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        ColorNoise.instance.colorNoise(x * 0.0625f + 20f + ctr * 0.05f, y * 0.0625f + 30f + ctr * 0.05f, 1234);
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("ColorNoise 3D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        ColorNoise.instance.colorNoise(x * 0.05f + 20f, y * 0.05f + 30f, ctr * 0.05f, 1234);
                            }
                        }
                        break;
                        case 6:
                        Gdx.graphics.setTitle("Merlin Noise 2D, x16 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int)MerlinNoise.noise2D(x + ctr, y + ctr, 9000L, 4, 1) * 255;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;
                    /*case 6:
                        Gdx.graphics.setTitle("Merlin Precalc Noise, seed 0");
                        map = MerlinNoise.preCalcNoise2D(width, height, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = map[x][y];
                                back[x][y] = floatGet(iBright, iBright, iBright);
                            }
                        }
                        break;
                        */
                    /*
                    case 7:
                        Gdx.graphics.setTitle("Merlin Precalc Noise, seed 65535");
                        map = MerlinNoise.preCalcNoise2D(width, height, 65535);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = map[x][y];
                                back[x][y] = floatGet(iBright, iBright, iBright);
                            }
                        }
                        break;*/
                    case 7:
                        Gdx.graphics.setTitle("Perlin 3D Noise, 3 manual octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (
                                        //PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                SeededNoise.noise(x * 0.02777777777777777, y * 0.02777777777777777, ctr * 0.03333333333333333, 123456789) * 4 +
                                        SeededNoise.noise(x * 0.05555555555555555, y * 0.05555555555555555, ctr * 0.03333333333333333, 123456789) * 2 +
                                        SeededNoise.noise(x * 0.1111111111111111, y * 0.1111111111111111, ctr * 0.03333333333333333, 123456789)

                                        // / 7);

                                                        + 7f) / 14f;
                                //+ 15.0f) / 30f;

                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("Perlin 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (float)
                                        (//PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                                //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                                //PerlinNoise.noise(xx / 4.0, yy / 4.0) * 4 +
                                                //PerlinNoise.noise(xx / 2.0, yy / 2.0) * 2 +
                                                PerlinNoise.noise(xx, yy)
                                                        + 1f) * 0.5f;
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 9:
                        Gdx.graphics.setTitle("Perlin 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)
                                        (//PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                                //PerlinNoise.noise(x / 4.0, y / 4.0, ctr * 0.125) * 4 +
                                                //PerlinNoise.noise(x / 2.0, y / 2.0, ctr * 0.125) * 2 +
                                                PerlinNoise.noise(x, y, ctr * 0.3)
                                                        + 1f) * 0.5f;
                                //+ 15.0f) / 30f;

                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 10:
                        Gdx.graphics.setTitle("Whirling 3D Noise, processed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = //(float) (
                                        prepare(WhirlingNoise.noise(x * 0.03125, y * 0.03125, ctr * 0.0375));
                                         //               + 1f) * 0.5f;
                                //+ 15.0f) / 30f;

                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 11:
                        Gdx.graphics.setTitle("Whirling 2D Noise, processed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = //(float) (
                                        prepare(WhirlingNoise.noise(xx * 0.03125, yy * 0.03125));
                                         //               + 1f) * 0.5f;
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 12:
                        Gdx.graphics.setTitle("Whirling 4D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        dBright = 1.5 * NumberTools.swayRandomized(12345L, 0.03125 * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = ((float)
                                        WhirlingNoise.noise(x * 0.03125, y * 0.03125, ctr * 0.0375, dBright)
                                + 1f) * 0.5f;
                                //+ 15.0f) / 30f;

                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 13:
                        Gdx.graphics.setTitle("Whirling Alt 2D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright =
                                        //prepare(
                                                WhirlingNoise.noiseAlt(xx * 0.03125, yy * 0.03125)
                                        //);
                                         * 0.5f + 0.5f;
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                        //You can preview this at https://dl.dropboxusercontent.com/u/11914692/rainbow-perlin.gif
                    case 14:
                        Gdx.graphics.setTitle("Whirling Alt 2D Noise, processed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                /*
                                (
                                        (WhirlingNoise.noiseAlt(xx * 0.0625, yy * 0.0625)) * 4.47f+
                                        (WhirlingNoise.noiseAlt(xx * 0.125, yy * 0.125)) * 2.34f +
                                        (WhirlingNoise.noiseAlt(xx * 0.25, yy * 0.25)) * 1.19f)
                                 */
                                bright = prepare(WhirlingNoise.noiseAlt(xx * 0.03125, yy * 0.03125)
                                );

                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 15:
                        Gdx.graphics.setTitle("Whirling 3D Color Noise, unprocessed " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 1234567L) + 1f) * 0.5f,
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f + 234.5, 7654321L) + 1f) * 0.5f,
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f + 678.9, 9999999L) + 1f) * 0.5f,
                                        1f);
                            }
                        }
                        break;
                                                /*
                        double p, q, r = ctr * 0.03125 * 0.0625,
                                dx_div_2pi = 2.0 * 0.15915494309189535,
                                dy_div_2pi = 2.0 * 0.15915494309189535,
                                dz_div_2pi = 16.0 * 0.15915494309189535,
                                ps, pc,
                                qs, qc,
                                rs = Math.sin(r * 6.283185307179586) * dz_div_2pi, rc = Math.cos(r * 6.283185307179586) * dz_div_2pi;
                        int idx = 0;
                        for (int x = 0; x < 64; x++) {
                            p = x * 0.015625;
                            ps = Math.sin(p * 6.283185307179586) * dx_div_2pi;
                            pc = Math.cos(p * 6.283185307179586) * dx_div_2pi;
                            for (int y = 0; y < 64; y++) {
                                q = y * 0.015625;
                                qs = Math.sin(q * 6.283185307179586) * dy_div_2pi;
                                qc = Math.cos(q * 6.283185307179586) * dy_div_2pi;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 1234) * 0.5) + 0.5f;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 54321) * 0.5) + 0.5f;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 1234321) * 0.5) + 0.5f;
                            }
                        }
                        */
                        /*
                        double p, q, r = ctr * 0.03125 * 0.0625,
                                dx_div_2pi = 2.0 * 0.15915494309189535,
                                dy_div_2pi = 2.0 * 0.15915494309189535,
                                dz_div_2pi = 16.0 * 0.15915494309189535,
                                ps, pc,
                                qs, qc,
                                rs = Math.sin(r * 6.283185307179586) * dz_div_2pi, rc = Math.cos(r * 6.283185307179586) * dz_div_2pi;
                        int idx = 0;
                        for (int x = 0; x < 64; x++) {
                            p = x * 0.015625;
                            ps = Math.sin(p * 6.283185307179586) * dx_div_2pi;
                            pc = Math.cos(p * 6.283185307179586) * dx_div_2pi;
                            for (int y = 0; y < 64; y++) {
                                q = y * 0.015625;
                                qs = Math.sin(q * 6.283185307179586) * dy_div_2pi;
                                qc = Math.cos(q * 6.283185307179586) * dy_div_2pi;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 123456) * 0.5) + 0.5f;
                            }
                        }
                        */


                    case 16:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                basicPrepare(Noise.seamless3D(seeded, x, y, ctr, 128.0, 128.0, 128.0, 1234567890L)),
                                                basicPrepare(Noise.seamless3D(seeded, x, y, ctr, 128.0, 128.0, 128.0, 9092929090L)),
                                                basicPrepare(Noise.seamless3D(seeded, x, y, ctr, 128.0, 128.0, 128.0, 9876543210L)),
//                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
//                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
//                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f);

                            }
                        }
                        break;
                    case 17:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(Noise.seamless3D(seeded, x, y, ctr, 128.0, 128.0, 128.0, 1234567890L));
//                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);

                            }
                        }
                        break;
                    case 60:
                    case 18:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 0.0, 0.0, 0.0,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 0.0, 0.0, 0.0,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 0.0, 0.0, 0.0,1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 61:
                        Gdx.graphics.setTitle("Foam 6D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                ((float)phantom6D_A.getNoise(alter6D(~x, y, ~ctr)) * 0.50f) + 0.50f,
                                                ((float)phantom6D_B.getNoise(alter6D(~x, ~y, ctr)) * 0.50f) + 0.50f,
                                                ((float)phantom6D_C.getNoise(alter6D(x, ~y, ~ctr)) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 62:
                        Gdx.graphics.setTitle("Foam 7D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)phantom7D_A.getNoise(alter7D(~x, y, ~ctr)) * 0.50f) + 0.50f,
                                                ((float)phantom7D_B.getNoise(alter7D(~x, ~y, ctr)) * 0.50f) + 0.50f,
                                                ((float)phantom7D_C.getNoise(alter7D(x, ~y, ~ctr)) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 19:
                    case 63:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        0.0, 0.0, 0.0, 123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 20:
                    case 64:
                        Gdx.graphics.setTitle("Seeded Seamless 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                    break;
                    case 21:
                    case 65:
                        Gdx.graphics.setTitle("Seeded Seamless 2D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                    break;
                    case 22:
                    case 66:
                        Gdx.graphics.setTitle("Seeded 4D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 0,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 0,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 0,1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 23:
                    case 67:
                        Gdx.graphics.setTitle("Seeded 4D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        0.0,123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 24:
                        Gdx.graphics.setTitle("Seeded 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 25:
                        Gdx.graphics.setTitle("Seeded 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 26:
                        Gdx.graphics.setTitle("Seeded 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float)(SeededNoise.noise(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(SeededNoise.noise(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(SeededNoise.noise(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 27:
                        Gdx.graphics.setTitle("Seeded 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125 + ctr  * 0.05125, y * 0.03125 + ctr  * 0.05125,
                                        123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 28:
                        Gdx.graphics.setTitle("Whirling Ridged 3D Color Noise, two octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1234) * 0.50f) + 0.50f,
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 54321) * 0.50f) + 0.50f,
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 29:
                        Gdx.graphics.setTitle("Whirling Ridged 3D Noise, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */ridged3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 30:
                        Gdx.graphics.setTitle("Seeded Ridged 2D Color Noise, two octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 31:
                        Gdx.graphics.setTitle("Seeded Ridged 2D Noise, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */ridged2D.getNoiseWithSeed(x * 0.03125 + ctr  * 0.05125, y * 0.03125 + ctr  * 0.05125,
                                        123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 32:
                        Gdx.graphics.setTitle("Whirling Turbulent Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 33:
                        //Gdx.graphics.setTitle("Seeded Turbulent Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        Gdx.graphics.setTitle("Turb6D Seamless at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 34:
                        Gdx.graphics.setTitle("Whirling Turbulent Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 35:
                        //Gdx.graphics.setTitle("Seeded Turbulent Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        Gdx.graphics.setTitle("Turb4D Seamless at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 36:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 37:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 38:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 39: {
                        Gdx.graphics.setTitle("Seeded Slick Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 40:
                        Gdx.graphics.setTitle("Whirling Turbulent 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234) * 0.50f) + 0.50f,
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 54321) * 0.50f) + 0.50f,
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 41:
                        total = 0.0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125,
                                        123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        Gdx.graphics.setTitle("Turb3D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 42:
                        Gdx.graphics.setTitle("Turbulent 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 43:
                        total = 0.0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */turb2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                        123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        Gdx.graphics.setTitle("Turb2D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 44:
                        Gdx.graphics.setTitle("Seeded Slick 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234) * 0.50f) + 0.50f,
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 54321) * 0.50f) + 0.50f,
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 45:
                        Gdx.graphics.setTitle("Seeded Slick 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 46:
                        Gdx.graphics.setTitle("Seeded Slick 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 47:
                        Gdx.graphics.setTitle("Seeded Slick 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        slick2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                        123456) * 0.5 + 0.5);
                                if(bright > 1f)
                                    bright = 1f;
                                else if(bright < 0f)
                                    bright = 0f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 48:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 49:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 50:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f);
                            }
                        }
                        break;
                    case 51: {
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;

                    case 52:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f);

                            }
                        }
                        break;
                    case 53:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                back[x][y] = getGray(bright);

                            }
                        }
                        break;
                    case 54:
                        Gdx.graphics.setTitle("Weaving 2D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                ((float)weavingNoise((x + ctr) * 0.03125 + 20, (y + ctr) * 0.03125 + 30, 1234) * 0.50f) + 0.50f,
                                                ((float)weavingNoise((x + ctr) * 0.03125 + 30, (y + ctr) * 0.03125 + 10, 54321) * 0.50f) + 0.50f,
                                                ((float)weavingNoise((x + ctr) * 0.03125 + 10, (y + ctr) * 0.03125 + 20, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
//                        Gdx.graphics.setTitle("Jack 2D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                back[x][y] =
//                                        floatGet(
//                                                ((float)JackNoise.instance.getNoiseWithSeed((x + ctr) * 0.03125 + 20, (y + ctr) * 0.03125 + 30, 1234) * 0.50f) + 0.50f,
//                                                ((float)JackNoise.instance.getNoiseWithSeed((x + ctr) * 0.03125 + 30, (y + ctr) * 0.03125 + 10, 54321) * 0.50f) + 0.50f,
//                                                ((float)JackNoise.instance.getNoiseWithSeed((x + ctr) * 0.03125 + 10, (y + ctr) * 0.03125 + 20, 1234321) * 0.50f) + 0.50f,
//                                                1.0f);
//                            }
//                        }
//                        Gdx.graphics.setTitle("Seeded 6D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                back[x][y] = 
//                                        floatGet(
//                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1.0, 3.0, 2.0,1234) * 0.50f) + 0.50f,
//                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 1.0, 3.0, 2.0,54321) * 0.50f) + 0.50f,
//                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1.0, 3.0, 2.0,1234321) * 0.50f) + 0.50f,
//                                                1.0f);
//                            }
//                        }
                        break;
                    case 55:
                        Gdx.graphics.setTitle("Weaving 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(weavingNoise(x * 0.03125, y * 0.03125, ctr * 0.05125, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
//                        Gdx.graphics.setTitle("Jack 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
//                                        20.0, 20.0, 20.0, 12) * 0.5
//                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
//                                        40.0, 40.0, 20.0, 1234)
//                                        + */JackNoise.instance.getNoiseWithSeed((x + ctr) * 0.03125, (y + ctr) * 0.03125, 123456) * 0.50f) + 0.50f;
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                        Gdx.graphics.setTitle("Seeded 6D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
//                                        20.0, 20.0, 20.0, 12) * 0.5
//                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
//                                        40.0, 40.0, 20.0, 1234)
//                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
//                                        1.0, 3.0, 2.0, 123456) * 0.50f) + 0.50f;
//                                back[x][y] = getGray(bright);
//                            }
//                        }
                        break;
                    case 56:
                        Gdx.graphics.setTitle("Jack 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.03125 + 10, 1234) * 0.50f) + 0.50f,
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.03125 + 20, 54321) * 0.50f) + 0.50f,
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.03125 + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
//                        Gdx.graphics.setTitle("Seeded Seamless 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                back[x][y] = 
//                                        floatGet(
//                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
//                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
//                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
//                                                1.0f);
//                            }
//                        }
                        break;
                    case 57: {
                        Gdx.graphics.setTitle("Jack 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(JackNoise.instance.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.03125, 123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
//                        Gdx.graphics.setTitle("Seeded Seamless 2D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                    }
                    break;
                    case 58:
                        Gdx.graphics.setTitle("Jack 4D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.04 * 0.8157559148337911 + y * 0.04 * 0.5797766823136037, x * 0.04 * -0.8186594543410776 + y * 0.04 * 0.5756498588428911, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * 0.816496580927726, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * -0.816496580927726, 1234) * 0.50f) + 0.50f,
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.04 * 0.8157559148337911 + y * 0.04 * 0.5797766823136037, x * 0.04 * -0.8186594543410776 + y * 0.04 * 0.5756498588428911, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * 0.816496580927726, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * -0.816496580927726, 54321) * 0.50f) + 0.50f,
                                                ((float)JackNoise.instance.getNoiseWithSeed(x * 0.04 * 0.8157559148337911 + y * 0.04 * 0.5797766823136037, x * 0.04 * -0.8186594543410776 + y * 0.04 * 0.5756498588428911, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * 0.816496580927726, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * -0.816496580927726, 1234321) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 59: {
                        Gdx.graphics.setTitle("Jack 4D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = ((float)JackNoise.instance.getNoiseWithSeed(x * 0.04 * 0.8157559148337911 + y * 0.04 * 0.5797766823136037, x * 0.04 * -0.8186594543410776 + y * 0.04 * 0.5756498588428911, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * 0.816496580927726, x * 0.04 * 0.0014176412627734311 + y * 0.04 * -0.5773484947708223 + ctr * 0.04 * -0.816496580927726, 123456) * 0.50f) + 0.50f;
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
//                        Gdx.graphics.setTitle("Ecto Noise 2D, 1 octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = ectoNoise((x + ctr) * 0.0625f, (y + ctr) * 0.0625f, -999999L) * 0.5f + 0.5f; //0.61803398875
//                                back[x][y] = getGray(bright);
//                            }
//                        }
                        break;
                    case 68:
                        Gdx.graphics.setTitle("Turing Pattern at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        TuringPattern.distort(turingActivate, width, height, stretchScaled3D, ctr * 5, 778899);
                        TuringPattern.distort(turingInhibit, width, height, stretchScaled3D, ctr * 5, 556677);
                        TuringPattern.step(turing, turingActivate, 0.2, turingInhibit, -0.2);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(turing[x * height + y]) * 0.5f + 0.5f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 69:
                        Gdx.graphics.setTitle("Turing Pattern from SeededNoise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        TuringPattern.step(turing, turingActivate, 0.1, turingInhibit, -0.1);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(turing[x * height + y]) * 0.5f + 0.5f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 70:
                        Gdx.graphics.setTitle("Mason 2D Standard Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        MasonNoise.noise(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        /*
                        Gdx.graphics.setTitle("Mason 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField[0], 0f);
                        ArrayTools.fill(fillField[1], 0f);
                        ArrayTools.fill(fillField[2], 0f);
                        MasonNoise.addNoiseField(fillField[0], 20f + ctr * 0.05125f, 30f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, -1234, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField[1], 30f + ctr * 0.05125f, 10f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 54321, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField[2], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 15951, 1f, 1.375f);

                        MasonNoise.addNoiseField(fillField[0], 20f + ctr * 0.05125f, 30f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 1234, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField[1], 30f + ctr * 0.05125f, 10f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, -321, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField[2], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -951, 1f, 0.625f);

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(fillField[0][x][y] * 0.25f + 0.5f, fillField[1][x][y] * 0.25f + 0.5f, fillField[2][x][y] * 0.25f + 0.5f, 1f)
                                ;
                            }
                        }*/
                        break;
                    case 71:
                        Gdx.graphics.setTitle("Mason 2D Alt Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        masonLayered2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        /*
                        Gdx.graphics.setTitle("Mason 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField[0], 0f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -12345,  3.5f, 1.08f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 123456, 2.625f, 1.44f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -54321, 1.25f, 1.87f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 654321, 0.625f, 2.19f);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = fillField[0][x][y] * 0.0625f + 0.5f;
                                back[x][y] = 
                                        getGray(bright)
                                ;
                            }
                        }
                        */
                        break;
                    case 72:
                        Gdx.graphics.setTitle("Mason 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField3DR[0], 0f);
                        ArrayTools.fill(fillField3DG[0], 0f);
                        ArrayTools.fill(fillField3DB[0], 0f);
                        MasonNoise.addNoiseField(fillField3DR,20f, 30f, 10f + ctr * 0.05125f,
                                512f * 0.0625f + 20f, 512f * 0.0625f + 30f, 10f + ctr * 0.05125f,
                                -1234, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField3DG, 30f, 10f, 20f + ctr * 0.05125f,
                                512f * 0.0625f + 30f, 512f * 0.0625f + 10f, 20f + ctr * 0.05125f,
                                54321, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField3DB,10f, 20f, 30f + ctr * 0.05125f,
                                512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f,
                                15951, 1f, 1.375f);

                        MasonNoise.addNoiseField(fillField3DR, 20f, 30f, 10f + ctr * 0.05125f,
                                512f * 0.0625f + 20f, 512f * 0.0625f + 30f, 10f + ctr * 0.05125f,
                                7123, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField3DG,30f, 10f, 20f + ctr * 0.05125f,
                                512f * 0.0625f + 30f, 512f * 0.0625f + 10f, 20f + ctr * 0.05125f,
                                -321, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField3DB, 10f, 20f, 30f + ctr * 0.05125f,
                                512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f,
                                -951, 1f, 0.625f);

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(fillField3DR[0][x][y] * 0.25f + 0.5f, fillField3DG[0][x][y] * 0.25f + 0.5f, fillField3DB[0][x][y] * 0.25f + 0.5f, 1f)
                                ;
                            }
                        }
                        break;
                    case 73:
                        Gdx.graphics.setTitle("Mason 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField3DG[0], 0f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, -12345,  3.5f, 1.08f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, 123456, 2.625f, 1.44f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, -54321, 1.25f, 1.87f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, 654321, 0.625f, 2.19f);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = fillField3DG[0][x][y] * 0.0625f + 0.5f;
                                back[x][y] = 
                                        getGray(bright)
                                ;
                            }
                        }
                        break;
                  
                    case 74:
                        Gdx.graphics.setTitle("Glitch 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        glitch.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                123456) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 75:
                        Gdx.graphics.setTitle("Glitch 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(glitch.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.045, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 76:
                        Gdx.graphics.setTitle("Glitch 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(glitch.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.045, ctr * -0.0375, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 77:
                        Gdx.graphics.setTitle("Glitch 6D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(glitch.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.045, (x + y) * -0.025, (x - ctr) * -0.025, (y - ctr) * -0.025, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 78:
                        Gdx.graphics.setTitle("Merlin Noise 3D, x8 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = ((int)MerlinNoise.noise3D(x, y,
                                        ctr + MerlinNoise.noise2D(x, y, 123456789L, 4, 5), 9000L, 6, 1)) * 255;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;
                    case 79:
                        Gdx.graphics.setTitle("Merlin Noise 3D, x16 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int)(MerlinNoise.noise3D(x, y, ctr, 9000L, 4, 1) * 255);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;

                    case 80:
                        Gdx.graphics.setTitle("Cosmic 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        bright = ctr * 0x5p-8f;
                        s0 = NumberTools.swayRandomized(0x9E3779B97F4A7C15L, bright - 1.11f) * 0.025f; //ctr * 0x5p-8f
                        c0 = NumberTools.swayRandomized(0xC13FA9A902A6328FL, bright - 1.11f) * 0.025f; //ctr * 0x5p-8f
                        s1 = NumberTools.swayRandomized(0xD1B54A32D192ED03L, bright + 1.41f) * 0.025f; //ctr * 0x5p-8f
                        c1 = NumberTools.swayRandomized(0xDB4F0B9175AE2165L, bright + 1.41f) * 0.025f; //ctr * 0x5p-8f
                        s2 = NumberTools.swayRandomized(0xE19B01AA9D42C633L, bright + 2.61f) * 0.025f; //ctr * 0x5p-8f
                        c2 = NumberTools.swayRandomized(0xE60E2B722B53AEEBL, bright + 2.61f) * 0.025f; //ctr * 0x5p-8f
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                connections[0] = ctr * 0.007 + x * c0 - y * s0;
                                connections[1] = ctr * 0.009 - x * c1 + y * s1;
                                connections[2] = ctr * 0.013 + x * c2 + y * s2;
                                
                                connections[0] = cosmos.getDoubleBase() + 0.5;
                                connections[1] = cosmos.getDoubleBase() + 0.5;
                                connections[2] = cosmos.getDoubleBase() + 0.5;
                                back[x][y] = 
                                        floatGet(
                                                NumberTools.swayTight((float)connections[0]), //(float)connections[0] * 4f,
                                                NumberTools.swayTight((float)connections[1]), //(float)connections[1] * 4f,
                                                NumberTools.swayTight((float)connections[2]), //(float)connections[2] * 4f,
                                                1.0f);
                            }
                        }
                        break;
                    case 81:
                        Gdx.graphics.setTitle("Cosmic 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        bright = ctr * 0x5p-8f;
                        s0 = NumberTools.swayRandomized(0x9E3779B97F4A7C15L, bright - 1.11f) * 0.025f; //ctr * 0x5p-8f
                        c0 = NumberTools.swayRandomized(0xC13FA9A902A6328FL, bright - 1.11f) * 0.025f; //ctr * 0x5p-8f
                        s1 = NumberTools.swayRandomized(0xD1B54A32D192ED03L, bright + 1.41f) * 0.025f; //ctr * 0x5p-8f
                        c1 = NumberTools.swayRandomized(0xDB4F0B9175AE2165L, bright + 1.41f) * 0.025f; //ctr * 0x5p-8f
                        s2 = NumberTools.swayRandomized(0xE19B01AA9D42C633L, bright + 2.61f) * 0.025f; //ctr * 0x5p-8f
                        c2 = NumberTools.swayRandomized(0xE60E2B722B53AEEBL, bright + 2.61f) * 0.025f; //ctr * 0x5p-8f
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                connections[0] = ctr * 0.007 + x * c0 - y * s0;
                                connections[1] = ctr * 0.009 - x * c1 + y * s1;
                                connections[2] = ctr * 0.013 + x * c2 + y * s2;
                                connections[0] = cosmos.getDoubleBase() + 0.5;
                                connections[1] = cosmos.getDoubleBase() + 0.5;
                                connections[2] = cosmos.getDoubleBase() + 0.5;
                                bright = NumberTools.swayTight((float)connections[1]);//(float)connections[1] * 4f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 82:
                        Gdx.graphics.setTitle("Whirling 2D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            s0 = (x + ctr) * 0.03125f; // 0.046875f
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(WhirlingNoise.noise(s0, (y + ctr) * 0.03125f, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 83:
                        Gdx.graphics.setTitle("Seeded 2D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            s0 = (x + ctr) * 0.03125f; // 0.046875f
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(SeededNoise.noise(s0, (y + ctr) * 0.03125f, 123456));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 84:
                        Gdx.graphics.setTitle("FastNoise 2D Noise, 15 octaves, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(15);
                        layeredFN.setFrequency(0.03125f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        for (int x = 0; x < width; x++) {
                            s0 = (x + ctr);
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getSimplexFractal(s0, (y + ctr)));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 85:
                        Gdx.graphics.setTitle("FastNoise 2D Noise, 30 octaves, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(30);
                        layeredFN.setFrequency(0.03125f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        for (int x = 0; x < width; x++) {
                            s0 = (x + ctr);
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getSimplexFractal(s0, (y + ctr)));
                                back[x][y] = getGray(bright);
                            }
                        }

//                        Gdx.graphics.setTitle("Whirling 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = basicPrepare(WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 123456)); // , 1.5f
//                                back[x][y] = getGray(bright);
//                            }
//                        }
                        break;
                    case 86:
                        Gdx.graphics.setTitle("Seeded 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(SeededNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 87:
                        Gdx.graphics.setTitle("FastNoise 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(1);
                        layeredFN.setFrequency(0.03125f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getSimplex(x, y, ctr)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 88:
                        Gdx.graphics.setTitle("Whirling 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        dBright = 1.5 * NumberTools.swayRandomized(12345L, 0.03125 * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, dBright, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 89:
                        Gdx.graphics.setTitle("Seeded 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        dBright = 1.5 * NumberTools.swayRandomized(12345L, 0.03125 * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(SeededNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, dBright, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 90:
                        Gdx.graphics.setTitle("FastNoise 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(1);
                        layeredFN.setFrequency(1f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        s0 = 1.5f * NumberTools.swayRandomized(12345, 0.03125f * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getNoise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, s0)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 91:
                        Gdx.graphics.setTitle("Mitchell 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(mitchell.arbitraryNoise(seedX0, x * 0.11125f + ctr * 0.11125f + 20, y * 0.11125f + ctr * 0.11125f + 30.12345f));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 92:
                        Gdx.graphics.setTitle("Mitchell 2D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                basicPrepare(mitchell.arbitraryNoise(seedX0, x * 0.11125f + ctr * 0.11125f + 20, y * 0.11125f + ctr * 0.11125f + 30.12345f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX1, x * 0.11125f + ctr * 0.11125f + 30, y * 0.11125f + ctr * 0.11125f + 10.23456f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX2, x * 0.11125f + ctr * 0.11125f + 10, y * 0.11125f + ctr * 0.11125f + 20.34567f)),
                                                1.0f);
                            }
                        }
                        break;
                    case 93:
                        Gdx.graphics.setTitle("Mitchell 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(mitchell.arbitraryNoise(seedX3, x * 0.11125f + ctr * 0.11125f + 20, y * 0.11125f + ctr * 0.11125f + 30));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 94:
                        Gdx.graphics.setTitle("Mitchell 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                basicPrepare(mitchell.arbitraryNoise(seedX0, x * 0.11125f + 20, y * 0.11125f + 30, ctr * 0.11125f + 10)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX1, x * 0.11125f + 30, y * 0.11125f + 10, ctr * 0.11125f + 20)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX2, x * 0.11125f + 10, y * 0.11125f + 20, ctr * 0.11125f + 30)),
                                                1.0f);
                            }
                        }
                        break;
                    case 95:
                        Gdx.graphics.setTitle("Mitchell 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(mitchell.arbitraryNoise(seedX3, x * 0.11125f + 30, y * 0.11125f + 20, ctr  * 0.11125f + 10));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                    case 96:
                        Gdx.graphics.setTitle("Mitchell 4D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                basicPrepare(mitchell.arbitraryNoise(seedX0, x * 0.11125f + 20, y * 0.11125f + 30, ctr * 0.13125f + 10, NumberTools.sway((x + y + ctr) * 0.0191f) + (x + y + ctr + 31.337) * 0.0311125f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX1, x * 0.11125f + 30, y * 0.11125f + 10, ctr * 0.13125f + 20, NumberTools.sway((x + y + ctr) * 0.0191f) + (x + y + ctr + 42.337) * 0.0311125f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX2, x * 0.11125f + 10, y * 0.11125f + 20, ctr * 0.13125f + 30, NumberTools.sway((x + y + ctr) * 0.0191f) + (x + y + ctr + 53.337) * 0.0311125f)),
                                                1.0f);
                            }
                        }
                        break;
                    case 97:
                        Gdx.graphics.setTitle("Mitchell 4D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(mitchell.arbitraryNoise(seedX3, x * 0.11125f + 30, y * 0.11125f + 20, ctr  * 0.15125f + 10,
                                        NumberTools.sway((x + y + ctr) * 0.0191f) + (x + y + ctr + 31.337) * 0.0311125f));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 98:
                        Gdx.graphics.setTitle("Mitchell 6D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = 
                                        floatGet(
                                                basicPrepare(mitchell.arbitraryNoise(seedX0, x * 0.071125f + 20, y * 0.071125f + 30, ctr * 0.072125f + 10, (NumberTools.sway((x + y) * 0.021f) + ctr + 31.337) * 0.072511125f, (NumberTools.sway((ctr - x) * 0.1681f) + ctr + y + 1.2) * 0.07811125f, (NumberTools.sway((y + ctr) * 0.191828) - ctr + x + 2.8) * 0.07611125f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX1, x * 0.071125f + 30, y * 0.071125f + 10, ctr * 0.072125f + 20, (NumberTools.sway((x + y) * 0.021f) + ctr + 42.337) * 0.072511125f, (NumberTools.sway((ctr - x) * 0.1681f) + ctr + y + 1.6) * 0.07811125f, (NumberTools.sway((y + ctr) * 0.191828) - ctr + x + 2.3) * 0.07611125f)),
                                                basicPrepare(mitchell.arbitraryNoise(seedX2, x * 0.071125f + 10, y * 0.071125f + 20, ctr * 0.072125f + 30, (NumberTools.sway((x + y) * 0.021f) + ctr + 53.337) * 0.072511125f, (NumberTools.sway((ctr - x) * 0.1681f) + ctr + y + 1.4) * 0.07811125f, (NumberTools.sway((y + ctr) * 0.191828) - ctr + x + 2.6) * 0.07611125f)),
                                                1.0f);
                            }
                        }
                        break;
                    case 99:
                        Gdx.graphics.setTitle("Mitchell 6D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(mitchell.arbitraryNoise(seedX3, x * 0.071125f + 20, y * 0.071125f + 30, ctr * 0.072125f + 10, (NumberTools.sway((x + y) * 0.021f) + ctr + 31.337) * 0.072511125f, (NumberTools.sway((ctr - x) * 0.1681f) + ctr + y + 1.2) * 0.07811125f, (NumberTools.sway((y + ctr) * 0.191828) - ctr + x + 2.8) * 0.07611125f));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 100:
                        Gdx.graphics.setTitle("Mitchell 5D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(
                                        basicPrepare(mitchell.arbitraryNoise(seedX0, alter5D(x, y, ctr))),
                                        basicPrepare(mitchell.arbitraryNoise(seedX1, alter5D(x, y, ctr))),
                                        basicPrepare(mitchell.arbitraryNoise(seedX2, alter5D(x, y, ctr))),
                                        1.0f);
                            }
                        }
                        break;
                    case 101:
//                        Gdx.graphics.setTitle("Mitchell 5D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright = basicPrepare(mitchell.arbitraryNoise(0x1337L, alter5D(x, y, ctr)));
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                        break;
                        Gdx.graphics.setTitle("Seeded 5D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter5D(x, y, ctr);
                                bright =
                                        basicPrepare(SeededNoise.noise(point5D[0], point5D[1], point5D[2], point5D[3], point5D[4], seedU0));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 102:
                        Gdx.graphics.setTitle("Sway 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(Noise.Sway2D.instance.getNoiseWithSeed(x * 0.11125f + ctr * 0.11125f + 20, y * 0.11125f + ctr * 0.11125f + 30.12345, seedX0) * 0.50f) + 0.50f;
                                back[x][y] =
                                        floatGet(bright, bright, bright, 1.0f);
                            }
                        }
                        break;
                    case 103:
                        Gdx.graphics.setTitle("Sway 2D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] =
                                        floatGet(
                                                (float)(Noise.Sway2D.instance.getNoiseWithSeed(x * 0.11125f + ctr * 0.11125f + 20, y * 0.11125f + ctr * 0.11125f + 30.12345, seedX0) * 0.50f) + 0.50f,
                                                (float)(Noise.Sway2D.instance.getNoiseWithSeed(x * 0.11125f + ctr * 0.11125f + 30, y * 0.11125f + ctr * 0.11125f + 10.23456, seedX1) * 0.50f) + 0.50f,
                                                (float)(Noise.Sway2D.instance.getNoiseWithSeed(x * 0.11125f + ctr * 0.11125f + 10, y * 0.11125f + ctr * 0.11125f + 20.34567, seedX2) * 0.50f) + 0.50f,
                                                1.0f);
                            }
                        }
                        break;
                    case 104:
                        Gdx.graphics.setTitle("Basic 1D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int i = 0; i < width - 1; i++)
                            System.arraycopy(back[i+1], 0, back[i], 0, width);
                        Arrays.fill(back[almost], FLOAT_WHITE);
                        //if((ctr & 3) == 0)
                        {
                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1,1);
//                            iBright = (int)(NumberTools.swayCubic(ctr * 0x1p-9f) * 0x.fp0 * half);
//                            iBright = (int)(basic1D.getNoise(ctr * 0.015625) * 0x.fp0 * half);
                            iBright = (int)(basic1D.getNoise(ctr * 0.015625) * 0x.fp0 * 0x1p+15f) >> 7;
                            back[width - 1][half - 1 + iBright] =  bright;
                            back[width - 1][half + 0 + iBright] =  bright;
                            back[width - 1][half + 1 + iBright] =  bright;

                            back[width - 2][half - 1 + iBright] =  bright;
                            back[width - 2][half + 0 + iBright] =  bright;
                            back[width - 2][half + 1 + iBright] =  bright;

                            back[width - 3][half - 1 + iBright] =  bright;
                            back[width - 3][half + 0 + iBright] =  bright;
                            back[width - 3][half + 1 + iBright] =  bright;
                        }
                        break;
                    case 105:
                        Gdx.graphics.setTitle("Basic 1D Noise, 30 octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int i = 0; i < width - 1; i++)
                            System.arraycopy(back[i+1], 0, back[i], 0, width);
                        Arrays.fill(back[width - 1], FLOAT_WHITE);
                        //if((ctr & 3) == 0)
                        {
//                            iBright = (int)(NumberTools.sin_(ctr * 0x1p-10f) * 0x.fp0 * half);
//                            iBright = (int)(layered1D.getNoise(ctr * 0.015625) * 0x.fp0 * half);
                            iBright = (int)(layered1D.getNoise(ctr * 0.015625) * 0x.fp0 * 0x1p+15f) >> 7;
                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1,1);
                            back[width - 1][half - 1 + iBright] =  bright;
                            back[width - 1][half + 0 + iBright] =  bright;
                            back[width - 1][half + 1 + iBright] =  bright;

                            back[width - 2][half - 1 + iBright] =  bright;
                            back[width - 2][half + 0 + iBright] =  bright;
                            back[width - 2][half + 1 + iBright] =  bright;

                            back[width - 3][half - 1 + iBright] =  bright;
                            back[width - 3][half + 0 + iBright] =  bright;
                            back[width - 3][half + 1 + iBright] =  bright;
                        }
                        break;
                    case 106:
//                        Gdx.graphics.setTitle("NumberTools.sin() approximation at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int i = 0; i < almost; i++)
//                            System.arraycopy(back[i+1], 0, back[i], 0, 512);
//                        Arrays.fill(back[almost], FLOAT_WHITE);
//                        //if((ctr & 3) == 0)
//                    {
//                        bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1,1);
//                        iBright = (int)(NumberTools.sin(ctr * 0.03125) * 64.0);
//                        back[almost][255 + iBright] =  bright;
//                        back[almost][256 + iBright] =  bright;
//                        back[almost][257 + iBright] =  bright;
//
//                        back[510][255 + iBright] =  bright;
//                        back[510][256 + iBright] =  bright;
//                        back[510][257 + iBright] =  bright;
//
//                        back[509][255 + iBright] =  bright;
//                        back[509][256 + iBright] =  bright;
//                        back[509][257 + iBright] =  bright;
//                    }
//                        Gdx.graphics.setTitle("SwayRandomized 1D Noise Battle, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Gdx.graphics.setTitle("1D noise for terrain, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int i = 0; i < width - 1; i++)
                            System.arraycopy(back[i+1], 0, back[i], 0, width);
                        Arrays.fill(back[width - 1], FLOAT_WHITE);
                        //if((ctr & 3) == 0)
                        {
//                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 0.75f, 1, 1);
//                            iBright = (int) (NumberTools.swayRandomized(0L, ctr * 0x1p-7f) * 240f);
//                            back[almost][255 + iBright] =  bright;
//                            back[almost][256 + iBright] =  bright;
//                            back[almost][257 + iBright] =  bright;
//
//                            back[510][255 + iBright] =  bright;
//                            back[510][256 + iBright] =  bright;
//                            back[510][257 + iBright] =  bright;
//
//                            back[509][255 + iBright] =  bright;
//                            back[509][256 + iBright] =  bright;
//                            back[509][257 + iBright] =  bright;

//                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 0.7f, 1);
//                            iBright = (int) (basic1D.getNoise(ctr * 0x1p-7f) * 240f);
                            iBright = (int)((257+ctr) * 0x1.44cbc89p-8f) % BLUE_GREEN_SERIES.length; 
                            bright = lerpFloatColors(BLUE_GREEN_SERIES[iBright].toFloatBits(),
                                    BLUE_GREEN_SERIES[(iBright + 1) % BLUE_GREEN_SERIES.length].toFloatBits(),
                                    (257+ctr) * 0x1.44cbc89p-8f - (int)((257+ctr) * 0x1.44cbc89p-8f));
                            iBright = (int) (riverSway(0, ctr * 0x3p-9f)  * 0x.fp0f * half);
                            back[width - 1][half - 1 + iBright] =  bright;
                            back[width - 1][half + 0 + iBright] =  bright;
                            back[width - 1][half + 1 + iBright] =  bright;

                            back[width - 2][half - 1 + iBright] =  bright;
                            back[width - 2][half + 0 + iBright] =  bright;
                            back[width - 2][half + 1 + iBright] =  bright;

                            back[width - 3][half - 1 + iBright] =  bright;
                            back[width - 3][half + 0 + iBright] =  bright;
                            back[width - 3][half + 1 + iBright] =  bright;

//                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 0.4f, 1f, 1);
//                            iBright = (int) (swayRandomized3(0, ctr * 0x1p-7f) * 240f);
//                            back[almost][255 + iBright] =  bright;
//                            back[almost][256 + iBright] =  bright;
//                            back[almost][257 + iBright] =  bright;
//
//                            back[510][255 + iBright] =  bright;
//                            back[510][256 + iBright] =  bright;
//                            back[510][257 + iBright] =  bright;
//
//                            back[509][255 + iBright] =  bright;
//                            back[509][256 + iBright] =  bright;
//                            back[509][257 + iBright] =  bright;
                        }
                        break;
                    case 107:
                        Gdx.graphics.setTitle("Quilez1D Noise, 1 octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int i = 0; i < width - 1; i++)
                            System.arraycopy(back[i+1], 0, back[i], 0, width);
                        Arrays.fill(back[width - 1], FLOAT_WHITE);
                        {
                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1, 1);
                            iBright = (int)(quilez1D.getNoise(ctr * 0x1p-7) * 0x.fp0 * half);
                            back[width - 1][half - 1 + iBright] =  bright;
                            back[width - 1][half + 0 + iBright] =  bright;
                            back[width - 1][half + 1 + iBright] =  bright;

                            back[width - 2][half - 1 + iBright] =  bright;
                            back[width - 2][half + 0 + iBright] =  bright;
                            back[width - 2][half + 1 + iBright] =  bright;

                            back[width - 3][half - 1 + iBright] =  bright;
                            back[width - 3][half + 0 + iBright] =  bright;
                            back[width - 3][half + 1 + iBright] =  bright;
                        }
                    break;
                    case 108:
                        Gdx.graphics.setTitle("Quilez1D Noise, 4 octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int i = 0; i < width - 1; i++)
                            System.arraycopy(back[i+1], 0, back[i], 0, width);
                        Arrays.fill(back[width - 1], FLOAT_WHITE);
                    {
                        bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1, 1);
                        iBright = (int)(layeredQuilez1D.getNoise(ctr * 0x1p-7) * 0x.fp0 * half);
                        back[width - 1][half - 1 + iBright] =  bright;
                        back[width - 1][half + 0 + iBright] =  bright;
                        back[width - 1][half + 1 + iBright] =  bright;

                        back[width - 2][half - 1 + iBright] =  bright;
                        back[width - 2][half + 0 + iBright] =  bright;
                        back[width - 2][half + 1 + iBright] =  bright;

                        back[width - 3][half - 1 + iBright] =  bright;
                        back[width - 3][half + 0 + iBright] =  bright;
                        back[width - 3][half + 1 + iBright] =  bright;
                    }

//                        Gdx.graphics.setTitle("swayRandomizedTight() at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int i = 0; i < width - 1; i++)
//                            System.arraycopy(back[i+1], 0, back[i], 0, width);
//                        Arrays.fill(back[width - 1], FLOAT_WHITE);
//                        {
//                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1, 1);
//                            iBright = (int) (swayRandomizedTight(9001L, ctr * 0x1p-8f) * 480f);
//                            back[width - 1][255 + iBright] =  bright;
//                            back[width - 1][256 + iBright] =  bright;
//                            back[width - 1][257 + iBright] =  bright;
//
//                            back[width - 2][255 + iBright] =  bright;
//                            back[width - 2][256 + iBright] =  bright;
//                            back[width - 2][257 + iBright] =  bright;
//
//                            back[width - 3][255 + iBright] =  bright;
//                            back[width - 3][256 + iBright] =  bright;
//                            back[width - 3][257 + iBright] =  bright;
//                        }
                        break;
//                        Gdx.graphics.setTitle("randomWobbleTight() at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int i = 0; i < almost; i++)
//                            System.arraycopy(back[i+1], 0, back[i], 0, 512);
//                        Arrays.fill(back[almost], FLOAT_WHITE);
//                        if((ctr & 3) == 0) {
//                            s0 = (ThrustAltRNG.determineInt(9001) >>> 8) * 0x1.5p-25f + 0.25f;
//                            s1 = (ThrustAltRNG.determineInt(9002) >>> 8) * 0x1.5p-25f + 0.25f;
//                            c0 = (ThrustAltRNG.determineInt(9003) >>> 8) * 0x1.5p-25f + 0.25f;
//                            c1 = (ThrustAltRNG.determineInt(9004) >>> 8) * 0x1.5p-25f + 0.25f;
//                            bright = SColor.floatGetHSV(ctr * 0x1.44cbc89p-8f, 1, 1, 1);
//                            iBright = (int) (randomWobble(ctr * 0.0125f, s0, s1, c0, c1) * 240f);
//                            back[almost][255 + iBright] =  bright;
//                            back[almost][256 + iBright] =  bright;
//                            back[almost][257 + iBright] =  bright;
//
//                            back[510][255 + iBright] =  bright;
//                            back[510][256 + iBright] =  bright;
//                            back[510][257 + iBright] =  bright;
//
//                            back[509][255 + iBright] =  bright;
//                            back[509][256 + iBright] =  bright;
//                            back[509][257 + iBright] =  bright;
//                        }
//                        break;

//                        Gdx.graphics.setTitle("Merlin Rivers 2D, x16 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                iBright = (int)MerlinNoise.noise2D(x + ctr, y + ctr, 9000L, 5, 32);
//                                iBright &= ~((int)MerlinNoise.noise2D(x + ctr + 5, y + ctr, 9000L, 5, 32) - 0x1C00000);
//                                iBright = (iBright >> 8) >>> 24;
//                                back[x][y] = floatGetI(iBright, iBright, iBright);
//                            }
//                        }
//                        break;
                    case 109:
                        Gdx.graphics.setTitle("Quilez2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        layeredQuilez2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                12345678987654321L) * 0.5 + 0.5);
                                back[x][y] = getGray(bright);
                            }
                        }

//                        Gdx.graphics.setTitle("Hilbert Curve on swayRandomizedTight(), at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < 256; x++) {
//                            for (int y = 0; y < 256; y++) {
//                                bright = swayRandomizedTight(1337L, ctr * 0.05f + 0.0131415f * ((CoordPacker.hilbertDistances[x | y << 8] & 0xFFFF)));
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                        if(width >= 512) {
//                            for (int x = 0; x < 256; x++) {
//                                for (int y = 0; y < 256; y++) {
//                                    bright = swayRandomizedTight(1337L, ctr * 0.05f + 0.0131415f * (0x10000 + (CoordPacker.hilbertDistances[y | x << 8] & 0xFFFF)));
//                                    back[x][y + 256] = getGray(bright);
//                                }
//                            }
//                            for (int x = 0; x < 256; x++) {
//                                for (int y = 0; y < 256; y++) {
//                                    bright = swayRandomizedTight(1337L, ctr * 0.05f + 0.0131415f * (0x20000 + (CoordPacker.hilbertDistances[y | x << 8] & 0xFFFF)));
//                                    back[x + 256][y + 256] = getGray(bright);
//                                }
//                            }
//                            for (int x = 0; x < 256; x++) {
//                                for (int y = 0; y < 256; y++) {
//                                    bright = swayRandomizedTight(1337L, ctr * 0.05f + 0.0131415f * (0x30000 + (CoordPacker.hilbertDistances[255 - x | 255 - y << 8] & 0xFFFF)));
//                                    back[x + 256][y] = getGray(bright);
//                                }
//                            }
//                        }
                        break;
//                        Gdx.graphics.setTitle("Merlin Rivers 3D, x16 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                iBright = (int)MerlinNoise.noise3D(x, y, ctr, 9000L, 5, 32);
//                                iBright ^= iBright - 0x8000000;
//                                iBright = (iBright >> 8) >>> 24;
//                                back[x][y] = floatGetI(iBright, iBright, iBright);
//                            }
//                        }
//                        break;
                    case 110:
                        Gdx.graphics.setTitle("Experimental Noise 2D, 1 octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = foamNoise(-999999, (x + ctr) * 0.03125f, (y + ctr) * 0.03125f);
//                                bright = prepare((
//                                        beachNoise(-999999, (x + ctr) * 0.03125f, (y + ctr) * 0.03125f) +
//                                        beachNoise(9999, (y + ctr) * 0.03125f - 1.618f, (x + ctr) * 0.03125f - 1.618f)) * 0.375f
//                                );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 111:
                        Gdx.graphics.setTitle("Value Noise 2D, 1 octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
//                                bright = basicPrepare(beachNoise(-999999L, x * 0.0625f, y * 0.0625f, ctr * 0.0625f)); //0.61803398875
                                bright = (float) (ValueNoise.valueNoise(0, (x + ctr) * 0.03125f, (y + ctr) * 0.03125f));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 112:
                        Gdx.graphics.setTitle("Value Noise 3D, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (ValueNoise.valueNoise(0, x * 0.03125f, y * 0.03125f, ctr * 0.03125f)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 113:
                        Gdx.graphics.setTitle("Seeded 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredSeeded.getNoiseWithSeed(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 114:
                        Gdx.graphics.setTitle("FastNoise 3D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(1);
                        layeredFN.setFrequency(1f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        c1 = ctr * 0.045f;
                        for (int x = 0; x < width; x++) {
                            c0 = x * 0.03125f;
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getNoise(c0, y * 0.03125f, c1)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 115:
                        Gdx.graphics.setTitle("Whirling 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        dBright = 1.5 * NumberTools.swayRandomized(12345L, 0.03125 * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(WhirlingNoise.instance.getNoiseWithSeed(x * 0.03125f, y * 0.03125f, ctr * 0.045f, dBright, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 116:
                        Gdx.graphics.setTitle("Seeded 4D Noise, 1 octave, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        dBright = 1.5 * NumberTools.swayRandomized(12345L, 0.03125 * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(SeededNoise.instance.getNoiseWithSeed(x * 0.03125f, y * 0.03125f, ctr * 0.045f, dBright, 123456)); // , 1.5f
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 117:
                        Gdx.graphics.setTitle("FastNoise 4D Noise, 5 octaves, at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        layeredFN.setNoiseType(FastNoise.SIMPLEX_FRACTAL);
                        layeredFN.setSeed(123456);
                        layeredFN.setFractalOctaves(5);
                        layeredFN.setFrequency(1f);
                        layeredFN.setFractalLacunarity(2f);
                        layeredFN.setFractalGain(0.5f);
                        s0 = 1.5f * NumberTools.swayRandomized(12345, 0.03125f * ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(layeredFN.getNoise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, s0));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 118:
                        Gdx.graphics.setTitle("Classic 2D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright =
                                        basicPrepare(ClassicNoise.instance.getNoise(xx * 0.025, yy * 0.025)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 120:
                        Gdx.graphics.setTitle("Classic 3D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(ClassicNoise.instance.getNoise(x * 0.025, y * 0.025, ctr * 0.03125)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 122:
                        Gdx.graphics.setTitle("Classic 4D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(ClassicNoise.instance.getNoise(x * 0.015 - y * 0.01625, ctr * 0.0225 - x * 0.01625, y * 0.015 - ctr * 0.0225, x * 0.0225 + y * 0.02 + ctr * 0.01625)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 124: {
                        Gdx.graphics.setTitle("Classic 6D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        double u = NumberTools.swayRandomized(123456L, ctr * -0.04),
                                v = NumberTools.swayRandomized(-987654321L, ctr * -0.013 + 0.3) * 1.5,
                                w = NumberTools.swayRandomized(543212345L, ctr * -0.02 + 0.7) * 1.25;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(ClassicNoise.instance.getNoise(x * 0.03125, y * 0.03125, ctr * 0.045, u + x * 0.015, v - y * 0.014, w - ctr * 0.021)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    }
                    case 119:
                        Gdx.graphics.setTitle("Jitter 2D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright =
                                        basicPrepare(JitterNoise.instance.getNoise(xx * 0.025, yy * 0.025)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 121:
                        Gdx.graphics.setTitle("Jitter 3D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(JitterNoise.instance.getNoise(x * 0.025, y * 0.025, ctr * 0.03125)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 123:
                        Gdx.graphics.setTitle("Jitter 4D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(JitterNoise.instance.getNoise(x * 0.015 - y * 0.01625, ctr * 0.0225 - x * 0.01625, y * 0.015 - ctr * 0.0225, x * 0.0225 + y * 0.02 + ctr * 0.01625)
                                        );
                                //+ 15f) / 30f;
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 125:
                    {
                        Gdx.graphics.setTitle("Classic5D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond() + " FPS");
//                        double w = NumberTools.swayRandomized(123456L, ctr * -0.028),
//                                u = NumberTools.swayRandomized(-987654321L, ctr * -0.017 + 0.3);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter5D(x, y, ctr);
                                bright = 
                                        basicPrepare(ClassicNoise.instance.getNoise(point5D[0], point5D[1], point5D[2], point5D[3], point5D[4]));
//                                        basicPrepare(ClassicNoise.instance.getNoise(x * 0.03125, y * 0.03125, ctr * 0.04, w * 0.025, u * 0.024));
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    }
//                        {
//                        Gdx.graphics.setTitle("Jitter 6D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond() + " FPS");
//                        double u = NumberTools.swayRandomized(123456L, ctr * -0.04),
//                                v = NumberTools.swayRandomized(-987654321L, ctr * -0.013 + 0.3) * 1.5,
//                                w = NumberTools.swayRandomized(543212345L, ctr * -0.02 + 0.7) * 1.25;
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                bright =
//                                        basicPrepare(JitterNoise.instance.getNoise(x * 0.03125, y * 0.03125, ctr * 0.045, u + x * 0.015, v - y * 0.014, w - ctr * 0.021)
//                                        );
//                                //+ 15f) / 30f;
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                    }
//                    break;
                    case 126:
                        Gdx.graphics.setTitle("Whirling 2D YCbCr Noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                back[x][y] = floatGetYCbCr(
                                        ((float) WhirlingNoise.noise(xx * 0.03125f + 0.1f, yy * 0.03125f + 0.4f, 1234567L) + 1f) * 0.5f,
                                        ((float) WhirlingNoise.noise(xx * 0.03125f + 0.4f, yy * 0.03125f + 0.7f, 7654321L)) * 0.45f,
                                        ((float) WhirlingNoise.noise(xx * 0.03125f + 0.7f, yy * 0.03125f + 0.1f, 9999999L)) * 0.45f,
                                        1f);
                            }
                        }
                        break;
                    case 127:
                        Gdx.graphics.setTitle("Jitter 2D YCbCr Noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                back[x][y] = floatGetYCbCr(
                                        ((float) JitterNoise.instance.getNoiseWithSeed(xx * 0.025f + 0.1f, yy * 0.025f + 0.4f, 1234567L) + 1f) * 0.5f,
                                        ((float) JitterNoise.instance.getNoiseWithSeed(xx * 0.025f + 0.4f, yy * 0.025f + 0.7f, 7654321L)) * 0.45f,
                                        ((float) JitterNoise.instance.getNoiseWithSeed(xx * 0.025f + 0.7f, yy * 0.025f + 0.1f, 9999999L)) * 0.45f,
                                        1f);
                            }
                        }
                        break;

                    case 128:
                        Gdx.graphics.setTitle("Whirling 3D YCbCr Noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGetYCbCr(
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 1234567L) + 1f) * 0.5f,
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f + 234.5, 7654321L)) * 0.45f,
                                        ((float) WhirlingNoise.noise(x * 0.03125f, y * 0.03125f, ctr * 0.045f + 678.9, 9999999L)) * 0.45f,
                                        1f);
                            }
                        }
                        break;
                    case 129:
                        Gdx.graphics.setTitle("Classic 3D YCbCr Noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGetYCbCr(
                                        ((float) ClassicNoise.instance.getNoiseWithSeed(x * 0.025f, y * 0.025f, ctr * 0.03125f, 1234567L) + 1f) * 0.5f,
                                        ((float) ClassicNoise.instance.getNoiseWithSeed(x * 0.025f, y * 0.025f, ctr * 0.03125f + 234.5, 7654321L)) * 0.45f,
                                        ((float) ClassicNoise.instance.getNoiseWithSeed(x * 0.025f, y * 0.025f, ctr * 0.03125f + 678.9, 9999999L)) * 0.45f,
                                        1f);
                            }
                        }
                        break;
                    case 130:
                        Gdx.graphics.setTitle("Blue Noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (BlueNoise.get(xx, yy) + 128) / 255f;
                                back[x][y] = floatGet(bright, bright, bright, 1f);
                            }
                        }
                        break;
                    case 131:
                        Gdx.graphics.setTitle("Blue Noise with scratchy adjust " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                //iBright = 12345789 ^ (xx >>> 6) * 0x1827F5 ^ (yy >>> 6) * 0x123C21;
//                                iBright = (((BlueNoise.get(xx, yy) & 0xE0) 
//                                        ^ (BlueNoise.get(yy, xx, BlueNoise.ALT_NOISE[((12345789 ^ (xx >>> 6) * 0x1827F5 ^ (yy >>> 6) * 0x123C21) >>> 16 & 62)]) >>> 3 & 0x1F)
                                iBright = ((BlueNoise.getSeeded(xx, yy,  12345789)
                                        //((iBright ^ (iBright << 19 | iBright >>> 13) ^ (iBright << 5 | iBright >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 20 & 0x3F)
                                        //^ (xx + xx + yy >> 2 & 0x3F) ^ (xx - yy - yy >> 2 & 0x3F)
                                ) + 128);

//                                bright = //(BlueNoise.getSeeded(xx, yy, 12345789) + 128) / 255f;
//                                        ((BlueNoise.get(xx, yy) ^ 128 ^ Noise.IntPointHash.hash64(xx >>> 6, yy >>> 6, 1234567)
//                                        ^ (xx + yy >> 2 & 0x3F) ^ (xx - yy >> 2 & 0x3F)) & 255) / 255f;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;
                    case 132:
                        Gdx.graphics.setTitle("Blue Noise with choice " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
//                            xx = x + (ctr >> 2);
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
//                                yy = y + (ctr >> 2);
                                yy = y + ctr;
//                                final int xs = xx & 63, ys = yy & 63;
//                                //final int hash = BlueNoise.ALT_NOISE[Noise.IntPointHash.hash64((xx >>> 5 & -2) + ((xx & 32) >>> 4) - 1, yy >>> 6, ~12345678)][(yy << 6 & 0xFC0) | (xx & 0x3F)] + 128;
//                                iBright = BlueNoise.ALT_NOISE[Noise.IntPointHash.hash64(xx >>> 6, yy >>> 6, 12345678)][(ys << 6) | (xs)] + 128;
//                                if((32 - xs ^ 32 - xs >> 31) > (iBright & 1) + 31 || (32 - ys ^ 32 - ys >> 31) > (iBright & 1) + 31)
//                                    iBright = BlueNoise.ALT_NOISE[Noise.IntPointHash.hash64((xx >>> 6) + ((xx & 32) >>> 4) - 1, (yy >>> 6) + ((yy & 32) >>> 4) - 1, 12345678)][(ys << 6) | (xs)] + 128;
                                iBright = (BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[ctr & 63]) + 128);
                                
                                
                                
                                
//                                a = BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[Noise.IntPointHash.hash32(xx >>> 6, yy >>> 6, 1122334455) & 15]) + 128;
//                                b = BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[(Noise.IntPointHash.hash32(xx - 32 >>> 6, yy - 32 >>> 6, 1122334455) & 15) | 16]) + 128;
//                                ad = Math.abs((xx << 1 & 126) - 63) + Math.abs((yy << 1 & 126) - 63);
////                                ad = Math.sqrt(ad * ad + bd * bd) * 0.031746031746031744;
//                                //bd = Math.max(0.0, ad - 1.0);
////                                bright = (128 + BlueNoise.getSeeded(xx, yy, 12345679, BlueNoise.ALT_NOISE[ctr >> 2 & 15])) / 255f;
////                                iBright = ((BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[32]) + 128 >>> 2 >= (yy & 63)) ? Math.min(a, b) : Math.max(a, b)) + 128;
////                                iBright = (a + b + 256) >>> 1;
////                                iBright = (((BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[32]) < 0) ? Math.min(a, b) : Math.max(a, b))) + 128;
//                                iBright = ((ad + 2 >>> 2 == 16//(Math.abs(ad - 63) <= 1) 
//                                        //? (BlueNoise.get(yy, xx, BlueNoise.ALT_NOISE[32]) + 128) 
//                                        ? (a + b + 1) >>> 1//((BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[32]) < 0) ? Math.min(a, b) : Math.max(a, b))
////                                        ? (a * (79 - ad)) + (b * (ad - 47)) >>> 5// ^ (BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[32]) & 0xF)
//                                        : ad <= 63 ? a : b));
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;
                    case 133:
//                        Gdx.graphics.setTitle("Blue Noise with choice " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        for (int x = 0; x < width; x++) {
//                            xx = (x + ctr);
//                            for (int y = 0; y < height; y++) {
//                                yy = (y + ctr);
//                                bright = (BlueNoise.get(xx, yy, BlueNoise.ALT_NOISE[ctr >>> 5 & 15]) + 128) / 255f;
//                                back[x][y] = getGray(bright);
//                            }
//                        }
//                        break;
                        Gdx.graphics.setTitle("Blue Noise with changing scratchy adjust " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                iBright = BlueNoise.getSeeded(xx, yy, ctr) + 128;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        break;
                    case 134:
                        Gdx.graphics.setTitle("Foam 2D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter2D(x, y, ctr);
                                bright =
                                        basicPrepare(foam2D_1.getNoise(point2D[0], point2D[1])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 135:
                        Gdx.graphics.setTitle("Foam 2D Noise, unprocessed, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter2D(x, y, ctr);
                                bright =
                                        basicPrepare(foam2D_2.getNoise(point2D[0], point2D[1])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 136:
                        Gdx.graphics.setTitle("Foam 3D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter3D(x, y, ctr);
                                bright =
                                        basicPrepare(foam3D_1.getNoise(point3D[0], point3D[1], point3D[2])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 137:
                        Gdx.graphics.setTitle("Foam3D Noise, unprocessed, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter3D(x, y, ctr);
                                bright =
                                        basicPrepare(foam3D_2.getNoise(point3D[0], point3D[1], point3D[2])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 138:
                        Gdx.graphics.setTitle("Foam 4D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point4D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point4D, x, y, ctr);
                                bright =
                                        basicPrepare(foam4D_1.getNoise(point4D[0], point4D[1], point4D[2], point4D[3])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 139:
                        Gdx.graphics.setTitle("Foam 4D Noise, unprocessed, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point4D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point4D, x, y, ctr);
                                bright =
                                        basicPrepare(foam4D_2.getNoise(point4D[0], point4D[1], point4D[2], point4D[3])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 140:
                        Gdx.graphics.setTitle("Foam 6D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point6D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point6D, x, y, ctr);
                                bright =
                                        basicPrepare(foam6D_1.getNoise(point6D[0], point6D[1], point6D[2], point6D[3], point6D[4], point6D[5])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 141:
                        Gdx.graphics.setTitle("Foam 6D Noise, unprocessed, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point6D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point6D, x, y, ctr);
                                bright =
                                        basicPrepare(foam6D_2.getNoise(point6D[0], point6D[1], point6D[2], point6D[3], point6D[4], point6D[5])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 142:
                        Gdx.graphics.setTitle("Phantom 2D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(phantom2D.getNoise(alter2D(x, y, ctr))
//                                        basicPrepare(phantom2D.getNoise(point2D[0], point2D[1])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 143:
                        Gdx.graphics.setTitle("Phantom 3D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright =
                                        basicPrepare(phantom3D.getNoise(alter3D(x, y, ctr))
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 144:
                        Gdx.graphics.setTitle("Phantom 4D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point4D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point4D, x, y, ctr);
                                bright =
                                        basicPrepare(phantom4D.getNoise(point4D)
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 145:
                        Gdx.graphics.setTitle("Phantom 5D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point5D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point5D, x, y, ctr);
                                bright =
                                        basicPrepare(phantom5D.getNoise(point5D)
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 146:
                        Gdx.graphics.setTitle("Phantom 6D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point6D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point6D, x, y, ctr);
                                bright =
                                        basicPrepare(phantom6D.getNoise(point6D)
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 147:
                        Gdx.graphics.setTitle("Phantom 7D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        Arrays.fill(point7D, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                slice3D(point7D, x, y, ctr);
                                bright =
                                        basicPrepare(phantom7D.getNoise(point7D)
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 148:
                        Gdx.graphics.setTitle("Warble3D Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter3D(x + ctr, y + ctr, ctr + ctr);
                                bright =
                                        basicPrepare(warble3D.getNoise(point3D[0], point3D[1], point3D[2])
                                        );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;
                    case 149:
                        Gdx.graphics.setTitle("Warble3D Noise, color, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                alter3D(x + ctr, y + ctr, ctr + ctr);
                                warble3D.getNoise(point3D[0], point3D[1], point3D[2]);
                                back[x][y] = floatGet(basicPrepare(warble3D.results[0]),
                                        basicPrepare(warble3D.results[1]), basicPrepare(warble3D.results[2]), 1f);
                            }
                        }
                        break;
                    case 150:
                        Gdx.graphics.setTitle("Triple Star Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare(fast1_2D.getNoiseWithSeed(x, y, 123))
                                        + basicPrepare(fast1_2D.getNoiseWithSeed(ctr, x, 12345))
                                        + basicPrepare(fast1_2D.getNoiseWithSeed(y, ctr, 1234567))
                                        > 2.5 ? FLOAT_WHITE : FLOAT_BLACK;
                                back[x][y] = bright;
                            }
                        }
                        break;
                    case 151:
                        Gdx.graphics.setTitle("Triangular White Noise, unprocessed, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = basicPrepare((fastWhite_2D.getNoiseWithSeed(x & -16, y & -16, ctr & -16, 123)
                                        + fastWhite_2D.getNoiseWithSeed(x & -16, y & -16, ctr & -16, 1234567)) * 0.5
                                );
                                back[x][y] = getGray(bright);
                            }
                        }
                        break;

                }
            }
            break;
            case 5: { //RNG mode
                switch (rngMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = jreRandom.nextInt() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = mm64.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("MiniMover64RNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = light.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = silk.nextInt() | 255;
                                back[x][y] = floatGet(iBright);
                            }
                        }
                        Gdx.graphics.setTitle("SilkRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
//                                back[x][y] = floatGet((randomGrid[x][y].nextLong() >> 63) | 255L);
                                back[x][y] = floatGet(-(randomGrid[x][y].nextLong() & 1L) | 255L);
//                                back[x][y] = floatGet(randomGrid[x][y].nextLong() << 8 | 255L);
                            }
                        }
                        Gdx.graphics.setTitle("Tangle Stream Grid at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
//                        for (int x = 0; x < width; x++) {
//                            for (int y = 0; y < height; y++) {
//                                code = xoRo.nextLong() | 255L;
//                                back[x][y] = floatGet(code);
//                            }
//                        }
//                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
//                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = permuted.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = longPeriod.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = isaac.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = gdxRandom.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from libGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = jreRandom.nextInt() >>> 24;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = mm64.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("MiniMover64RNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = light.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = silk.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("SilkRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = xoRo.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = permuted.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = longPeriod.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = isaac.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(gdxRandom.nextLong() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from libGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(diver.nextLong() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("DiverRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = diver.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("DiverRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(orbit.nextLong() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("OrbitRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = orbit.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("OrbitRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(br64.nextLong() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom64 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (br64.nextInt() & 0xFF);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom64 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(br32.nextInt() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom32 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (br32.nextInt() & 0xFF);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom32 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(tangle.nextLong() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("TangleRNG (LSB only) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = tangle.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("TangleRNG (8 bits via next()) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 28:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = xoRo.nextLong() << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 29:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int) (xoRo.nextLong() & 255);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 30:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = ta.nextLong() << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 31:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int) (ta.nextLong() & 255);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 32:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = basic64.nextLong() | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom64 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 33:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = basic64.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("BasicRandom64 at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 34:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = xosh.nextLong() << 8 | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("XoshiroStarPhi32RNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 35:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int) (xosh.nextLong() & 255);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("XoshiroStarPhi32RNG (low bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 36:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = -(nlfsr.nextInt() & 1L) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        Gdx.graphics.setTitle("NLFSR at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 37:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = nlfsr.nextInt() & 255;
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("NLFSR at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 38:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(-(linnorm.nextLong() & 1L) | 255L);
                            }
                        }
                        Gdx.graphics.setTitle("LinnormRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 39:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = linnorm.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("LinnormRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 40:
                        code = LinnormRNG.determine(System.nanoTime());
                        for (int x = 0; x < width; x++) {
                            extra = LinnormRNG.determine(code + x);
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(LinnormRNG.determine(extra + y) >>> 32 | 255);
                            }
                        }
                        Gdx.graphics.setTitle("LinnormRNG (determine(), highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 41:
                        code = LinnormRNG.determine(System.nanoTime());
                        for (int x = 0; x < width; x++) {
                            extra = LinnormRNG.determine(code + x);
                            for (int y = 0; y < height; y++) {
                                iBright = (int)(LinnormRNG.determine(extra + y) >>> 56);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("LinnormRNG (determine(), highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 42:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(-(ta.nextLong() & 1L) | 255L);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 43:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = ta.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 44:
                        code = System.nanoTime();
                        for (int x = 0; x < width; x++) {
                            extra = ThrustAltRNG.determine(code + x);
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(ThrustAltRNG.determine(extra + y) >>> 32 | 255);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG color (determine(), highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 45:
                        code = System.nanoTime();
                        for (int x = 0; x < width; x++) {
                            extra = ThrustAltRNG.determine(code + x);
                            for (int y = 0; y < height; y++) {
                                iBright = (int)(ThrustAltRNG.determine(extra + y) >>> 56);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("ThrustAltRNG gray (determine(), highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 46:
                        extra = System.nanoTime();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = floatGet(PointHash.hashAll(x, y, extra) >>> 32 | 255);
                            }
                        }
                        Gdx.graphics.setTitle("PointHash color (highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 47:
                        extra = System.nanoTime();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = PointHash.hash256(x, y, extra);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("PointHash gray (highest bits) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 48:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = starfish.nextInt() | 255;
                                back[x][y] = floatGet(iBright);
                            }
                        }
                        Gdx.graphics.setTitle("Starfish32RNG using higher bits at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 49:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = starfish.next(8);
                                back[x][y] = floatGetI(iBright, iBright, iBright);
                            }
                        }
                        Gdx.graphics.setTitle("Starfish32RNG using next(8) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 50: {
                        // all nextDouble() implementations here have the LSB of the mantissa set half as frequently as
                        // a uniform distribution would produce. But, it's mostly java.util.Random that has this strange
                        // issue... Everything else looks like uniform white noise, with no stripes.
                        // java.util.Random looks like https://i.imgur.com/ELw3W5p.gif
                        float counter = 0f;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                counter += (bright = (Double.doubleToLongBits(jreRandom.nextDouble()) & 1L));
                                back[x][y] = floatGet(bright, bright, bright, 1f);
                            }
                        }
                        Gdx.graphics.setTitle(counter+"/262144 from j.u.Random using nextDouble() at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        break;
                    }
                    case 51: {
                        float counter = 0f;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                counter += (bright = (Double.doubleToLongBits(gdxRandom.nextDouble()) & 1L));
                                back[x][y] = floatGet(bright, bright, bright, 1f);
                            }
                        }
                        Gdx.graphics.setTitle(counter+"/262144 from RandomXS128 using nextDouble() at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        break;
                    }
                }
            }
            break;
            case 2: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = yolkA.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = yolkA.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = yolkA.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = yolkA.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = yolkA.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = yolkA.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = yolkA.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = yolkA.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                back[x][y] = floatGet(code);
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                }
            }
            break;
            default: {
                switch (otherMode) {
                    case 0: {
                        Gdx.graphics.setTitle("Conway's Game Of Life at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        ArrayTools.fill(back, FLOAT_BLACK);
                        for (Coord c : ca.current) {
                            back[c.x][c.y] = FLOAT_WHITE;
                        }
                        ca.runGameOfLife();
                    }
                    break;
                    case 1:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 2D Noise, 1 normal octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = smoothPrepare(fastPerlin1.getConfiguredNoise(xx, yy));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 2:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 2D Noise, 1 ridged octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = smoothPrepare(fastPerlinRidged1.getConfiguredNoise(xx, yy));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 3:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 2D Noise, 3 normal octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = smoothPrepare(fastPerlin3.getConfiguredNoise(xx, yy));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 4:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 2D Noise, 3 ridged octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = smoothPrepare(fastPerlinRidged3.getConfiguredNoise(xx, yy));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 5:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 3D Noise, 1 normal octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin1.getConfiguredNoise(x, y, ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 6:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 3D Noise, 1 ridged octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged1.getConfiguredNoise(x, y, ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 7:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 3D Noise, 3 normal octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin3.getConfiguredNoise(x, y, ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 8:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 3D Noise, 3 ridged octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged3.getConfiguredNoise(x, y, ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 9:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 4D Noise, 1 normal octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        s0 = ctr;
                        for (int x = 0; x < width; x++) {
                            s1 = s0 * -0.3333333333333333f + x * 0.9428090415820634f;
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin1.getConfiguredNoise(s0, s1,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * 0.816496580927726f,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * -0.816496580927726f
                                ));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 10:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 4D Noise, 1 ridged octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        s0 = ctr;
                        for (int x = 0; x < width; x++) {
                            s1 = s0 * -0.3333333333333333f + x * 0.9428090415820634f;
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged1.getConfiguredNoise(s0, s1,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * 0.816496580927726f,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * -0.816496580927726f
                                ));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 11:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 4D Noise, 3 normal octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        s0 = ctr;
                        for (int x = 0; x < width; x++) {
                            s1 = s0 * -0.3333333333333333f + x * 0.9428090415820634f;
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin3.getConfiguredNoise(s0, s1,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * 0.816496580927726f,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * -0.816496580927726f
                                ));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 12:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 4D Noise, 3 ridged octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        s0 = ctr;
                        for (int x = 0; x < width; x++) {
                            s1 = s0 * -0.3333333333333333f + x * 0.9428090415820634f;
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged3.getConfiguredNoise(s0, s1,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * 0.816496580927726f,
                                        s0 * -0.3333333333333333f + x * -0.4714045207910317f + y * -0.816496580927726f
                                ));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 13:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 6D Noise, 1 normal octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin1.getNoise(x, y, ctr, -x, -y, -ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 14:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 6D Noise, 1 ridged octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged1.getNoise(x, y, ctr, -x, -y, -ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 15:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 6D Noise, 3 normal octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlin3.getNoise(x, y, ctr, -x, -y, -ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 16:
                    {
                        Gdx.graphics.setTitle("Classic Perlin 6D Noise, 3 ridged octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = smoothPrepare(fastPerlinRidged3.getNoise(x, y, ctr, -x, -y, -ctr));
                                back[x][y] = getGray(bright);
                            }
                        }
                    }
                    break;
                    case 17:
                    {
                        Gdx.graphics.setTitle("Seamless Foam Noise, 3 octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        if(keepGoing) {
                            ++foamUsed.seed;
                            fastUsed.setSeed(fastUsed.getSeed()+1);
                        }
                        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                back[x][y] = basicPrepare(seamlessFoam.getNoise(x, y));
                                min = Math.min(back[x][y], min);
                                max = Math.max(back[x][y], max);
                            }
                        }
                        float range = 1f / (max - min);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
//                                back[x][y] = getGray(back[x][y]);
                                back[x][y] = getGray((back[x][y] - min) * range);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private final double[]
            point2D = new double[]{12345.6789, 23451.6789},
            point3D = new double[]{12345.6789, 23451.6789, 34512.6789},
            point4D = new double[]{12345.6789, 23451.6789, 34512.6789, 45123.6789},
            point5D = new double[]{12345.6789, 23451.6789, 34512.6789, 45123.6789, 51234.6789},
            point6D = new double[]{12345.6789, 23451.6789, 34512.6789, 45123.6789, 51234.6789, 92345.6781},
            point7D = new double[]{12345.6789, 23451.6789, 34512.6789, 45123.6789, 51234.6789, 92345.6781, 89345.6712};
    private static void slice2D(double[] points, int x, int y, int ctr) {
        points[0]  = x * 0.03125 + ctr * 0.1375;
        points[1]  = y * 0.03125 - ctr * 0.1375;
    }
    private static void slice3D(double[] points, int x, int y, int ctr) {
        points[0] = x * 0.03125;
        points[1] = y * 0.03125;
        points[2] = ctr * 0.1375;
        for (int i = 3; i < points.length; i++) {
            points[i] = -points[i - 3];
        }
    }
    private double[] alter2D(int x, int y, int ctr) {
        point2D[0]  = x * 0.03125 + ctr * 0.1375;
        point2D[1]  = y * 0.03125 - ctr * 0.1375;
        return point2D;
    }

    private double[] alter3D(int x, int y, int ctr) {
        point3D[0]  = x * 0.03125;
        point3D[1]  = y * 0.03125;
        point3D[2]  = ctr * 0.1375;
//        point3D[0]  = (y * 0.6 + x) * 0.02625f + ctr * 0.03125f;
//        point3D[1]  = (x * 0.6 - y) * 0.01625f + ctr * 0.05125f;
//        point3D[2]  = (x * 0.8 + y * 0.5) * 0.02125f - ctr * 0.04625f;
        return point3D;
    }
    private double[] alter4D(int x, int y, int ctr) {
        point4D[0]  = (y * 0.6 + x) * 0.03125 + ctr * 0.1375;
        point4D[1]  = (x * 0.6 - y) * 0.03125 + ctr * 0.1375;
        point4D[2]  = (x * 0.8 + y * 0.5) * 0.03125 - ctr * 0.1375;
        point4D[3]  = (y * 0.8 - x * 0.5) * 0.03125 - ctr * 0.1375;
        return point4D;
    }
    private double[] alter5D(int x, int y, int ctr) {
        point5D[0] = (y * 0.6 + x * 0.4) * 0.03125 + ctr * 0.05375;
        point5D[1] = (x * 0.6 - y * 0.4) * 0.03125 + ctr * 0.05375;
        point5D[2] = (x * 0.7 + y * 0.3) * 0.03125 - ctr * 0.05375;
        point5D[3] = (y * 0.7 - x * 0.3) * 0.03125 - ctr * 0.05375;
        point5D[4] = (x * 0.35 - y * 0.25) * 0.02125 - (x * 0.25 + y * 0.35) * 0.04125;
        return point5D;
    }
    private double[] alter6D(int x, int y, int ctr) {
        point6D[0]  = (y * 0.6 + x) * 0.03125 + ctr * 0.1375;
        point6D[1]  = (x * 0.6 - y) * 0.03125 + ctr * 0.1375;
        point6D[2]  = (x * 0.8 + y * 0.5) * 0.03125 - ctr * 0.1375;
        point6D[3]  = (y * 0.8 - x * 0.5) * 0.03125 - ctr * 0.1375;
        point6D[4]  = (ctr * 0.35 + x * 0.5 - y * 0.3) * 0.03125 + ctr * 0.1375;
        point6D[5]  = (ctr * 0.35 + y * 0.5 - x * 0.3) * 0.03125 - ctr * 0.1375;
        return point6D;
    }
    private double[] alter7D(int x, int y, int ctr) {
        point7D[0]  = (y * 0.6 + x) * 0.03125 + ctr * 0.1375;
        point7D[1]  = (x * 0.6 - y) * 0.03125 + ctr * 0.1375;
        point7D[2]  = (x * 0.8 + y * 0.5) * 0.03125 - ctr * 0.1375;
        point7D[3]  = (y * 0.8 - x * 0.5) * 0.03125 - ctr * 0.1375;
        point7D[4]  = (ctr * 0.35 + x * 0.5 - y * 0.3) *   0.03125 + ctr * 0.1375;
        point7D[5]  = (ctr * 0.41 + y * 0.5 - x * 0.3) *   0.03125 - ctr * 0.1375;
        point7D[6]  = (ctr * 0.31 + x * 0.45 + y * 0.45) * 0.03125 - ctr * 0.1375;
        //point7D[7]  = (x * 0.7 + y * 0.4) * 0.02125f - ctr * 0.05125f;
        //point7D[8]  = (y * 0.7 - x * 0.4) * 0.02625f - ctr * 0.04125f;
        //point7D[9]  = (ctr * 0.55 + x * 0.3 - y * 0.2) * 0.04125f + ctr * 0.02625f;
        //point7D[10] = (ctr * 0.55 + y * 0.3 - x * 0.2) * 0.06125f - ctr * 0.01625f;
        //point7D[11] = (ctr * 0.45 + x * 0.3 + y * 0.3) * 0.05125f - ctr * 0.02125f;
        return point7D;
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        if (testType == 3 || keepGoing) {
            ctr++;
            putMap();
        }
        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
        batch.begin();
        tcf.draw(batch, back, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: Hash Visualization");
        config.setWindowedMode(width, height);
        config.useVsync(true);
        config.setForegroundFPS(2);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new HashVisualizer(), config);
    }
}
