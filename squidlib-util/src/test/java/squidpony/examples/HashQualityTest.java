package squidpony.examples;

import org.junit.Ignore;
import org.junit.Test;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.MarkovTextLimited;
import squidpony.StringKit;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Created by Tommy Ettinger on 8/15/2016.
 */
/*
Mixed-script strings: Ыνωплiam аσιοguι ζi ινыπλειtsαυ рæφes
JDK collisions, 32-bit: 146
FNV collisions, 32-bit: 134
Sip collisions, 32-bit: 129
Lit collisions, 32-bit: 125
English strings: Loorly sait nundeags jeefout; dug?
JDK collisions, 32-bit: 123
FNV collisions, 32-bit: 106
Sip collisions, 32-bit: 130
Lit collisions, 32-bit: 118
Japanese (Romanized) strings: Iyainyado su shaisou oyo rogyadedu?
JDK collisions, 32-bit: 113
FNV collisions, 32-bit: 116
Sip collisions, 32-bit: 128
Lit collisions, 32-bit: 113
Arabic (simplified, Romanized) strings: Akhrariid adhirzhaizh luju lari kuuramiit aih!
JDK collisions, 32-bit: 140
FNV collisions, 32-bit: 118
Sip collisions, 32-bit: 141
Lit collisions, 32-bit: 124

 */
@Ignore
public class HashQualityTest {
    public static int jdkHash(char[] data, int start, int end)
    {
        char[] used = new char[end - start];
        System.arraycopy(data, start, used, 0, used.length);
        return Arrays.hashCode(used);
    }

