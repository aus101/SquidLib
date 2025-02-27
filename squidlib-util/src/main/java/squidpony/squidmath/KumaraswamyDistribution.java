package squidpony.squidmath;

/**
 * A parameterized IDistribution that can be configured to take various shapes.
 * See <a href="https://en.wikipedia.org/wiki/Kumaraswamy_distribution">Wikipedia's
 * article on this distribution</a> for more.
 */
public class KumaraswamyDistribution extends IDistribution.SimpleDistribution implements IDistribution {
    public static final KumaraswamyDistribution instance = new KumaraswamyDistribution(2.0, 2.0);

    /**
     * Shape parameter a.
     * Stored internally as its reciprocal to avoid dividing extra times in {@link #nextDouble(IRNG)}.
     */
    private double a;
    /**
     * Shape parameter b.
     * Stored internally as its reciprocal to avoid dividing extra times in {@link #nextDouble(IRNG)}.
     */
    private double b;
    public double getA() {return 1.0/a;}
    public void setA(double a) {this.a = 1.0/a;}
    public double getB() {return 1.0/b;}
    public void setB(double b) {this.b = 1.0/b;}

    public KumaraswamyDistribution() {
        this(2.0, 2.0);
    }
    public KumaraswamyDistribution(double a, double b) {
        this.a = 1.0/a;
        this.b = 1.0/b;
    }

    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     *
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    @Override
    public double nextDouble(IRNG rng) {
        return Math.pow(1.0 - Math.pow(1.0 - rng.nextDouble(), b), a);
    }
}
