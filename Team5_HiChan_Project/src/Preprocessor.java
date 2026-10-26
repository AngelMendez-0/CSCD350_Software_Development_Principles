import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

   // converts coordinates and road features into a geohash and returns only unique ones
    public static Set<String> computeGeohashes(RoadFeature feature, int precision) {

        // creates the set that remembers the order things are added
        Set<String> hashes = new LinkedHashSet<>();

        // adding each coordinate pair
        for(double[] coord : feature.coordinates) {
            double lon = coord[0];
            double lat = coord[1];

            // turn it into a geohash and add it while duplicates are ignored
            hashes.add(GeoHash.encode(lat, lon, precision));
        } // end of for

        return hashes; // yipee
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
        for(int i = 0; i < coordinates.size() - 1; i++){
            double[] a = coordinates.get(i);
            double[] b = coordinates.get(i + 1);
            double dist = Math.max(Math.abs(b[0] - a[0]), Math.abs(b[1] - a[1]));
            int steps = Math.max(1, (int) Math.ceil(dist/maxStepDegrees));

            for(int s = 0; s < steps; s++) {
                double t = s / (double) steps;
                result.add(new double[] {
                        a[0] + t * (b[0] - a[0]),
                        a[1] + t * (b[1] - a[1])
                });
            }
        }

        if (!coordinates.isEmpty()) result.add(coordinates.get(coordinates.size() - 1));
        
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
                }

            }
        }

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
