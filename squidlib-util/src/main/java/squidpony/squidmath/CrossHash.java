package squidpony.squidmath;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * These use the Fowler/Noll/Vo Hash (FNV-1a) algorithm, which is public domain.
 * The hashes this returns are always 0 when given null to hash. Arrays with identical
 * elements of identical types will hash identically. Arrays with identical numerical
 * values but different types will hash differently. There are faster hashes out there,
 * but many of them are intended to run on very modern desktop processors (not, say,
 * Android phone processors, and they don't need to worry about uncertain performance
 * on GWT regarding 64-bit math). We probably don't need to hash arrays or Strings so
 * often that this (still very high-performance!) hash would be a bottleneck.
 * <br>
 * Note: This class was formerly called StableHash, but since that refers to a specific
 * category of hashing algorithm that this is not, and since the goal is to be cross-
 * platform, the name was changed to CrossHash.
 * Created by Tommy Ettinger on 1/16/2016.
 * @author Glenn Fowler
 * @author Phong Vo
 * @author Landon Curt Noll
 * @author Tommy Ettinger
 */
public class CrossHash {
    public static int hash(boolean[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length, o = 0;
        for (int i = 0; i < len; i++) {
            o |= (data[i]) ? (1 << (i % 8)) : 0;
            if(i % 8 == 7) {
                h ^= o;
                h *= 16777619;
                o = 0;
            }
        }
        return h;
    }
    public static int hash(byte[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i];
            h *= 16777619;
        }
        return h;
    }
    public static int hash(char[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 8;
            h *= 16777619;
        }
        return h;
    }
    public static int hash(short[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 8;
            h *= 16777619;
        }
        return h;
    }
    public static int hash(int[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= (data[i] >>> 8) & 0xff;
            h *= 16777619;
            h ^= (data[i] >>> 16) & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 24;
            h *= 16777619;
        }
        return h;
    }
    public static int hash(long[] data)
    {
        if(data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= (int)(data[i] & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 8) & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 16) & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 24) & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 32) & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 40) & 0xff);
            h *= 16777619;
            h ^= (int)((data[i] >>> 48) & 0xff);
            h *= 16777619;
            h ^= (int)(data[i] >>> 56);
            h *= 16777619;
        }
        return h;
    }
    public static int hash(String s)
    {
        if(s == null)
            return 0;
        return hash(s.toCharArray());
    }
}