    public static byte[] byteSection(byte[] data, int start, int end)
    {
        byte[] used = new byte[end - start];
        System.arraycopy(data, start, used, 0, used.length);
        return used;
    }
    //where this restrict is used, it will only use the bottom 16 bits and 8 alternating bits from the top of a hash
    //public static final int restrict = 0x5555FFFF;
    //where this restrict is used, it will use all bits of a hash
    public static final int restrict = -1;
    @Test
    public void testMost()
    {
        CrossHash.Mist storm = CrossHash.Mist.chi;
        CrossHash.Mist mist = CrossHash.Mist.epsilon;
        byte[][] bytes = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {127}, {127, 127}, {127, 127, 127},
                {-128}, {(byte)0xC0}, {(byte)0xF0},
        };
        short[][] shorts = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fff}, {0x7fff, 0x7fff}, {0x7fff, 0x7fff, 0x7fff},
                {-0x8000}, {(short)0xC000}, {(short)0xFF00},
        };
        int[][] ints = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fffffff}, {0x7fffffff, 0x7fffffff}, {0x7fffffff, 0x7fffffff, 0x7fffffff},
                {0x80000000}, {0xC0000000}, {0xFF000000},
        };
        long[][] longs = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fffffffffffffffL}, {0x7fffffffffffffffL, 0x7fffffffffffffffL}, {0x7fffffffffffffffL, 0x7fffffffffffffffL, 0x7fffffffffffffffL},
                {0x8000000000000000L, 0x8000000000000000L, 0x8000000000000000L},
                {0xC000000000000000L, 0xC000000000000000L, 0xC000000000000000L},
                {0xFF00000000000000L, 0xFF00000000000000L, 0xFF00000000000000L},
        };
        Object[][] objects = {
            null,
            {},
                {new OrderedSet<String>(2)},
                {new OrderedSet<String>(2), new OrderedMap<String, String>(2)},
                {new OrderedSet<String>(2), new OrderedMap<String, String>(2), new OrderedSet<String>(2)},

                {new GreasedRegion()},
                {new GreasedRegion(), new GreasedRegion()},
                {new GreasedRegion(), new GreasedRegion(), new GreasedRegion()},

                {new LightRNG(0)},
                {new LightRNG(0), new LightRNG(0)},
                {new LightRNG(0), new LightRNG(0), new LightRNG(0)},

                {FakeLanguageGen.ENGLISH},
                {FakeLanguageGen.ENGLISH, FakeLanguageGen.RUSSIAN_AUTHENTIC},
                {FakeLanguageGen.ENGLISH, FakeLanguageGen.RUSSIAN_AUTHENTIC, FakeLanguageGen.GREEK_AUTHENTIC},

                {new LongPeriodRNG(0)},
                {new LongPeriodRNG(0), new LongPeriodRNG(0)},
                {new LongPeriodRNG(0), new LongPeriodRNG(0), new LongPeriodRNG(0)},

                {new ShortVLA()},
                {new ShortVLA(), new ShortVLA()},
                {new ShortVLA(), new ShortVLA(), new ShortVLA()},

                {new IntVLA()},
                {new IntVLA(), new IntVLA()},
                {new IntVLA(), new IntVLA(), new IntVLA()},

                {Radius.SQUARE},
                {Radius.SQUARE, Radius.CIRCLE},
                {Radius.SQUARE, Radius.CIRCLE, Radius.DIAMOND},

                {new XoRoRNG(0)},
                {new XoRoRNG(0), new XoRoRNG(0)},
                {new XoRoRNG(0), new XoRoRNG(0), new XoRoRNG(0)},

                {new SquidID(5, 5)},
                {new SquidID(5, 5), new SquidID(5, 5)},
                {new SquidID(5, 5), new SquidID(5, 5), new SquidID(5, 5)},


                {new GreasedRegion(), new LightRNG(0)},
                {new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0)},
                {new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0)},

                {new LightRNG(0), FakeLanguageGen.ENGLISH},
                {new LightRNG(0), FakeLanguageGen.ENGLISH, new LightRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC},
                {new LightRNG(0), FakeLanguageGen.ENGLISH, new LightRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LightRNG(0), FakeLanguageGen.GREEK_AUTHENTIC},

                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0)},
                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LongPeriodRNG(0)},
                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LongPeriodRNG(0), FakeLanguageGen.GREEK_AUTHENTIC, new LongPeriodRNG(0)},

                {new LongPeriodRNG(0), new ShortVLA()},
                {new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA()},
                {new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA()},

                {new ShortVLA(), new IntVLA()},
                {new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA()},
                {new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA()},

                {new IntVLA(), Radius.SQUARE},
                {new IntVLA(), Radius.SQUARE, new IntVLA(), Radius.CIRCLE},
                {new IntVLA(), Radius.SQUARE, new IntVLA(), Radius.CIRCLE, new IntVLA(), Radius.DIAMOND},

                {Radius.SQUARE, new XoRoRNG(0)},
                {Radius.SQUARE, new XoRoRNG(0), Radius.CIRCLE, new XoRoRNG(0)},
                {Radius.SQUARE, new XoRoRNG(0), Radius.CIRCLE, new XoRoRNG(0), Radius.DIAMOND, new XoRoRNG(0)},

                {new XoRoRNG(0), new SquidID(5, 5)},
                {new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5)},
                {new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5)},

                {new SquidID(5, 5), null},
                {new SquidID(5, 5), null, new SquidID(5, 5), null},
                {new SquidID(5, 5), null, new SquidID(5, 5), null, new SquidID(5, 5), null},

                {Coord.get(0, 0)}, {Coord.get(0, 0), Coord.get(0, 0)}, {Coord.get(0, 0), Coord.get(0, 0), Coord.get(0, 0)},
                {Coord.get(-1, -1)}, {Coord.get(-1, -1), Coord.get(-1, -1)}, {Coord.get(-1, -1), Coord.get(-1, -1), Coord.get(-1, -1)},
        };
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("JDK bytes: " + Arrays.hashCode(bytes[i]));
            System.out.println("JDK shorts: " + Arrays.hashCode(shorts[i]));
            System.out.println("JDK ints: " + Arrays.hashCode(ints[i]));
            System.out.println("JDK longs: " + Arrays.hashCode(longs[i]));
            System.out.println("JDK objects: " + Arrays.hashCode(objects[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("CrossHash bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("CrossHash shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("CrossHash ints: " + CrossHash.hash(ints[i]));
            System.out.println("CrossHash longs: " + CrossHash.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Lightning bytes: " + CrossHash.Lightning.hash(bytes[i]));
            System.out.println("Lightning shorts: " + CrossHash.Lightning.hash(shorts[i]));
            System.out.println("Lightning ints: " + CrossHash.Lightning.hash(ints[i]));
            System.out.println("Lightning longs: " + CrossHash.Lightning.hash(longs[i]));
            System.out.println("Lightning objects: " + CrossHash.Lightning.hash(objects[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Storm bytes: " + storm.hash(bytes[i]));
            System.out.println("Storm shorts: " + storm.hash(shorts[i]));
            System.out.println("Storm ints: " + storm.hash(ints[i]));
            System.out.println("Storm longs: " + storm.hash(longs[i]));
            System.out.println("Storm objects: " + storm.hash(objects[i]));
        }


        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Wisp bytes: " + CrossHash.Wisp.hash(bytes[i]));
            System.out.println("Wisp shorts: " + CrossHash.Wisp.hash(shorts[i]));
            System.out.println("Wisp ints: " + CrossHash.Wisp.hash(ints[i]));
            System.out.println("Wisp longs: " + CrossHash.Wisp.hash(longs[i]));
            System.out.println("Wisp objects: " + CrossHash.Wisp.hash(objects[i]));
        }
        
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Water bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("Water shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("Water ints: " + CrossHash.hash(ints[i]));
            System.out.println("Water longs: " + CrossHash.hash(longs[i]));
            System.out.println("Water objects: " + CrossHash.hash(objects[i]));
        }
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Mist bytes: " + mist.hash(bytes[i]));
            System.out.println("Mist shorts: " + mist.hash(shorts[i]));
            System.out.println("Mist ints: " + mist.hash(ints[i]));
            System.out.println("Mist longs: " + mist.hash(longs[i]));
            System.out.println("Mist objects: " + mist.hash(objects[i]));
        }



        int longHashLength = 0x80000, stringHashLength = 0xC0000;
        IntDoubleOrderedMap colliderJDK = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderLit = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderSto = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderWis = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderWat = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderMis = new IntDoubleOrderedMap(longHashLength, 0.75f);
        LongPeriodRNG lprng = new LongPeriodRNG();

        System.out.println("Long Hashing:");
        System.out.println("---------------------------------");

        for (int bits = 16, mask = 0xFFFF; bits < 33; mask |= 1 << bits++) {
            lprng.reseed(0x66L);
            for (int i = 0; i < longHashLength; i++) {
                lprng.nextLong();
                colliderJDK.put(Arrays.hashCode(lprng.state) & mask, i);
                colliderLit.put(CrossHash.Lightning.hash(lprng.state) & mask, i);
                colliderSto.put(storm.hash(lprng.state) & mask, i);
                colliderWis.put(CrossHash.Wisp.hash(lprng.state) & mask, i);
                colliderWat.put(CrossHash.hash(lprng.state) & mask, i);
                colliderMis.put(mist.hash(lprng.state) & mask, i);
            }
            System.out.println("JDK collisions, " + bits + "-bit: " + (longHashLength - colliderJDK.size()));
            System.out.println("Lit collisions, " + bits + "-bit: " + (longHashLength - colliderLit.size()));
            System.out.println("Sto collisions, " + bits + "-bit: " + (longHashLength - colliderSto.size()));
            System.out.println("Wis collisions, " + bits + "-bit: " + (longHashLength - colliderWis.size()));
            System.out.println("Wat collisions, " + bits + "-bit: " + (longHashLength - colliderWat.size()));
            System.out.println("Mis collisions, " + bits + "-bit: " + (longHashLength - colliderMis.size()));
            System.out.println();
            colliderJDK.clear();
            colliderLit.clear();
            colliderSto.clear();
            colliderWis.clear();
            colliderWat.clear();
            colliderMis.clear();
        }

        System.out.println("\nString Hashing:");
        System.out.println("---------------------------------");
        StatefulRNG srng = new StatefulRNG(0x1337CAFE);
        String input;
        FakeLanguageGen oddLang;
        String[] midPunct = {",", ";", " -"}, endPunct = {"..."};
        char[] massive;
        int langLength;
        System.out.println("Arabic-ish text, Romanized");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.ARABIC_ROMANIZED;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.put(jdkHash(massive, s, s+e+32) & restrict, i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict, i);
            colliderSto.put(storm.hash(massive, s, s+e+32) & restrict, i);
            colliderWis.put(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict, i);
            colliderWat.put(CrossHash.hash(massive, s, s+e+32) & restrict, i);
            colliderMis.put(mist.hash(massive, s, s+e+32) & restrict, i);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size()));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("Japanese-ish text, Romanized");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.JAPANESE_ROMANIZED;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.put(jdkHash(massive, s, s+e+32) & restrict, i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict, i);
            colliderSto.put(storm.hash(massive, s, s+e+32) & restrict, i);
            colliderWis.put(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict, i);
            colliderWat.put(CrossHash.hash(massive, s, s+e+32) & restrict, i);
            colliderMis.put(mist.hash(massive, s, s+e+32) & restrict, i);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size()));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("Unicode-heavy fantasy text");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.FANCY_FANTASY_NAME.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.67).mix(FakeLanguageGen.RUSSIAN_AUTHENTIC, 0.45);
        String hugeSentence = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3);
        massive = hugeSentence.toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.put(jdkHash(massive, s, s+e+32) & restrict, i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict, i);
            colliderSto.put(storm.hash(massive, s, s+e+32) & restrict, i);
            colliderWis.put(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict, i);
            colliderWat.put(CrossHash.hash(massive, s, s+e+32) & restrict, i);
            colliderMis.put(mist.hash(massive, s, s+e+32) & restrict, i);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size()));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("English text");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.ENGLISH;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.put(jdkHash(massive, s, s+e+32) & restrict, i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict, i);
            colliderSto.put(storm.hash(massive, s, s+e+32) & restrict, i);
            colliderWis.put(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict, i);
            colliderWat.put(CrossHash.hash(massive, s, s+e+32) & restrict, i);
            colliderMis.put(mist.hash(massive, s, s+e+32) & restrict, i);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size()));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        /*
        System.out.println("Unicode fantasy as UTF-8 bytes");
        byte[] massiveBytes = hugeSentence.getBytes(StandardCharsets.UTF_8), section;
        langLength = massiveBytes.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            section = byteSection(massiveBytes, s, s+e+32);
            colliderJDK.put(jdkHash(massive, s, s+e+32) & restrict, i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict, i);
            colliderSto.put(storm.hash(massive, s, s+e+32) & restrict, i);
            colliderWis.put(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict, i);
            colliderWat.put(CrossHash.hash(massive, s, s+e+32) & restrict, i);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        */
    }
    public static int slitherHash(final CharSequence data) {
        if (data == null)
            return 0;
        //long result = 0x9E3779B97F4A7C80L, a = 0x632BE59BD9B4E019L;
        long result = 0x1A976FDF6BF60B8EL, a = 0x60642E2A34326F15L;// 253
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a = (a ^ data.charAt(i)) * 0x41C64E6BL);
        }
        a ^= (result ^ result >>> 27) * 0xAEF17502108EF2D9L;
        return (int) (((a ^ a >>> 25)));// ^ (a >>> 32));
    }
    public static int slitherHashConfig(final CharSequence data, long running, long receiver) {
        if (data == null)
            return 0;
        //long running = 0x9E3779B97F4A7C80L, receiver = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            running += (receiver = (receiver ^ data.charAt(i)) * 0x41C64E6BL);
        }
        receiver ^= (running ^ running >>> 27) * 0xAEF17502108EF2D9L;
        return (int) (((receiver ^ receiver >>> 25)));// ^ (a >>> 32));
