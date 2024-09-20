package EEssentials.util;

import java.util.Random;

/**
 * A class used for determining the result of various levels of randomness.
 */
public class RandomHelper {
    public static int randomIntBetween(int low, int high) {
        return new Random().nextInt((high - low) + 1) + low;
    }
}
