import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Constructor / Stores all the road data
        RoadFeature(String route, String stfips, double begMp, double endMp, List<double[]> coordinates) {
            super();

            // Routes / States
            this.route = route;
            this.stfips = stfips;

            // Coordinates (makes sure there's no mean nullPointers :/ )
            this.coordinates = (coordinates == null) ? new ArrayList<>() : coordinates;

            // Points
            this.begMp = begMp;
            this.endMp = endMp;
        } // end of RoadFeature

        // Parses output / Example: (I90 16 MP 0.0-6.825)
        @Override
        public String toString() {
            int count = (coordinates == null) ? 0 : coordinates.size();
            return route + " " + stfips + " MP " + begMp + "-" + endMp + " (" + count + " points)";

        } // end of toString
    } // end of RoadFeature

    // Gets the JSON / Parses it / Sends it to output
    // If it fails it throws IOException
    public static void main(String[] args) throws IOException {
        if(args.length >= 2) {
            try {
                // Get JSON
                String json = readJsonFile(args[0]);

                // Parse
                List<RoadFeature> features = parseFeatures(json);
                List<HighwaySegment> rows = buildSegments(features, GeoHash.DEFAULT_PRECISION);

                // Output
                writeCsv(rows, args[1]);
                System.out.println("Read " + features.size() +
                        " features, wrote " + rows.size() + " rows"); // verify it worked via the print

            } catch (IOException e) {
                // oopsie was made
                throw new IOException("Failed to Parse JSON due to: ", e);

            } // end of try catch
        } else {
        System.out.println("Usage: java Preprocessor <input.json> <output.csv>");

        } // end of if
    } // end of main

    // Reads the entire JSON file and turns it into one string
    public static String readJsonFile(String path) throws IOException {

        // Bob the Builder String
        StringBuilder sb = new StringBuilder();

        // Reads the file path and appends each line and creates a new line
        // for each until line is null
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null)
                sb.append(line).append("\n");

        } // end of try

        return sb.toString();
    } // end of readJsonFile

    // Breaks down each feature into smaller substrings called chunks
    // Takes all those chunks at the end and returns them as a fully parsed array called features
    // takes in JSON string and outputs I90 16 MP 0.0-6.825
    public static List<RoadFeature> parseFeatures(String json) {

        List<RoadFeature> features = new ArrayList<>();

        // returns index of the geometry and returns -1 if not found
        int start = json.indexOf("\"geometry\"");

        // matches the pattern where -? is 0 or 1,
        // d+ any digit one or more,
        // .? is a decimal point,
        // d* is zero or more
        Pattern numberPattern = Pattern.compile("-?\\d+\\.?\\d*");

        // while we still have geometry
        while (start != -1) {

            // Geometry
            // get next Geometry
            int next = json.indexOf("\"geometry\"", start  +1);

            // gives the text from start up to next and nothing more
            String chunk = (next == -1) ? json.substring(start) : json.substring(start, next);

            // Coordinates
            // breaks the coordinates into a substring chunk
            String coordText = chunk.substring(chunk.indexOf("\"coordinates\""), chunk.indexOf("\"SIGN1\""));

            // cord list
            List<double[]> coords = new ArrayList<>();

            // takes previous pattern and checks for a match
            Matcher matcher = numberPattern.matcher(coordText);

            // while match is found
            while (matcher.find()) {
                double lon = Double.parseDouble(matcher.group()); // first is always longitude then adds to parse
                matcher.find(); // the next number is always latitude
                double lat = Double.parseDouble(matcher.group()); //adds lat to parse
                coords.add(new double[]{ lon, lat }); // adds both to array list

            } // end of while

            // Last Fields
            String route  = getValue(chunk, "SIGN1");
            String stfips = getValue(chunk, "STFIPS");
            double begMp  = Double.parseDouble(getValue(chunk, "BEGMP"));
            double endMp  = Double.parseDouble(getValue(chunk, "ENDMP"));

            // creates the given feature one at a time and hands them to the aray for storage
            features.add(new RoadFeature(route, stfips, begMp, endMp, coords));

            start = next;
        } // end of while

        return features; // yipee
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

    public static String getValue(String chunk, String key){

        // Where the key is and the colon right after it
        int keyPos = chunk.indexOf("\"" + key + "\"");
        int colon = chunk.indexOf(":", keyPos);

        // The value ends at the next comma or the end of the line ENDMP has no comma
        int comma = chunk.indexOf(",", colon);
        int newline = chunk.indexOf("\n", colon);

        int end;

        if (comma == -1) { // if no comma then new line
            end = newline;

        } else if (newline == -1) { // if no new line then comma
            end = comma;

        } else { // otherwise finds smallest of the two
            end = Math.min(comma, newline);

        } // end of if

        // If neither was found the value runs to the end of the chunk
        if (end == -1)
            end = chunk.length();

        // Cuts it out strip spaces/\r and remove quotes
        return chunk.substring(colon + 1, end).trim().replace("\"", "");

    } // end of getValue

} // end of Preprocessor