// 0x9E3779B97F4A7C15L
        //        if (data == null)
//            return 0;
//        long result = 0x9E3779B97F4A7C80L, a = 0x632BE59BD9B4E019L;
//        final int len = data.length();
//        for (int i = 0; i < len; i++) {
//            result += (a = (a ^ data.charAt(i)) * 0xC6BC279692B5CC83L);
//        }
//        a ^= (result ^ result >>> 27);
//        return (int) (((a ^ a >>> 25)));// ^ (a >>> 32));
    }
//    public static int joltHash(final CharSequence data) {
//        if (data == null)
//            return 0;
//        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
//        for (int i = 0; i < data.length(); i++) {
//            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
//        }
//        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
//        return (int) (result ^ result >>> 25 ^ z ^ z >>> 29);
//    }
    
    public static int joltHash(final CharSequence data)
    {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length();
        for (int i = 0; i < len; ++i) {
            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }
    
    public static int buzzHash(final CharSequence data) {
        if (data == null)
            return 0;
        //long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        //int result = 0xA539B, z = 0xE47AF;
        int result = 0xF369B, z = 0xEF17B;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) ^ 0xC74EAD55) * 0xA5CB3);
        }
        result += (z ^ z >>> 13) * 0x62BD5;
        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }
    public static int buzzHashConfig(final CharSequence data, int result, int z) {
        if (data == null)
            return 0;
        //long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) ^ 0xC74EAD55) * 0xA5CB3);
        }
        result += (z ^ z >>> 13) * 0x62BD5;
        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }
    public static int lantern(int state)
    {
        //return (state = ((state = (((state * 0x62BD5) ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13) * 0xAF2D9) ^ state >>> 10;
//        return ((state = ((state = ((state ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13) * 0x62BD5) << 22 | state >>> 10);
//        return ((state = (state ^ 0xC74EAD55) * 0xA5CB3) ^ ((state << 20) | (state >>> 12)) ^ ((state << 9) | (state >>> 23)));
        return (state = (state ^ (state << 20 | state >>> 12) ^ (state << 9 | state >>> 23) ^ 0xC74EAD55) * 0xA5CB3) + (state >>> 16);
    }
    public static int xlxs(int state)
    {
//        return ((state = ((state ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13);
        return (state *= 0x9E375) ^ state >>> 16;
    }
    public static int xs3(int state)
    {
        state ^= state >>> 14;
        state ^= state >>> 15;
        return state ^ state << 13;
    }
    public static int yuraHash(final CharSequence data, final int seed)
    {
        int h1 = seed ^ 0x41C64E6B;
        int h2 = (seed << 15 | seed >>> 17);
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            h1 += data.charAt(i);
            h2 += (h1 ^= h1 << 3);
            h2 = (h2 << 7 | h2 >>> 25);
            h2 ^= h2 << 2;
        }
        h1 ^= h2;
        h1 += (h2 << 14 | h2 >>> 18);
        h2 ^= h1;
        h2 += (h1 << 26 | h1 >>> 6);
        h1 ^= h2;
        h1 += (h2 << 5 | h2 >>> 27);
        return (h2 + (h1 << 24 | h1 >>> 8)) ^ h1;
    }

    
    public static int oldCoord(long x, long y)
    {
        x *= 0x9E3779B97F4A7C15L;
        y *= 0x632BE59BD9B4E019L;
        return (int) (((x ^ y) >>> ((x & 15) + (y & 15))) * 0x85157AF5L);

    }

    //179922 with (r << 25 | r >>> 7)
    public static int latheCoord(int x, int y)
    {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << 25 | r >>> 7);
    }
    public static int olderLatheCoord(int x, int y)
    {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ r >>> 8;
    }

    public static int latheCoordConfig(int x, int y, int rot)
    {
//        y ^= x;
//        y = (((x << 13 | x >>> 19) ^ y ^ (y << 5)) + (y << 28 | y >>> 4));
//        return Integer.rotateLeft(y, rot) + x;
        
//        y ^= x;
//        y ^= (x << 13 | x >>> 19) ^ (y << 5) ^ (y << 28 | y >>> 4);
//        return (x ^= (y << 11 | y >>> 21)) ^ x >>> rot;
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << rot | r >>> -rot);
    }

    public static int buzzCoord(int x, int y) {
//        x += y * 0xC13FA9A9;
//        y += x * 0x91E10DA5;
        y = (y + ((x+y) * (x+y+1) >> 1)) * 0x9E375;
        return (y ^ y >>> 14);
//        return ((y = (y ^ y >>> 13 ^ 0x9E3779BD) * 0xC6BC2793) ^ y >>> 11);
//        int result = 0xF369B, z = 0xEF17B;
//        result ^= (z += (x ^ 0xC74EAD55) * 0xA5CB3);
//        result ^= (z += (y ^ 0xC74EAD55) * 0xA5CB3);
//        result += (z ^ z >>> 13) * 0x62BD5;
//        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }

    public static int szudzikCoord(int x, int y)
    {
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        return (x >= y
                ? x * x + x + y
                : x + y * y);
    }

    public static int rosenbergStrongCoord(int x, int y)
    {
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        return x + (x >= y ? x * x + x - y : y * y);
//        return y + (y >= x ? y * y + y - x : x * x);
    }
    public static int szudzik2Coord(int x, int y)
    {
//        s = 42 ^ s * 0x1827F5 ^ y * 0x123C21;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;

        //return (x + (x >= y ? x * x + y : y * y)) * 0x41C6B ^ 0x9E3779BD;
        //x *= 0x41C64E6B;
        //y *= 0x9E3779BD;
//        return (x += (x >= y ? x * x + y : y * y)) ^ (x << 11 | x >>> 21) ^ (x << 20 | x >>> 12);
        return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 11 | x >>> 21) ^ (x << 20 | x >>> 12)) * 0x13C6EF;

