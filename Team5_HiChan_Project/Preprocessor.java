package Hi_Chan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;
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
 *   JSON Reader -> Segment Filter -> Segment Splitter -> Geohash Indexer -> Lookup File Writer
 *
 * Output CSV (one row per highway point, sorted by geohash, FR-03 / FR-04):
 *     GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL
 *     c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10
 *
 * Facts about the provided JSON (checked):
 *   - Top level is {"type":"FeatureCollection","features":[ ... ]}
 *   - 1275 features: 1148 Washington (STFIPS "53") and 127 Idaho (STFIPS "16")
 *   - Every geometry is a "LineString"
 *   - Each feature has exactly: geometry, SIGN1, STFIPS, BEGMP, ENDMP
 *   - IMPORTANT: coordinates are [LONGITUDE, LATITUDE] (longitude first!)
 *
 * Usage: java Hi_Chan.Preprocessor I90_WA_ID.json segments.csv
 */
public class Preprocessor {

    public static class RoadFeature {
        public String route;                 // SIGN1, e.g. "I90"
        public String stfips;                // "53" or "16"
        public double begMp;                 // BEGMP
        public double endMp;                 // ENDMP
        public List<double[]> coordinates;   // each double[] is {lon, lat}, same order as the file

        // Constructor / Stores all the road data
        public RoadFeature(String route, String stfips, double begMp, double endMp, List<double[]> coordinates) {

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
    // Any problem prints one line, ERROR:<file>:<reason>, and exits with status 1 (NFR-U1, NFR-U2)
    public static void main(String[] args) {
        if(args.length == 2) {
            // Get JSON
            String json;
            try {
                json = readJsonFile(args[0]);
            } catch (NoSuchFileException e) {
                System.err.println("ERROR:" + args[0] + ":file not found");
                System.exit(1);
                return;
            } catch (IOException e) {
                System.err.println("ERROR:" + args[0] + ":file not readable");
                System.exit(1);
                return;
            } // end of try catch

            if (json.trim().isEmpty()) {
                System.err.println("ERROR:" + args[0] + ":file empty");
                System.exit(1);
                return;
            } // end of if

            // Parse
            List<RoadFeature> features;
            try {
                features = parseFeatures(json);
            } catch (RuntimeException e) {
                // oopsie was made while parsing (bad number, missing field, etc.)
                System.err.println("ERROR:" + args[0] + ":bad value in JSON");
                System.exit(1);
                return;
            } // end of try catch

            // Filter, split at 10-mile marks, geohash, sort
            List<RoadFeature> kept = filterSegments(features);
            List<HighwaySegment> rows = splitAtChannelBoundaries(kept);
            computeGeohashes(rows, GeoHash.DEFAULT_PRECISION);
            rows = buildSegments(rows);

            // Output
            try {
                writeCsv(rows, args[1]);
            } catch (IOException e) {
                System.err.println("ERROR:" + args[1] + ":cannot write file");
                System.exit(1);
                return;
            } // end of try catch

            System.out.println("Read " + features.size() +
                    " features, wrote " + rows.size() + " rows"); // verify it worked via the print

        } else {
            // Wrong number of arguments so say how to run it and exit with an error code
            System.err.println("Usage: java Hi_Chan.Preprocessor <input.json> <output.csv>");
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

    /*
     * Segment Filter (FR-02)
     * What it does: keeps only features on I-90 in Washington or Idaho and prints a
     * skip report line for every other feature.
     *
     * How to code it:
     *   1. List<RoadFeature> kept = new ArrayList<>();
     *   2. For each feature f:
     *        if f.route equals HiChanConfig.HIGHWAY and f.stfips equals
     *        HiChanConfig.STATE_WA or HiChanConfig.STATE_ID -> kept.add(f)
     *        else -> System.out.println("Skipped: " + f);
     *   3. return kept;  (same order as the input)
     */
    public static List<RoadFeature> filterSegments(List<RoadFeature> features) {
        // TODO: keep I-90 WA/ID features, report the rest
        return new ArrayList<>();
    } // end of filterSegments

    /*
     * Segment Splitter (FR-07, AD-4)
     * What it does: makes one HighwaySegment row per highway point (geohash left null)
     * and gives each point its 10-mile channel.
     *
     * How to code it:
     *   1. Group the features that share route, STFIPS, BEGMP and ENDMP (NHPN splits one
     *      segment into many LineStrings). Keep the groups in file order.
     *   2. For each group, put its points in west-to-east order (mileposts increase going
     *      east): turn any piece whose first longitude is east of its last around, sort the
     *      pieces by their first longitude, then join them into one list of {lon, lat}.
     *   3. Walk the list adding GeoHash.haversineMiles between neighbors, so distance[i] is
     *      the miles from the first point to point i (measured across ALL pieces).
     *   4. channel = HiChan.computeChannelNumber(ENDMP). For each multiple of 10 (B)
     *      strictly between BEGMP and ENDMP, find the point whose distance is closest to
     *      (B - BEGMP): points up to and including it get channel B, later points move on
     *      to the next mark. Example: BEGMP 9.0, ENDMP 15.0 splits at the point closest to
     *      1 mile; points up to it are channel 10, the rest are channel 20.
     *   5. rows.add(new HighwaySegment(null, route, stfips, begMp, endMp, lat, lon, channel))
     *      for every point, in that west-to-east order. Note lat = point[1], lon = point[0].
     */
    public static List<HighwaySegment> splitAtChannelBoundaries(List<RoadFeature> features) {
        // TODO: one row per point with its 10-mile channel
        return new ArrayList<>();
    } // end of splitAtChannelBoundaries

    // Geohash Indexer: sets each row's geohash from its own LAT and LON (latitude first)
    public static void computeGeohashes(List<HighwaySegment> rows, int precision) {

        // adding each point's geohash
        for(HighwaySegment row : rows)
            row.setGeohash(GeoHash.encode(row.getLat(), row.getLon(), precision));

    } // end of computeGeohashes

    // Lookup File Writer: returns the rows sorted by geohash (FR-04)
    public static List<HighwaySegment> buildSegments(List<HighwaySegment> rows) {
        List<HighwaySegment> sorted = new ArrayList<>(rows);

        sorted.sort(Comparator.comparing(HighwaySegment::getGeohash).thenComparingDouble(HighwaySegment::getBegMp));
        //Comparator that sorts alphabetically by geohash string using method reference, then when two rows have same geohash, the smaller BegMP goes first.

        return sorted;
    } // end of buildSegments

    // outputs the CSV with a header and with the rows appended after
    public static void writeCsv(List<HighwaySegment> rows, String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {

            // Header
            pw.println("GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL");

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

} // end of Hi_Chan.Preprocessor
