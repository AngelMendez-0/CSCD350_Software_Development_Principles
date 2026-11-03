package Hi_Chan;

/*
 * Every tunable value for both programs lives here (NFR-M2).
 * Change a value here and the Preprocessor and HiChan both follow it.
 */
public final class HiChanConfig {

    public static final String HIGHWAY = "I90"; // highway kept by the Segment Filter
    public static final String STATE_WA = "53"; // Washington FIPS code
    public static final String STATE_ID = "16"; // Idaho FIPS code
    public static final int GEOHASH_PRECISION = 5; // geohash length (AD-2)
    public static final int CHANNEL_MILES = 10; // miles per channel (FR-07, FR-08)
    public static final String CHANNEL_PREFIX = "#"; // start of every Channel ID (FR-13)
    public static final double SAME_LOCATION_TOLERANCE = 0.00001; // degrees, about 1 m (FR-11)

    // Constants only, so nobody should make one of these
    private HiChanConfig() {
    } // end of HiChanConfig
} // end of HiChanConfig
