/*
 * Converts a latitude/longitude pair into a geohash string.
 * Both the Preprocessor (for each road coordinate) and HiChan (for each GPS
 * point) use this class, so the two programs MUST produce identical hashes
 * for the same location.
 *
 * Project spec: precision 6 (a cell is about 0.6 km x 1.2 km at this latitude).
 * You write this yourself; there is no built-in Java geohash function.
 */
public class GeoHash {

    // The geohash Base32 alphabet. Note it skips the letters a, i, l, and o.
    // Index 0 = '0', index 31 = 'z'.
    public static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    // Default precision (number of characters) used everywhere in this project.
    public static final int DEFAULT_PRECISION = 6;

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
     * Test it: encode(47.6588, -117.4260, 6) should return "c2krpu" (Spokane).
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
     * Useful when a GPS point lands in a cell that no road vertex fell into
     * (see the "One Potential Issue" section of PreprocessorLogic.docx).
     *
     * How to code it (one simple approach):
     *   1. Decode the geohash back to its cell center and cell width/height.
     *      (Or keep the lat/lon you started with and the cell size for this
     *      precision.)
     *   2. For each dLat in {-1, 0, 1} and dLon in {-1, 0, 1}, skipping (0,0),
     *      call encode(centerLat + dLat * cellHeight, centerLon + dLon * cellWidth).
     *   3. Return the 8 hashes as a List<String>.
     *
     * If you don't need it, leave it alone. The spec's "read the next GPS
     * point and back-fill" approach also handles misses.
     */
    public static java.util.List<String> neighbors(String geohash) {
        // TODO (optional): compute the 8 surrounding geohash cells
        return new java.util.ArrayList<>();
    } // end of neighbors
} //end of GeoHash