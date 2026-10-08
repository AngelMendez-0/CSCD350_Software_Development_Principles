import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
 * Runs ONCE. Converts the NHPN highway JSON (I90_WA_ID.json) into a plain CSV
 * lookup file indexed by geohash. No database is allowed.
 *
 *   Input JSON  ->  parse features  ->  geohash every coordinate (precision 6)
 *               ->  de-duplicate per feature (Set<String>)  ->  write CSV
 *
 * Output CSV (one row per unique geohash per feature):
 *     GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP
 *     c2kx9p,I90,53,280.12,282.5
 *
 * Facts about the provided JSON (checked):
 *   - Top level is {"type":"FeatureCollection","features":[ ... ]}
 *   - 1275 features: 1148 Washington (STFIPS "53") and 127 Idaho (STFIPS "16")
 *   - Every geometry is a "LineString"
 *   - Each feature has exactly: geometry, SIGN1, STFIPS, BEGMP, ENDMP
 *   - IMPORTANT: coordinates are [LONGITUDE, LATITUDE] (longitude first!)
 *
 * Usage: java Preprocessor I90_WA_ID.json segments.csv
 */
public class Preprocessor {

    static class RoadFeature {
        String route;                 // SIGN1, e.g. "I90"
        String stfips;                // "53" or "16"
        double begMp;                 // BEGMP
        double endMp;                 // ENDMP
        List<double[]> coordinates;   // each double[] is {lon, lat}, same order as the file

        /* Constructor
         * What it does: stores the parsed values for one feature.
         *
         * How to code it:
         *   1. Assign each parameter to its field.
         *   2. If coordinates is null, use a new ArrayList so later loops don't crash.
         */
        RoadFeature(String route, String stfips, double begMp, double endMp, List<double[]> coordinates) {
            // TODO: assign fields
        } // end of RoadFeature

        /*
         * What it does: shows one parsed feature without printing every coordinate.
         * Print features.get(0) after parsing to check your parser works.
         *
         * Example output (first feature in the JSON):
         *   I90 16 MP 0.0-6.825 (13 points)
         */
        @Override
        public String toString() {
            int count = (coordinates == null) ? 0 : coordinates.size();
            return route + " " + stfips + " MP " + begMp + "-" + endMp + " (" + count + " points)";

        } // end of toString
    }

    /*
     * What it does: drives the whole preprocessing run.
     *
     * How to code it:
     *   1. Check args.length >= 2. If not, print a usage message and return.
     *   2. String json = readJsonFile(args[0]);
     *   3. List<RoadFeature> features = parseFeatures(json);
     *   4. List<HighwaySegment> rows = buildSegments(features, GeoHash.DEFAULT_PRECISION);
     *   5. writeCsv(rows, args[1]);
     *   6. Print a summary such as "Read 1275 features, wrote N rows" so you
     *      can see it worked (1275 is the expected feature count).
     *   7. Catch IOException and print a clear error message.
     */
    public static void main(String[] args) {
        // TODO: wire the steps together
    } // end of main

    /*
     * What it does: reads the entire JSON file into one String.
     *
     * How to code it:
     *   1. return Files.readString(Path.of(path));   (Java 11+)
     *      or use a BufferedReader + StringBuilder in a loop.
     *   2. Let IOException propagate up to main.
     *   (The file is about 1 MB, so reading it all at once is fine.)
     */
    public static String readJsonFile(String path) throws IOException {
        // TODO: read file to a String
        return "";
    } // end of readJsonFile

    /*
     * What it does: turns the JSON text into a List<RoadFeature>.
     *
     * How to code it. Pick ONE approach:
     *
     *   Option A: Jackson library (allowed, not required)
     *     1. ObjectMapper mapper = new ObjectMapper();
     *        JsonNode root = mapper.readTree(json);
     *     2. for (JsonNode f : root.get("features")) {
     *            route  = f.get("SIGN1").asText();
     *            stfips = f.get("STFIPS").asText();
     *            begMp  = f.get("BEGMP").asDouble();
     *            endMp  = f.get("ENDMP").asDouble();
     *            for (JsonNode pt : f.get("geometry").get("coordinates"))
     *                coords.add(new double[]{ pt.get(0).asDouble(), pt.get(1).asDouble() });
     *            list.add(new RoadFeature(...));
     *        }
     *
     *   Option B: no library (the file layout is very regular)
     *     1. Split the text into features. Every feature contains "geometry",
     *        so you can find each occurrence of "\"geometry\"" with indexOf in a loop.
     *     2. Inside each feature chunk:
     *        - Find "coordinates": [ ... ]] and pull out all numbers in order
     *          (a regex like  -?\d+\.?\d*  works). Group them into pairs
     *          {lon, lat}.
     *        - Find "SIGN1", "STFIPS", "BEGMP", "ENDMP" with indexOf and read the
     *          value after the colon (remove quotes for the string fields).
     *     3. Build a RoadFeature for each chunk.
     *
     *   Either way: print features.size() while testing. It should be 1275.
     */
    public static List<RoadFeature> parseFeatures(String json) {
        List<RoadFeature> features = new ArrayList<>();
        // TODO: parse the JSON into RoadFeature objects
        return features;
    } // end of parseFeatures

