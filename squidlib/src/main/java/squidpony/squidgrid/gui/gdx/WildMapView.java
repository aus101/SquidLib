package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.ArrayTools;
import squidpony.Maker;
import squidpony.squidgrid.mapping.WildMap;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.Noise;
import squidpony.squidmath.SilkRNG;

import static squidpony.squidgrid.gui.gdx.WorldMapView.*;

/**
 * Created by Tommy Ettinger on 9/6/2019.
 */
public class WildMapView {
    protected int width, height;
    protected float[][] colorMap;
    public WildMap<? extends ICellVisible> wildMap;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float[][] getColorMap() {
        return colorMap;
    }
    
    public WildMap<? extends ICellVisible> getWildMap() {
        return wildMap;
    }

    public void setWildMap(WildMap<? extends ICellVisible> wildMap) {
        this.wildMap = wildMap;
        if(this.width != wildMap.width || this.height != wildMap.height)
        {
            width = wildMap.width;
            height = wildMap.height;
            colorMap = new float[width][height];
        }
    }

    public WildMapView()
    {
        this(null);
    }
    public WildMapView(WildMap<? extends ICellVisible> wildMap)
    {
        if(wildMap == null) {
            final ICellVisible.Named mound = new ICellVisible.Named('∆', SColor.darkenFloat(iceColor, 0.15f), "snow mound");
            final ICellVisible.Named divot = new ICellVisible.Named('˛', SColor.darkenFloat(iceColor, 0.3f), "icy divot");
            final ICellVisible.Named fluff = new ICellVisible.Named('…', SColor.darkenFloat(iceColor, 0.1f), "fluffy powder snow");
            this.wildMap = new WildMap<>(128, 128, 0, new SilkRNG(),
                    Maker.makeList(".snow", ".snow", "_ice"),
                    Maker.makeList(mound, mound, divot, fluff, mound, fluff, divot, fluff, mound, mound, fluff, fluff));
        }
        else
            this.wildMap = wildMap;
        width = this.wildMap.width;
        height = this.wildMap.height;
        colorMap = new float[width][height];
        initialize();
    }
    
    public WildMapView(long seed, int width, int height, int biome)
    {
        this(new WildMap<ICellVisible>(width, height, biome));
    }
    
    protected float[] biomeColors = {
            desertColor,
            savannaColor,
            tropicalRainforestColor,
            grasslandColor,
            woodlandColor,
            seasonalForestColor,
            temperateRainforestColor,
            borealForestColor,
            tundraColor,
            iceColor,
            beachColor,
            rockyColor,
            shallowColor,
            deepColor,
            emptyColor
    };
    public final float[] BIOME_COLOR_TABLE = new float[61], BIOME_DARK_COLOR_TABLE = new float[61];

    public void initialize()
    {
        initialize(0f, 0f, 0f, 1f);
    }

    public void initialize(float hue, float saturation, float brightness, float contrast)
    {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f * contrast;
            BIOME_COLOR_TABLE[i] = b = SColor.toEditedFloat((diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff), hue, saturation, brightness, 0f);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
    }

