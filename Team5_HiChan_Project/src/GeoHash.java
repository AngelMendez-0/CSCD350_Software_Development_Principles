package Hi_Chan;

/*
 * Converts a latitude/longitude pair into a geohash string.
 * Both the Hi_Chan.Preprocessor (for each road coordinate) and Hi_Chan.HiChan (for each GPS
 * point) use this class, so the two programs MUST produce identical hashes
 * for the same location.
 *
 * Project spec: precision 5 (HiChanConfig.GEOHASH_PRECISION, AD-2).
 * You write this yourself; there is no built-in Java geohash function.
 */
public class GeoHash {

    // The geohash Base32 alphabet. Note it skips the letters a, i, l, and o.
    // Index 0 = '0', index 31 = 'z'.
    public static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    // Default precision (number of characters) used everywhere in this project.
    public static final int DEFAULT_PRECISION = HiChanConfig.GEOHASH_PRECISION;

    /*
     * What it does: returns a geohash string that is `precision` characters long.
     *
     * How to code it:
     *   1. Start with two ranges: latitude [-90, 90] and longitude [-180, 180].
     *   2. Use a StringBuilder for the result, plus three trackers:
     *        - boolean evenBit = true   (true means the next bit is longitude)
     *        - int bit = 0              (counts bits in the current character, 0-4)
     *        - int ch = 0               (the 5-bit value being built)
     *   3. Loop while result.length() < precision:
     *        a. If evenBit, work with longitude, otherwise latitude.
     *        b. Find mid = (min + max) / 2 of that range.
     *        c. If the coordinate >= mid: ch = (ch << 1) | 1 and min = mid.
     *           Otherwise:                ch = (ch << 1)     and max = mid.
     *        d. Flip evenBit and increment bit.
     *        e. Once bit == 5, append BASE32.charAt(ch), then reset bit and ch to 0.
     *   4. Return result.toString().
     *
     * Test it: encode(47.6588, -117.4260, 6) should return "c2krpu" (Spokane)
     * and encode(47.709889, -116.993985, 5) should return "c2kz3".
     * You can check other values on an online geohash site.
     */
    public static String encode(double lat, double lon, int precision) {
        // TODO: implement geohash encoding
        return "";
    } // end of encode

    /*
     * What it does: a convenience overload that uses DEFAULT_PRECISION.
     *
     * How to code it:
     *   1. Return encode(lat, lon, DEFAULT_PRECISION).
     */
    public static String encode(double lat, double lon) {
        // TODO: call the 3-argument version with DEFAULT_PRECISION
        return "";
    } // end of encode

    /*
     * What it does: returns the 8 geohashes surrounding the given cell.
     * Used only as a fallback when a reading's own cell has no highway point (AD-2).
     *
     * How to code it:
     *   1. Decode the geohash back to its cell: reverse the bit steps of encode to get
     *      minLat, maxLat, minLon, maxLon. cellHeight = maxLat - minLat, cellWidth = maxLon - minLon.
     *   2. For each dLat in {-1, 0, 1} and dLon in {-1, 0, 1}, skipping (0,0),
     *      call encode(centerLat + dLat * cellHeight, centerLon + dLon * cellWidth, geohash.length()).
     *      Skip latitudes past 90 or -90; wrap longitudes past 180 or -180.
     *   3. Return the hashes as a List<String> (8 of them, fewer at a pole).
     */
    public static java.util.List<String> neighbors(String geohash) {
        // TODO: compute the surrounding geohash cells
        return new java.util.ArrayList<>();
    } // end of neighbors

    /*
     * What it does: Haversine distance in miles between two latitude/longitude points.
     * Used to find the nearest highway point and the point closest to a 10-mile mark.
     *
     * How to code it:
     *   1. dLat = toRadians(lat2 - lat1), dLon = toRadians(lon2 - lon1)
     *   2. a = sin(dLat/2)^2 + cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLon/2)^2
     *   3. return 2 * 3958.8 * asin(sqrt(a))     (3958.8 = Earth radius in miles)
     */
    public static double haversineMiles(double lat1, double lon1, double lat2, double lon2) {
        // TODO: implement the Haversine formula
        return 0;
    } // end of haversineMiles
} //end of Hi_Chan.GeoHash