//        return x ^ (x << 13 | x >>> 19) ^ (x << 22 | x >>> 12) ^ (x << 6 | x >>> 26) ^ (x << 29 | x >>> 3);
//        x ^= x >>> (x >>> 28) + 4 ^ 0x91E10DA5;
//        return x * 0x125493;
//        return x ^ x >>> (x >>> 28) + 4;

//        x ^= x >> 31;
//        y ^= y >> 31;
//        return ((x >= y 
//                ? x * x + x + y 
//                : x + y * y) ^ 0xD1B54A35) * 0x125493 ^ 0x9E3779B9;

    }


    public static int cantorCoord(int x, int y)
    {
//        x ^= x >> 31;
//        y ^= y >> 31;
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        return (((x+y >> 1) * (x+y+1 >> 1)) + y);// * 0xA5CB3;

//        x ^= x >> 1;
//        y ^= y >> 1;
//        x += 3;
//        y += 3;
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        return y + ((x+y) * (x+y+1) >> 1);
//        return y ^ y >>> 1;

//        return (((x+y) * (x+y+1) >> 1) + y);// * 0xA5CB3;
    }

    public static int goldCoord(int x, int y)
    {
        int s = 42;
        y ^= (s ^ 0xD192ED03) * 0x1A36A9;
        x ^= (y ^ 0xFB8FAC03) * 0x157931;
        s ^= (x ^ 0x2F3D8DD7) * 0x119725;
        return (s = (s ^ s >>> 11 ^ s >>> 21) * (s | 0xFFE00001) ^ x ^ y) ^ s >>> 13 ^ s >>> 19;
    }

    public static int pelotonCoord(int x, int y) {
//        final long a = x + CrossHash.Water.b1, b = y + CrossHash.Water.b2;
//        return (int) ((a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25)) >>> 32);
//        x = (x ^ (x << 13 | x >>> 19) ^ (x << 21 | x >>> 11));
//        y = (y ^ (y << 15 | y >>> 17) ^ (y << 29 | y >>> 3));
//        x ^= x >> 31;
//        y ^= y >> 31;

//        y += ((x+y) * (x+y+1) >> 1);

//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += (y >= x ? y * y + x : x * x);
//        x *= 0x125493;
//        y *= 0x177C0B;
        
//        //working fine except for lowest 4 bits
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += ((x+y) * (x+y+1) >> 1);
//        y ^= y >>> 2 ^ y >>> 11;
//        y = ((y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35) * 0x125493;
//        return y ^ y >>> 1;

        // similar to above but has slightly better low bits
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += ((x+y) * (x+y+1) >> 1);
//        y ^= y >>> 2 ^ y >>> 5 ^ y >>> 6 ^ y >>> 11;
//        y = ((y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35) * 0x125493;
//        return y ^ y >>> 1;

        // about the same as either hash above in collision rates, beats raw Cantor, but horrible visual bit bias
//        return (int)((x * 0xC13FA9A902A6328FL + y * 0x91E10DA5C79E7B1DL) >>> 32);
        return (int)((x * 0xC13FA9A902A6328FL + y * 0x91E10DA5C79E7B1DL) >>> 32);

// ^ 0x9E3779BD;
//        y ^= y >>> 1 ^ y >>> 6;
//        y = (y ^ (y << 15 | y >>> 17) ^ (y << 23 | y >>> 9)) * 0xACEDB;
//        y = (y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35;
//        return y ^ y >>> 1;

        //        return (y ^ (y << 13 | y >>> 19) ^ (y << 21 | y >>> 11) ^ 0xD1B54A35) * 0xACEDB ^ 0x9E3779BD;
//        return (y ^ y >>> 1);// * 0x125493 ^ 0xD1B54A35;

//        y =
//                (x >= y
//                ? x * x + x + y
//                : x + y * y);
        
        
//        int s = 42 ^ x * 0x1827F5 ^ y * 0x123C21;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;

        //0xD1B54A32D192ED03L, 0xABC98388FB8FAC03L, 0x8CB92BA72F3D8DD7L
    }

    public static int peloton3D(int x, int y, int z) {
//        int s = 42 ^ x + y + z;
//        s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1A36A9;// ^ 0x02A6328F;
//        s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
//        s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x157931;// ^ 0xC79E7B1D;
//        s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
//        s = (z ^ (z << 11 | z >>> 21) ^ (z << 23 | z >>> 9) ^ s) * 0x119725;// ^ 0xC79E7B1D;
//        return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
//        int s = 0x75AE2165;
//        s = (s + x + y + z ^ 0x75AE2165) * 0x1B69E1;
//        s += (x ^ 0x03A4615F) * 0x177C0B;
//        s += (y ^ 0xA1FE1575) * 0x141E5D;
//        s += (z ^ 0x7D9ED689) * 0x113C31;
//        int s = 0x9E3779B9;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ x) * 0x177C0B;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ y) * 0x141E5B;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ z) * 0x113C33;
//        return s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11);
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ x * 3) * 0x177C0B;
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ y * 5) * 0x141E5B;
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ z * 7) * 0x113C33;
        //s ^= s << 8 ^ s >>> 15 ^ s >>> 9;