    /**
     * Initializes the colors to use for each biome (these are almost always mixed with other biome colors in practice).
     * Each parameter may be null to use the default for an Earth-like world; otherwise it should be a libGDX
     * {@link Color} or some subclass, like {@link SColor}. All non-null parameters should probably be fully opaque,
     * except {@code emptyColor}, which is only used for world maps that show empty space (like a globe, as produced by
     * {@link WorldMapGenerator.RotatingSpaceMap}).
     * @param desertColor hot, dry, barren land; may be sandy, but many real-world deserts don't have much sand
     * @param savannaColor hot, mostly-dry land with some parched vegetation; also called scrub or chaparral
     * @param tropicalRainforestColor hot, extremely wet forests with dense rich vegetation
     * @param grasslandColor prairies that are dry and usually wind-swept, but not especially hot or cold
     * @param woodlandColor part-way between a prairie and a forest; not especially hot or cold
     * @param seasonalForestColor forest that becomes barren in winter (deciduous trees); not especially hot or cold
     * @param temperateRainforestColor forest that tends to be slightly warm but very wet
     * @param borealForestColor forest that tends to be cold and very wet
     * @param tundraColor very cold plains that still have some low-lying vegetation; also called taiga 
     * @param iceColor cold barren land covered in permafrost; also used for rivers and lakes that are frozen
     * @param beachColor sandy or otherwise light-colored shorelines; here, these are more common in warmer places
     * @param rockyColor rocky or otherwise rugged shorelines; here, these are more common in colder places
     * @param shallowColor the color of very shallow water; will be mixed with {@code deepColor} to get most ocean colors
     * @param deepColor the color of very deep water; will be mixed with {@code shallowColor} to get most ocean colors
     * @param emptyColor the color used for empty space off the edge of the world map; may be transparent
     */
    public void initialize(
            Color desertColor,
            Color savannaColor,
            Color tropicalRainforestColor,
            Color grasslandColor,
            Color woodlandColor,
            Color seasonalForestColor,
            Color temperateRainforestColor,
            Color borealForestColor,
            Color tundraColor,
            Color iceColor,
            Color beachColor,
            Color rockyColor,
            Color shallowColor,
            Color deepColor,
            Color emptyColor
    )
    {
        biomeColors[ 0] = desertColor == null ? WorldMapView.desertColor : desertColor.toFloatBits();
        biomeColors[ 1] = savannaColor == null ? WorldMapView.savannaColor : savannaColor.toFloatBits();
        biomeColors[ 2] = tropicalRainforestColor == null ? WorldMapView.tropicalRainforestColor : tropicalRainforestColor.toFloatBits();
        biomeColors[ 3] = grasslandColor == null ? WorldMapView.grasslandColor : grasslandColor.toFloatBits();
        biomeColors[ 4] = woodlandColor == null ? WorldMapView.woodlandColor : woodlandColor.toFloatBits();
        biomeColors[ 5] = seasonalForestColor == null ? WorldMapView.seasonalForestColor : seasonalForestColor.toFloatBits();
        biomeColors[ 6] = temperateRainforestColor == null ? WorldMapView.temperateRainforestColor : temperateRainforestColor.toFloatBits();
        biomeColors[ 7] = borealForestColor == null ? WorldMapView.borealForestColor : borealForestColor.toFloatBits();
        biomeColors[ 8] = tundraColor == null ? WorldMapView.tundraColor : tundraColor.toFloatBits();
        biomeColors[ 9] = iceColor == null ? WorldMapView.iceColor : iceColor.toFloatBits();
        biomeColors[10] = beachColor == null ? WorldMapView.beachColor : beachColor.toFloatBits();
        biomeColors[11] = rockyColor == null ? WorldMapView.rockyColor : rockyColor.toFloatBits();
        biomeColors[12] = shallowColor == null ? WorldMapView.shallowColor : shallowColor.toFloatBits();
        biomeColors[13] = deepColor == null ? WorldMapView.deepColor : deepColor.toFloatBits();
        biomeColors[14] = emptyColor == null ? WorldMapView.emptyColor : emptyColor.toFloatBits();
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = b = (diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = biomeColors[14];
        biomeColors[ 0] = WorldMapView.desertColor;
        biomeColors[ 1] = WorldMapView.savannaColor;
        biomeColors[ 2] = WorldMapView.tropicalRainforestColor;
        biomeColors[ 3] = WorldMapView.grasslandColor;
        biomeColors[ 4] = WorldMapView.woodlandColor;
        biomeColors[ 5] = WorldMapView.seasonalForestColor;
        biomeColors[ 6] = WorldMapView.temperateRainforestColor;
        biomeColors[ 7] = WorldMapView.borealForestColor;
        biomeColors[ 8] = WorldMapView.tundraColor;
        biomeColors[ 9] = WorldMapView.iceColor;
        biomeColors[10] = WorldMapView.beachColor;
        biomeColors[11] = WorldMapView.rockyColor;
        biomeColors[12] = WorldMapView.shallowColor;
        biomeColors[13] = WorldMapView.deepColor;
        biomeColors[14] = WorldMapView.emptyColor;
    }


    public void generate()
    {
        wildMap.generate();
        float baseColor = BIOME_COLOR_TABLE[wildMap.biome & 1023];
        int h, change, seed = wildMap.rng.nextInt();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                change = Noise.IntPointHash.hashAll(x, y, 
                        h = seed ^ (wildMap.floorTypes.get(wildMap.floors[x][y]).hashCode()));
                colorMap[x][y] = SColor.toEditedFloat(baseColor,
                        0x1p-12f * ((h & 0xFF) - 0x9F + (change >>> 8 & 0x3F)),
                        0x1p-11f * ((h >>> 8 & 0xFF) - 0x9F + (change >>> 16 & 0x3F)),
                        0x1.3p-12f * ((h >>> 16 & 0xFF) - 0x90 + (change >>> 24 & 0x3F)), 
                        0f);
            }
        }
    }
    
    public void show(SparseLayers layers)
    {
        ArrayTools.insert(colorMap, layers.backgrounds, 0, 0);
        int c;
        for (int x = 0; x < width && x < layers.gridWidth; x++) {
            for (int y = 0; y < height && y < layers.gridHeight; y++) {
                if((c = wildMap.content[x][y]) >= 0)
                    layers.put(x, y, wildMap.contentTypes.get(c));
                else
                    layers.put(x, y, wildMap.floorTypes.get(wildMap.floors[x][y]).charAt(0), SColor.contrastLuma(colorMap[x][y]));
            }
        }
    }
}
