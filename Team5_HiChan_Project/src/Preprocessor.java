import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;
import java.util.HashSet;
import java.io.FileWriter;
import java.io.PrintWriter;

/*
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⡴⠚⢉⡃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣉⠛⠻⢶⢦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠖⢫⢗⣴⡿⣻⠟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠑⢰⣜⣎⣎⠷⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⠞⢁⣴⢣⣫⠌⣽⢃⣀⡤⠤⠀⠀⠀⠀⠀⠀⠀⠤⠤⠤⢤⣀⣠⡟⣟⣘⠘⡆⠑⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠂⠀⡴⠃⣰⡿⣴⡧⠷⠚⠉⠁⣀⣠⠤⢤⣒⣒⣂⣀⣠⣠⣄⣠⣤⣶⣿⡟⠙⠻⢼⣾⡞⡆⠈⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡜⢁⣾⠟⠋⠙⣿⣶⣶⣶⣷⣶⣶⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣶⣦⣤⣌⣙⠿⣕⡀⠿⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣎⠴⠋⠀⣠⠔⣢⣼⣿⣿⣿⣿⣿⡿⠉⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠛⢫⡙⠈⠳⣄⢻⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡄⠀⠀⣠⠞⠁⢀⡤⣚⣵⣿⣿⣿⣿⣿⣿⣿⠏⠀⠀⠀⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⣄⠈⠲⣄⠈⣳⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢿⣶⣴⣉⣀⣴⣫⣾⣿⣿⣿⣿⣿⡟⣿⣿⠋⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⡈⢧⡈⢙⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⢸⣿⠇⠀⠀⠀⠀⠀⠀⠀⢻⣿⣿⣿⠀⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡀⠙⣄⠹⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠋⠀⡙⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⣸⣟⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠀⠀⠘⣿⣿⣿⡿⣿⣿⣿⣿⣿⣿⡟⢿⡜⣎⢦⠙⣷⡀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⡼⠃⢠⣯⣾⣿⣿⣿⣿⣿⣿⣿⣿⠁⠀⣻⡧⣀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣿⢀⣤⠶⢻⣿⣿⡷⢿⣿⣿⣿⣿⣿⣿⣴⡿⡜⣾⡇⠈⣷⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⣼⠁⢠⣿⣿⠟⠋⣽⣿⣿⣿⣿⣿⣧⠶⠚⢻⡏⠛⠲⢦⣀⠀⠀⣠⠀⠀⠀⢿⣿⠋⠀⠀⠸⣿⣿⠀⢸⣿⣿⣿⣿⣿⣿⣿⡅⠙⠀⠘⣄⠙⣧⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢸⠃⠀⣼⡟⠁⠀⣠⣿⣿⣿⣿⣿⣿⠁⠀⠀⠈⢇⠀⠀⠉⠈⠑⠀⠙⢦⠀⠀⢸⡏⠀⢀⣠⣶⣿⠇⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣄⠀⠀⢸⠀⠸⡇⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢸⠀⠀⡇⠁⠄⣰⣿⣿⣿⣿⣿⣿⣇⠀⠀⠠⣄⣿⣶⣦⣄⡄⠀⠀⢄⣿⠆⠀⠘⠃⣰⣿⣷⡿⠿⠶⣦⡀⣿⣿⢻⣿⣿⣿⣿⡝⠻⣦⣠⠀⠇⠀⢳⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢸⠇⠀⢁⣴⡾⠿⢻⣿⣿⣿⣿⣿⣿⠀⣠⣾⠟⠉⠠⠶⠛⠇⠀⠀⣿⠁⠀⠀⠀⠈⠛⣙⣉⣉⣀⣠⡴⠛⣿⡏⣿⣿⣿⣿⣿⡇⠀⠀⠙⠈⢡⠀⣼⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡖⣾⠄⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣟⢿⡇⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠉⠣⠘⠂⠀⣀⣐⣀⣈⣁⡤⠤⠷⠶⣟⠀⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠱⠀⣧⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠁⢯⡆⠐⡄⠀⠀⠀⣾⣿⡇⣿⣿⣿⣆⢹⣷⠞⠚⠋⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠉⠁⠀⠀⢀⠀⠀⢀⠀⢸⢀⣿⣿⣿⣿⠹⣇⠀⠀⠀⠀⡇⠀⣸⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢸⡃⠀⢳⢰⡀⣄⠈⣿⡀⣼⣛⢻⡏⣿⠃⠀⡆⠀⢠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡇⠀⣸⡤⢸⢸⡿⣿⢻⣿⡄⠉⠀⠀⠀⢸⠀⢠⡏⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⢧⠀⠈⣧⢷⡙⣮⢯⡧⣇⣯⣷⢹⣸⡆⠦⢽⣄⣘⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠤⠤⠒⠚⡟⠋⠹⢀⡟⡾⣾⣿⢸⠈⢻⣦⣄⣠⢄⡟⣀⡞⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠈⢣⣤⠘⢿⡻⠈⠉⠁⠹⣜⢯⡓⢧⢳⡀⢸⡁⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠀⢀⡼⣽⢗⣫⢏⡞⠀⠛⠿⡿⢃⡞⠀⡞⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠳⣅⠀⠝⣆⠺⣔⢴⣬⣳⢭⣩⣦⢳⣄⠀⠀⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡞⣱⢗⣋⡽⢋⡀⠀⠀⡀⣠⠊⣰⡜⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠢⣄⠈⢳⣌⡛⠋⠉⠁⠀⠈⠳⢼⡷⢤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⡾⢋⣴⡿⠉⠱⣶⠿⢟⡵⣺⠞⢁⣴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠓⠤⣀⢀⠀⢦⡿⣔⠯⣷⠆⢻⣶⣬⣳⠲⠤⣄⣀⣀⣀⣤⠤⠤⢖⡻⣟⣭⣴⣿⠏⢳⣺⠴⢀⡀⢉⡜⡁⣠⠞⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⢥⣀⠉⠻⣋⠀⠀⠈⠓⢻⢎⠙⠒⠯⣄⡀⢀⣐⡶⠾⠛⠋⡽⣸⠃⠉⠀⠀⠀⢚⡩⠞⢡⣾⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡬⠵⣶⡤⣄⡐⠦⠄⢸⢸⠀⠀⠀⠀⠉⠉⠀⠀⠀⠀⣸⠁⡏⠄⣀⣀⣤⠮⣭⣤⠚⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⡾⢃⣴⣿⣿⣿⣿⣿⣷⣦⡟⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡏⠀⣷⣛⣉⣬⣤⣤⡈⠹⢳⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⠀⣠⠴⠚⠉⠛⠒⢲⡏⣰⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⡞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣧⠀⢻⣿⣿⣿⣿⣿⣿⣧⠈⠙⠦⣄⡤⠴⠦⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⢀⡞⢉⣴⣾⣿⣿⣷⣿⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⣰⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡀⠸⣿⣿⣿⣿⣿⣿⣿⣷⣄⣤⣤⣤⣴⣶⣭⡙⢦⡀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢠⠏⣠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⡴⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⡀⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡄⠱⡄⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡟⢠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡫⠞⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⢤⡙⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⣿⡀⠀⠀⠀⠀
⠀⠀⢀⡴⠞⣡⣬⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠻⠥⠯⢤⣄⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⣤⠤⠽⠦⠭⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡞⢿⣦⡀⠀⠀
⠀⣰⠏⣡⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧⡀⠀⠀⠀⠀⠉⠙⠲⢤⠀⠀⠀⠀⠤⠒⠋⠉⠁⠀⠀⠀⠀⠀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣜⠛⣆⠀
⠼⠁⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡆⠘⢆
*/

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

    // Biggest gap (in degrees) allowed between two consecutive points after densify.
    // A precision 6 geohash cell is about 0.0055 wide and 0.0027 tall, so 0.001
    // is small enough that a line can't hop over a whole cell without a point landing in it.
    static final double MAX_STEP_DEGREES = 0.001;

    static class RoadFeature {
        String route;                 // SIGN1, e.g. "I90"
        String stfips;                // "53" or "16"
        double begMp;                 // BEGMP
        double endMp;                 // ENDMP
        List<double[]> coordinates;   // each double[] is {lon, lat}, same order as the file

        // Constructor / Stores all the road data
        RoadFeature(String route, String stfips, double begMp, double endMp, List<double[]> coordinates) {

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
                // oopsie was made reading or writing a file
                throw new IOException("Failed to read or write a file due to: ", e);

            } catch (RuntimeException e) {
                // oopsie was made while parsing (bad number, missing field, etc.)
                throw new IOException("Failed to Parse JSON due to: ", e);

            } // end of try catch
        } else {
            // Not enough arguments so say how to run it and exit with an error code
            System.err.println("Usage: java Preprocessor <input.json> <output.csv>");
            System.exit(1);

        } // end of if
    } // end of main

    // Reads the entire JSON file and turns it into one string
    public static String readJsonFile(String path) throws IOException {

        // Bob the Builder String
        StringBuilder sb = new StringBuilder();

        // Reads the file path (as UTF-8) and appends each line and creates a new line
        // for each until line is null
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
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

        // matches one number where -? is 0 or 1,
        // d+ any digit one or more,
        // .? is a decimal point,
        // d* is zero or more,
        // and the last part lets scientific notation like 1.2E-5 through
        String number = "-?\\d+\\.?\\d*(?:[eE][-+]?\\d+)?";

        // matches one inner [lon, lat] pair, an extra third value (like elevation) is allowed but ignored
        // group 1 is longitude and group 2 is latitude
        Pattern pairPattern = Pattern.compile("\\[\\s*(" + number + ")\\s*,\\s*(" + number + ")(?:\\s*,\\s*" + number + ")*\\s*\\]");

        // while we still have geometry
        while (start != -1) {

            // Geometry
            // get next Geometry
            int next = json.indexOf("\"geometry\"", start  +1);

            // gives the text from start up to next and nothing more
            String chunk = (next == -1) ? json.substring(start) : json.substring(start, next);

            // Coordinates
            // breaks the coordinates into a substring chunk
            // (empty if the feature has no coordinates, like "geometry": null)
            String coordText = "";
            int coordKey = chunk.indexOf("\"coordinates\"");
            int open = (coordKey == -1) ? -1 : chunk.indexOf("[", coordKey);

            if (open != -1) {
                int depth = 0; // how many brackets deep we are
                int close = chunk.length();

                // walks forward until the bracket that opened the coordinates is closed again
                for (int i = open; i < chunk.length(); i++) {
                    char c = chunk.charAt(i);

                    if (c == '[') {
                        depth++;

                    } else if (c == ']') {
                        depth--;

                        if (depth == 0) {
                            close = i + 1;
                            break;
                        } // end of if
                    } // end of if
                } // end of for

                coordText = chunk.substring(open, close);
            } // end of if

            // cord list
            List<double[]> coords = new ArrayList<>();

            // takes previous pattern and checks for a match
            Matcher matcher = pairPattern.matcher(coordText);

            // while match is found
            while (matcher.find()) {
                double lon = Double.parseDouble(matcher.group(1)); // first is always longitude then adds to parse
                double lat = Double.parseDouble(matcher.group(2)); // the next number is always latitude
                coords.add(new double[]{ lon, lat }); // adds both to array list

            } // end of while

            // Last Fields
            String route  = getValue(chunk, "SIGN1");
            String stfips = getValue(chunk, "STFIPS");
            String begText = getValue(chunk, "BEGMP");
            String endText = getValue(chunk, "ENDMP");

            // only keep the feature if every field was actually there (skips broken or null ones)
            if (route != null && stfips != null && begText != null && endText != null) {
                double begMp  = Double.parseDouble(begText);
                double endMp  = Double.parseDouble(endText);

                // creates the given feature one at a time and hands them to the array for storage
                features.add(new RoadFeature(route, stfips, begMp, endMp, coords));

            } // end of if

            start = next;
        } // end of while

        return features; // yipee
    } // end of parseFeatures

    // converts coordinates and road features into a geohash and returns only unique ones
    public static Set<String> computeGeohashes(RoadFeature feature, int precision) {

        // creates the set that remembers the order things are added
        Set<String> hashes = new LinkedHashSet<>();

        // fills in extra points first so no geohash cell the line crosses gets skipped
        List<double[]> dense = densify(feature.coordinates, MAX_STEP_DEGREES);

        // adding each coordinate pair
        for(double[] coord : dense) {
            double lon = coord[0];
            double lat = coord[1];

            // turn it into a geohash and add it while duplicates are ignored
            hashes.add(GeoHash.encode(lat, lon, precision));
        } // end of for

        return hashes; // yipee
    } // end of computeGeohashes

    // Inserts extra points along each line piece so consecutive points are never farther apart than maxStepDegrees
    // this fixes when a line crosses a cell with no vertex in it
    public static List<double[]> densify(List<double[]> coordinates, double maxStepDegrees) {

        // creating double array list
        List<double[]> result = new ArrayList<>();

        // for index in coordinates
        for(int i = 0; i < coordinates.size() - 1; i++){

            double[] a = coordinates.get(i); // create cord A
            double[] b = coordinates.get(i + 1); // create cord B
            double dist = Math.max(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1])); // get the max distance
            int steps = Math.max(1, (int) Math.ceil(dist/maxStepDegrees)); // get the max steps

            // for steps in steps create a new double for each step
            for(int s = 0; s < steps; s++) {
                double t = s / (double) steps;
                result.add(new double[] {
                        a[0] + t * (b[0] - a[0]),
                        a[1] + t * (b[1] - a[1])
                });
            } // end of for
        } // end of for

        // If coordinates not empty add coordinates to result
        if (!coordinates.isEmpty())
            result.add(coordinates.get(coordinates.size() - 1));

        // returning result
        return result;
    } // end of densify


    // converts every feature into one HighwaySegment row per unique geohash
    public static List<HighwaySegment> buildSegments(List<RoadFeature> features, int precision) {
        List<HighwaySegment> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>(); //HashSet holds each value at most once, efficient for checking values inside.

        for (RoadFeature f : features) { //Outer loop over the 1,275 road pieces.
            for(String h : computeGeohashes(f, precision)) { //Inner Loop of Strings made by computeGeohashes using f road piece with given precision.
                String key = h + "|" + f.route + "|" + f.stfips + "|" + f.begMp + "|" + f.endMp; //key is the row identifying string, exists so HashSet can compare.
                if (seen.add(key)) { //add() inserts the value and returns true if not present, returns false if row already exists there.
                    rows.add(new HighwaySegment(h, f.route, f.stfips, f.begMp, f.endMp));
                    //Constructor takes the 5 values in order and object goes in output list.
                } // end of if

            } // end of for
        } // end of for

        rows.sort(Comparator.comparing(HighwaySegment::getGeohash).thenComparingDouble(HighwaySegment::getBegMp));
        //Comparator that sorts alphabetically by geohash string using method reference, then when two rows have same geohash, the smaller BegMP goes first.

        return rows;
    } // end of buildSegments

    // outputs the CSV with a header and with the rows appended after
    public static void writeCsv(List<HighwaySegment> rows, String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {

            // Header
            pw.println("GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP");

            // appends to rows from toCsvLine
            for(HighwaySegment row : rows)
                pw.println(row.toCsvLine());

        } // end of try
    } // end of writeCsv

    // Finds the value for a key in a chunk, returns null if the key (or its value) isn't there
    public static String getValue(String chunk, String key){

        // Where the key is and the colon right after it
        int keyPos = chunk.indexOf("\"" + key + "\"");

        // key isn't in this chunk so there is nothing to read
        if (keyPos == -1)
            return null;

        int colon = chunk.indexOf(":", keyPos);

        // no colon means the key has no value
        if (colon == -1)
            return null;

        // The value ends at the next comma, closing brace or the end of the line (ENDMP has no comma)
        int comma = chunk.indexOf(",", colon);
        int brace = chunk.indexOf("}", colon);
        int newline = chunk.indexOf("\n", colon);

        // finds the smallest of the three that actually exist
        int end = -1;

        if (comma != -1)
            end = comma;

        if (brace != -1 && (end == -1 || brace < end))
            end = brace;

        if (newline != -1 && (end == -1 || newline < end))
            end = newline;

        // If none were found the value runs to the end of the chunk
        if (end == -1)
            end = chunk.length();

        // Cuts it out strip spaces/\r and remove quotes
        String value = chunk.substring(colon + 1, end).trim().replace("\"", "");

        // an empty or null value counts as missing
        if (value.isEmpty() || value.equals("null"))
            return null;

        return value;

    } // end of getValue

} // end of Preprocessor