//        int s = 0x9E3779B9 ^ x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;

        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        z = z << 1 ^ z >> 31;

//        x ^= x >> 31;
//        y ^= y >> 31;
//        z ^= z >> 31;

//        x += (x >= z ? x * x + z : z * z);
//        y += (y >= x ? y * y + x : x * x);
        y += ((z+y) * (z+y+1) >> 1);
        y += ((x+y) * (x+y+1) >> 1);
        y ^= y >>> 1 ^ y >>> 6;
        return (y ^ (y << 15 | y >>> 17) ^ (y << 21 | y >>> 11)) * 0x125493 ^ 0xD1B54A35;

//        int s = 0x9E3779B9 ^ x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 6 | s >>> 26) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;


//        int s = 0x9E3779B9 ^ 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
//        return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
        
//        return ((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L) ^ state >>> 28);
//        int s = 0x75AE2165;
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (x * 0x177C0B);
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (y * 0x141E5B);
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (z * 0x113C33);
//        return s ^ s << 8 ^ s >>> 15 ^ s >>> 9;

        //0xDB4F0B9175AE2165L, 0xBBE0563303A4615FL, 0xA0F2EC75A1FE1575L, 0x89E182857D9ED689L
    }
//        y ^= (s ^ 0xD192ED03) * 0x1A36A9;
//        x ^= (y ^ 0xFB8FAC03) * 0x157931;
//        s ^= (x ^ 0x2F3D8DD7) * 0x119725;
//        return (s = (s ^ s >>> 11 ^ s >>> 21) * (s | 0xFFE00001) ^ x ^ y) ^ s >>> 13 ^ s >>> 19;

    /**
     * Running on sizes: 16, 56, 61, 65, 92, 95, 103, 106, 116, 131, 136, 150, 152, 172, 220, 273, 293, 297, 311, 316, 324, 327, 353, 357, 360, 369, 370, 376, 386, 398
     * Number of Coords added: 150053244
     * TOTAL Lath collisions: 4498342 (2.997830556732249%), BEST 0, WORST 9010
     * TOTAL Szud collisions: 5240115 (3.492170419188005%), BEST 0, WORST 6438
     * TOTAL Pelo collisions: 4494271 (2.9951175197518554%), BEST 0, WORST 6724
     * TOTAL Cant collisions: 7260455 (4.8385858289075045%), BEST 0, WORST 8859
     * TOTAL Gold collisions: 23246597 (15.492232210587863%), BEST 1, WORST 21527
     * TOTAL Obje collisions: 106566447 (71.01908906414579%), BEST 0, WORST 87793
     * <br>
     * Running on sizes: 16, 56, 61, 65, 92, 95, 103, 106, 116, 131, 136, 150, 152, 154, 172, 173, 220, 273, 293, 297, 311, 316, 324, 327, 353, 357, 360, 369, 370, 376, 386, 398
     * Number of Coords added: 192057238
     * TOTAL Lath collisions: 32765410 (17.060231804437382%), BEST 0, WORST 30062
     * TOTAL Szud collisions: 28092096 (14.6269394960267%), BEST 0, WORST 26903
     * TOTAL Pelo collisions: 28679649 (14.932865482528703%), BEST 1, WORST 27994
     * TOTAL Cant collisions: 28541456 (14.86091141225305%), BEST 0, WORST 27817
     * TOTAL Gold collisions: 38067211 (19.8207635371701%), BEST 2, WORST 33344
     * TOTAL Obje collisions: 112523166 (58.588349583575706%), BEST 1, WORST 99462
     */
    @Test
    public void testCoord() {
        RNG prng = new RNG(new LightRNG(123));
        final int[] params = new int[32];// ArrayTools.range(10, 26);// new int[]{33, 65, 129, 257, 513};
        System.arraycopy(prng.randomOrdering(400), 0, params, 0, params.length);
//        final int[] params = new int[]{64, 128, 256, 512};
        Arrays.sort(params);
        System.out.println("Running on sizes: " + StringKit.join(", ", params));
        long lathTotal = 0L, objeTotal = 0L, peloTotal = 0L, rostTotal = 0L, szudTotal = 0L, cantTotal = 0L, total = 0L,
                lathBest = 1000000L,
                objeBest = 1000000L,
                peloBest = 1000000L,
                rostBest = 1000000L,
                szudBest = 1000000L,
                cantBest = 1000000L,
                lathWorst = 0L, objeWorst = 0L, peloWorst = 0L, rostWorst = 0L, szudWorst = 0L, cantWorst = 0L, t;
//        long[] confTotals = new long[31];
        for (int reduction = 7; reduction >= 0; reduction--) {
            System.out.println("Running reduction level " + reduction);
            for (int w : params) {
                final int WIDTH = w;
                for (int h : params) {
                    final int HEIGHT = h;
                    Coord.expandPoolTo(WIDTH, HEIGHT);
                    int SIZE = WIDTH * HEIGHT;
                    int restrict = HashCommon.nextPowerOfTwo(SIZE) - 1;

                    IntDoubleOrderedMap colliderLath = new IntDoubleOrderedMap(SIZE, 0.5f),
                            colliderObje = new IntDoubleOrderedMap(SIZE, 0.5f),
                            colliderPelo = new IntDoubleOrderedMap(SIZE, 0.5f),
                            colliderSzud = new IntDoubleOrderedMap(SIZE, 0.5f),
                            colliderCant = new IntDoubleOrderedMap(SIZE, 0.5f),
                            colliderRoSt = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    IntDoubleOrderedMap[] colliders = new IntDoubleOrderedMap[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    }
                    LightRNG rng = new LightRNG(1L);
                    SNShuffledIntSequence
                            xShuffle = new SNShuffledIntSequence(WIDTH, 1),
                            yShuffle = new SNShuffledIntSequence(HEIGHT, -1);
                    BitSet points = new BitSet(WIDTH * HEIGHT + WIDTH + HEIGHT << 2);
//                    UnorderedSet<Coord> points = new UnorderedSet<>(WIDTH * HEIGHT);
                    for (int i = 0; i < WIDTH; i++) {
                        for (int j = 0; j < HEIGHT; j++) {
//                            int x = xShuffle.next(), y = yShuffle.next();
                            int x = xShuffle.next() ^ -rng.next(1), y = yShuffle.next() ^ -rng.next(1);
                            int c = x + WIDTH + 1 + (y + HEIGHT + 1) * WIDTH;
                            if (rng.next(3) > reduction || points.get(c)) {
                                --SIZE;
                                continue;
                            }
                            points.set(c);
//                            colliderLath.put(latheCoord(x, y) & restrict, 0.0);
                            colliderSzud.put(szudzikCoord(x, y) & restrict, 0.0);
                            colliderPelo.put(pelotonCoord(x, y) & restrict, 0.0);
                            colliderCant.put(cantorCoord(x, y) & restrict, 0.0);
                            colliderRoSt.put(rosenbergStrongCoord(x, y) & restrict, 0.0);
//                            colliderGold.put(Noise.IntPointHash.hashAll(x, y, 42) & restrict, 0.0);
//                            colliderObje.put(Objects.hash(x, y) & restrict, 0.0);
                            
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].put(latheCoordConfig(x, y, i + 1) & restrict, 0.0);
//                            }
                        }
                    }
//                    System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT + ", SIZE: " + SIZE);
                    t = (SIZE - colliderLath.size()); lathBest = Math.min(t, lathBest); lathWorst = Math.max(t, lathWorst);
//                    System.out.println("Lath collisions: " + t);
                    t = (SIZE - colliderPelo.size()); peloBest = Math.min(t, peloBest); peloWorst = Math.max(t, peloWorst);
//                    System.out.println("Pelo collisions: " + t);
                    t = (SIZE - colliderSzud.size()); szudBest = Math.min(t, szudBest); szudWorst = Math.max(t, szudWorst);
//                    System.out.println("Szud collisions: " + t);
                    t = (SIZE - colliderCant.size()); cantBest = Math.min(t, cantBest); cantWorst = Math.max(t, cantWorst);
//                    System.out.println("Cant collisions: " + t);
                    t = (SIZE - colliderRoSt.size()); rostBest = Math.min(t, rostBest); rostWorst = Math.max(t, rostWorst);
//                    System.out.println("RoSt collisions: " + t);
                    t = (SIZE - colliderObje.size()); objeBest = Math.min(t, objeBest); objeWorst = Math.max(t, objeWorst);
//                    System.out.println("Obje collisions: " + t);

//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size()));
//                        confTotals[i] += (SIZE - colliders[i].size());
//                    }
                    lathTotal += (SIZE - colliderLath.size());
                    peloTotal += (SIZE - colliderPelo.size());
                    szudTotal += (SIZE - colliderSzud.size());
                    cantTotal += (SIZE - colliderCant.size());
                    rostTotal += (SIZE - colliderRoSt.size());
                    objeTotal += (SIZE - colliderObje.size());
                    total += SIZE;
                }
            }
        }
        System.out.println("Number of Coords added: " + total);
        System.out.println("TOTAL Lath collisions: " + lathTotal + " (" + (lathTotal * 100.0 / total) + "%), BEST " + lathBest + ", WORST " + lathWorst);
        System.out.println("TOTAL Szud collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
        System.out.println("TOTAL Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
        System.out.println("TOTAL Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
        System.out.println("TOTAL RoSt collisions: " + rostTotal + " (" + (rostTotal * 100.0 / total) + "%), BEST " + rostBest + ", WORST " + rostWorst);
        System.out.println("TOTAL Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }
    // This one takes a while to run; be advised.
    @Test
    public void testCoord3() {
        final int[] params = ArrayTools.range(8, 18);// new int[]{33, 65, 129, 257, 513};
//        final int[] params = new int[]{64, 128, 256, 512};
        long baseTotal = 0L, objeTotal = 0L, peloTotal = 0L, hastTotal = 0L, szudTotal = 0L, cantTotal = 0L, total = 0L,
                baseBest = 1000000L,
                objeBest = 1000000L,
                peloBest = 1000000L,
                hastBest = 1000000L,
                szudBest = 1000000L,
                cantBest = 1000000L,
                baseWorst = 0L, objeWorst = 0L, peloWorst = 0L, hastWorst = 0L, szudWorst = 0L, cantWorst = 0L, t;
//        long[] confTotals = new long[31];
        for (int reduction = 7; reduction >= 0; reduction--) {

            for (int d : params) {
                int DEPTH = d * 3;
                for (int w : params) {
                    int WIDTH = w * 3;
                    for (int h : params) {
                        int HEIGHT = h * 3;
                        int SIZE = WIDTH * HEIGHT * DEPTH;
                        int restrict = HashCommon.nextPowerOfTwo(SIZE) - 1;

                        IntDoubleOrderedMap colliderBase = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderObje = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderPelo = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderSzud = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderCant = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderHast = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    IntDoubleOrderedMap[] colliders = new IntDoubleOrderedMap[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    }
                        DiverRNG rng = new DiverRNG(1L);
                        SNShuffledIntSequence
                                xShuffle = new SNShuffledIntSequence(WIDTH, 1),
                                yShuffle = new SNShuffledIntSequence(HEIGHT, -1),
                                zShuffle = new SNShuffledIntSequence(DEPTH, rng.nextInt());
                        UnorderedSet<Coord3D> points = new UnorderedSet<>(SIZE);
                        for (int i = 0; i < WIDTH; i++) {
                            for (int j = 0; j < HEIGHT; j++) {
                                for (int k = 0; k < DEPTH; k++) {
//                            int x = xShuffle.next(), y = yShuffle.next();
                                    int x = xShuffle.next() ^ -rng.next(1), y = yShuffle.next() ^ -rng.next(1), z = zShuffle.next() ^ -rng.next(1);
                                    Coord3D c = Coord3D.get(x, y, z);
                                    if (rng.next(3) > reduction || points.contains(c)) {
                                        --SIZE;
                                        continue;
                                    }
                                    points.add(c);
                                    colliderBase.put(Noise.IntPointHash.hashAll(x, y, z, 0x9E3779B9) & restrict, 0.0);//((int) Noise.PointHash.hashAll(x, y, z, 0x9E3779B9L) & restrict, 0.0);
                                    colliderPelo.put(peloton3D(x, y, z) & restrict, 0.0);
                                    colliderSzud.put(szudzikCoord(z, szudzikCoord(x, y)) & restrict, 0.0);
                                    colliderCant.put(cantorCoord(z, cantorCoord(x, y)) & restrict, 0.0);
                                    colliderHast.put((int) Noise.HastyPointHash.hashAll(x, y, z, 0x9E3779B9L) & restrict, 0.0);
                                    colliderObje.put(Objects.hash(x, y, z) & restrict, 0.0);
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].put(latheCoordConfig(x, y, i + 1) & restrict, 0.0);
//                            }
                                }
                            }
                        }
//                    System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT + ", SIZE: " + SIZE);
                        t = (SIZE - colliderBase.size());
                        baseBest = Math.min(t, baseBest);
                        baseWorst = Math.max(t, baseWorst);
//                    System.out.println("Base collisions: " + t);
                        t = (SIZE - colliderPelo.size());
                        peloBest = Math.min(t, peloBest);
                        peloWorst = Math.max(t, peloWorst);
//                    System.out.println("Szu2 collisions: " + t);
                        t = (SIZE - colliderSzud.size());
                        szudBest = Math.min(t, szudBest);
                        szudWorst = Math.max(t, szudWorst);
//                    System.out.println("Szud collisions: " + t);
                        t = (SIZE - colliderCant.size());
                        cantBest = Math.min(t, cantBest);
                        cantWorst = Math.max(t, cantWorst);
//                    System.out.println("Cant collisions: " + t);
                        t = (SIZE - colliderHast.size());
                        hastBest = Math.min(t, hastBest);
                        hastWorst = Math.max(t, hastWorst);
//                    System.out.println("Gold collisions: " + t);
                        t = (SIZE - colliderObje.size());
                        objeBest = Math.min(t, objeBest);
                        objeWorst = Math.max(t, objeWorst);
//                    System.out.println("Obje collisions: " + t);

//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size()));
//                        confTotals[i] += (SIZE - colliders[i].size());
//                    }
                        baseTotal += (SIZE - colliderBase.size());
                        peloTotal += (SIZE - colliderPelo.size());
                        szudTotal += (SIZE - colliderSzud.size());
                        cantTotal += (SIZE - colliderCant.size());
                        hastTotal += (SIZE - colliderHast.size());
                        objeTotal += (SIZE - colliderObje.size());
                        total += SIZE;
                    }
                }
            }
            System.out.println("INTERMEDIATE number of Coords added: " + total + " on reduction " + reduction);
            System.out.println("INTERMEDIATE Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
            System.out.println("INTERMEDIATE Szud collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
            System.out.println("INTERMEDIATE Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
            System.out.println("INTERMEDIATE Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
            System.out.println("INTERMEDIATE Hast collisions: " + hastTotal + " (" + (hastTotal * 100.0 / total) + "%), BEST " + hastBest + ", WORST " + hastWorst);
            System.out.println("INTERMEDIATE Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);

        }
        System.out.println("Number of Coords added: " + total);
        System.out.println("TOTAL Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
        System.out.println("TOTAL Szud collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
        System.out.println("TOTAL Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
        System.out.println("TOTAL Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
        System.out.println("TOTAL Hast collisions: " + hastTotal + " (" + (hastTotal * 100.0 / total) + "%), BEST " + hastBest + ", WORST " + hastWorst);
        System.out.println("TOTAL Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }
    
    
    
    
    
    
    
    @Test
    public void testLimited()
    {
        int restrict = 0xFFFFF;
        MarkovTextLimited markovText = new MarkovTextLimited();
        String theme = "dun dun dun, dun dundun, dun dundun, dun dun dun dun dundun dun dundun.";
        String party = "party party party, I wanna have a party, we're gonna have a party, you better have a party!";
        //markovText.analyze(party);//theme.replace("dun", "wiggle")
        final int LIMIT = 0x100000;
        UnorderedSet<String> strings = new UnorderedSet<>(LIMIT);
        for (int i = 0; i < (LIMIT << 2) && strings.size() < LIMIT; i++) {
            // usually not necessary
            //strings.add(markovText.chain(DiverRNG.determine(i * 0x9E3779B97F4A7C15L + 0xC6BC279692B5CC83L), 100));
            // try changing length
            //strings.add(markovText.chain(DiverRNG.determine(i + 0x1234567890L), 190));
            strings.add(String.format("       %16s       ", Integer.toString(i, 5)));
            //strings.add(FakeLanguageGen.JAPANESE_ROMANIZED.sentence(DiverRNG.determine(i + 0x12345678), 10, 10));
        }
        int stringHashLength = strings.size();
        IntDoubleOrderedMap colliderJDK = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderLit = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderWis = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderSli = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderWat = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderJol = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderHiv = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderSpl = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderYur = new IntDoubleOrderedMap(stringHashLength, 0.5f),
                colliderBuz = new IntDoubleOrderedMap(stringHashLength, 0.5f);
//        LightRNG rng1 = new LightRNG(DiverRNG.determine(System.nanoTime() * 0x9E3779B97F4A7C15L + 0xC6BC279692B5CC83L));
//        DiverRNG rng2 = new DiverRNG(LightRNG.determine(System.nanoTime() * 0xC6BC279692B5CC83L + 0x9E3779B97F4A7C15L));
//        final int SIZE = 1024;
//        int[][] pairs = new int[SIZE][2];
//        IntDoubleOrderedMap[] colliders = new IntDoubleOrderedMap[SIZE];
//        for (int i = 0; i < SIZE; i++) {
//            colliders[i] = new IntDoubleOrderedMap(stringHashLength, 0.65f);
//            pairs[i][0] = rng1.next(20);
//            pairs[i][1] = rng2.next(20);
//        }
        for(String s : strings)
        {
            colliderJDK.put(s.hashCode() & restrict, 1.0);
            colliderLit.put(CrossHash.Lightning.hash(s) & restrict, 1.0);
            colliderWis.put(CrossHash.Wisp.hash(s) & restrict, 1.0);
            colliderSli.put(slitherHash(s) & restrict, 1.0);
            colliderWat.put(CrossHash.hash(s) & restrict, 1.0);
            colliderJol.put(joltHash(s) & restrict, 1.0);
            colliderHiv.put(CrossHash.Hive.hash(s) & restrict, 1.0);
            colliderSpl.put(lantern(s.hashCode()) & restrict, 1.0);
            colliderYur.put(yuraHash(s, 0xC74EAD55) & restrict, 1.0);
            colliderBuz.put(buzzHash(s) & restrict, 1.0);
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].put(slitherHashConfig(s, pairs[i][0], pairs[i][1]) & restrict, i);
//            }
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].put(buzzHashConfig(s, pairs[i][0], pairs[i][1]) & restrict, i);
//            }
        }
        System.out.println(strings.iterator().next());
        System.out.println("With " + stringHashLength + " distinct Strings:");
        System.out.println("JDK collisions, 16-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("Lit collisions, 16-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Wis collisions, 16-bit: " + (stringHashLength - colliderWis.size()));
        System.out.println("Sli collisions, 16-bit: " + (stringHashLength - colliderSli.size()));
        System.out.println("Wat collisions, 16-bit: " + (stringHashLength - colliderWat.size()));
        System.out.println("Jol collisions, 16-bit: " + (stringHashLength - colliderJol.size()));
        System.out.println("Lan collisions, 16-bit: " + (stringHashLength - colliderSpl.size()));
        System.out.println("Yur collisions, 16-bit: " + (stringHashLength - colliderYur.size()));
        System.out.println("Buz collisions, 16-bit: " + (stringHashLength - colliderBuz.size()));
        System.out.println("Hiv collisions, 16-bit: " + (stringHashLength - colliderHiv.size()));
//        Arrays.sort(colliders, new Comparator<IntDoubleOrderedMap>() {
//            @Override
//            public int compare(IntDoubleOrderedMap o1, IntDoubleOrderedMap o2) {
//                return o2.size() - o1.size();
//            }
//        });
//        IntDoubleOrderedMap idm;
//        int idx;
//        for (int i = 0; i < 10; i++) {
//            idm = colliders[i];
//            idx = (int)idm.get(idm.firstIntKey());
//            System.out.printf("0x%08X, 0x%08X : %d\n", pairs[idx][0], pairs[idx][1], (stringHashLength - idm.size()));
//            idm.clear();
//        }
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderWis.clear();
        colliderSli.clear();
        colliderWat.clear();
        colliderJol.clear();
        colliderHiv.clear();
        colliderSpl.clear();
        colliderYur.clear();
        colliderBuz.clear();
    }
    /*
    0x2EF022689E573495L, 0x8628F680AFADEDABL : 629
    0xCFEB847AE4B6AD26, 0xE7FFD14DDB14DD2D : 652
    0xA8BC4AE2C9542B80, 0xC8D3A0DE9FE21288 : 653
    0x7DC2031CCF49AC00, 0x20BF5813242226B7 : 654
    
    0x7B59E7FC789BA792L, a = 0xA0729045E286D65FL;// 959
    0xABCB254A1454AE87, 0x6762DC42F20490D1 : 963
    0xED55E99E89BAC9AA, 0x2AD7C0BD32AA8502 : 966
    0x63B36541EDEA1AE2, 0x37B32F846A5CB867 : 971
    
    0x1A976FDF6BF60B8E, a = 0x60642E2A34326F15L;// 253
    
    with 32-bit:
    0x000A539B, 0x000E47AF : 6786
    0x0007A702, 0x00023F9F : 6786
    0x000284FD, 0x0008F853 : 6787
    0x00056F55, 0x00021219 : 6789
     */

    @Test
    public void testMix() {
        final int[] params = new int[]{64, 128, 256, 512, 1024}, increases = {0,1,2};
        long baseTotal = 0, hacoTotal = 0, lantTotal = 0, xlxsTotal = 0, total = 0;
//        long[] confTotals = new long[31];
        NLFSR gen = new NLFSR(1234567);
        int mul = gen.nextInt() << 1 | 1;
        for (int r = 1; r < 32; r++) {

            for (int m = 1; m < 0x1000; m++) {
                mul = gen.nextInt() << 1 | 1;
                for (int INCREASE : increases) {
                    for (int SIZE : params) {
                        int restrict = (SIZE << INCREASE) - 1;

                        IntDoubleOrderedMap colliderBase = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderHaCo = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderLant = new IntDoubleOrderedMap(SIZE, 0.5f),
                                colliderXLXS = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    IntDoubleOrderedMap[] colliders = new IntDoubleOrderedMap[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntDoubleOrderedMap(SIZE, 0.5f);
//                    }
                        for (int y = 1; y <= SIZE; y++) {
//                        int x = (ThrustAltRNG.determineInt(y));
//                        int x = xs3(y*0x1FFFFFFF);
                            int x = Integer.rotateLeft(y * mul, r);
//                        x = x << 16 | x >>> 16;
                            colliderBase.put(x & restrict, 0.0);
                            colliderLant.put(xs3(x) & restrict, 0.0);
                            colliderXLXS.put(xlxs(x) & restrict, 0.0);
                            colliderHaCo.put(HashCommon.mix(x) & restrict, 0.0);
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].put(latheCoordConfig(x, y, i + 1) & restrict, 0.0);
//                            }
                        }
//                    System.out.println("INCREASE: " + INCREASE + ", SIZE: " + SIZE);
//                    System.out.println("Base collisions: " + (SIZE - colliderBase.size()));
//                    System.out.println("Lant collisions: " + (SIZE - colliderLant.size()));
//                    System.out.println("XLXS collisions: " + (SIZE - colliderXLXS.size()));
//                    System.out.println("HaCo collisions: " + (SIZE - colliderHaCo.size()));
//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size()));
//                        confTotals[i] += (SIZE - colliders[i].size());
//                    }
                        baseTotal += (SIZE - colliderBase.size());
                        lantTotal += (SIZE - colliderLant.size());
                        xlxsTotal += (SIZE - colliderXLXS.size());
                        hacoTotal += (SIZE - colliderHaCo.size());
                        total += SIZE;
                    }
                }
            }
        }
        System.out.println("Number of ints added: " + total);
        System.out.println("TOTAL Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL Lant collisions: " + lantTotal + " (" + (lantTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL XLXS collisions: " + xlxsTotal + " (" + (xlxsTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL HaCo collisions: " + hacoTotal + " (" + (hacoTotal * 100.0 / total) + "%)");
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }
}
