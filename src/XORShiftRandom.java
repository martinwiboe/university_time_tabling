import java.util.Random;

/**
 * A faster, higher-quality replacement for java.util.Random.
 * See http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
 */
public class XORShiftRandom extends Random {
    private long seed = System.nanoTime();

    public XORShiftRandom() {
    }
    protected int next(int nbits) {
        // N.B. Not thread-safe!
        long x = this.seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        this.seed = x;
        x &= ((1L << nbits) -1);
        return (int) x;
    }
}