    /*
     * What it does: returns the set of UNIQUE geohashes that this road
     * segment passes through.
     *
     * How to code it:
     *   1. Set<String> hashes = new LinkedHashSet<>();  (keeps insertion order,
     *      removes duplicates; a plain HashSet also works)
     *   2. for each double[] c in feature.coordinates:
     *          double lon = c[0];  double lat = c[1];      <-- watch the order!
     *          hashes.add(GeoHash.encode(lat, lon, precision));
     *   3. (Recommended) Also add hashes for points BETWEEN vertices so a long
     *      straight piece of road doesn't skip a cell. Call densify(...) first
     *      and loop over its output instead of feature.coordinates.
     *   4. return hashes;
     */
    public static Set<String> computeGeohashes(RoadFeature feature, int precision) {
        Set<String> hashes = new LinkedHashSet<>();
        // TODO: geohash every coordinate (and optionally densified points)
        return hashes;
    } // end of computeGeohashes

    /*
     * What it does: inserts extra points along each line piece so consecutive
     * points are never farther apart than maxStepDegrees. This fixes the
     * "line crosses a cell with no vertex in it" problem from PreprocessorLogic.docx.
     *
     * How to code it:
     *   1. Create a new List<double[]> result.
     *   2. For each pair of neighbors A = coords[i], B = coords[i+1]:
     *        a. dist = max(|B.lon - A.lon|, |B.lat - A.lat|)
     *        b. steps = (int) Math.ceil(dist / maxStepDegrees)  (at least 1)
     *        c. for s = 0 .. steps-1:
     *               t = s / (double) steps
     *               add { A.lon + t*(B.lon-A.lon), A.lat + t*(B.lat-A.lat) }
     *   3. Add the very last coordinate.
     *   4. Return result.
     *   A good maxStepDegrees for precision 6 is about 0.002 (smaller than one cell).
     */
    public static List<double[]> densify(List<double[]> coordinates, double maxStepDegrees) {
        List<double[]> result = new ArrayList<>();
        // TODO (optional): interpolate extra points between vertices
        return result;
    } // end of densify

    /*
     * What it does: converts every feature into one HighwaySegment row per
     * unique geohash (Strategy 1 in PreprocessorLogic.docx).
     *
     * How to code it:
     *   1. List<HighwaySegment> rows = new ArrayList<>();
     *   2. for each RoadFeature f:
     *          for each String h in computeGeohashes(f, precision):
     *              rows.add(new HighwaySegment(h, f.route, f.stfips, f.begMp, f.endMp));
     *   3. (Optional) sort rows by geohash with
     *      rows.sort(Comparator.comparing(HighwaySegment::getGeohash))
     *      so the file is easy to scan by eye.
     *   4. return rows;
     */
    public static List<HighwaySegment> buildSegments(List<RoadFeature> features, int precision) {
        List<HighwaySegment> rows = new ArrayList<>();
        // TODO: one row per (feature, unique geohash)
        return rows;
    } // end of buildSegments

    /*
     * What it does: writes the lookup file that HiChan will read.
     *
     * How to code it:
     *   1. Open a PrintWriter (or BufferedWriter) using try-with-resources.
     *   2. Write the header: GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP
     *   3. For each row: writer.println(row.toCsvLine());
     *   4. The header column order must match HighwaySegment.toCsvLine()
     *      and HighwaySegment.fromCsvLine().
     */
    public static void writeCsv(List<HighwaySegment> rows, String path) throws IOException {
        // TODO: write header + rows
    } // end of writeCsv
}