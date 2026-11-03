package Testers;

import Hi_Chan.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Plain-Java tester for Preprocessor (no JUnit needed).
 * Covers FR-01 to FR-04 and FR-07, the error messages (NFR-U1, NFR-U2) and a full
 * run on res/I90_WA_ID.json (ST-01 to ST-04, ST-07, AT-01 to AT-03).
 * main is tested by starting the real program, so its exit status and stderr are checked.
 * Run from the project folder so res/ can be found.
 *
 *   Run: Testers.PreprocessorTester
 */
public class PreprocessorTester {
    static int passed = 0;
    static int failed = 0;

    // ---------- tiny assertion helpers ----------
    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    } // end of check

    static void checkEq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? actual == null : expected.equals(actual);
        if (!ok) {
            System.out.println("        expected: " + expected + "  actual: " + actual);
        }
        check(name, ok);
    } // end of checkEq

    // Builds one feature in the same shape as the NHPN file
    static String feature(String coords, String route, String stfips, String beg, String end) {
        return "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":" + coords + "},"
                + "\"properties\":{\"SIGN1\":\"" + route + "\",\"STFIPS\":\"" + stfips + "\","
                + "\"BEGMP\":" + beg + ",\"ENDMP\":" + end + "}}";
    } // end of feature

    static String collection(String... features) {
        return "{\"type\":\"FeatureCollection\",\"features\":[" + String.join(",", features) + "]}";
    } // end of collection

    static final String HEADER = "GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL";

    // A feature whose points sit on latitude 47 at the given distances (miles) east of -117
    static final double MILE_DEG = 1.0 / GeoHash.haversineMiles(47.0, -117.0, 47.0, -116.0);

    static Preprocessor.RoadFeature lineAt(String stfips, double beg, double end, double... miles) {
        List<double[]> pts = new ArrayList<>();
        for (double m : miles)
            pts.add(new double[]{-117.0 + m * MILE_DEG, 47.0});
        return new Preprocessor.RoadFeature("I90", stfips, beg, end, pts);
    } // end of lineAt

    // Starts "java Hi_Chan.Preprocessor args" as its own program and returns
    // {exit status, stdout, stderr}; JVM notices ("Picked up ...") are dropped
    static Object[] runCapture(String... args) {
        try {
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            List<String> cmd = new ArrayList<>(List.of(javaBin, "-cp", System.getProperty("java.class.path"), "Hi_Chan.Preprocessor"));
            cmd.addAll(Arrays.asList(args));
            Path out = Files.createTempFile("pre_out_", ".txt"), err = Files.createTempFile("pre_err_", ".txt");
            Process p = new ProcessBuilder(cmd).redirectOutput(out.toFile()).redirectError(err.toFile()).start();
            if (!p.waitFor(120, java.util.concurrent.TimeUnit.SECONDS))
                p.destroyForcibly();
            String o = Files.readString(out), e = Files.readString(err);
            Files.deleteIfExists(out);
            Files.deleteIfExists(err);
            StringBuilder cleanErr = new StringBuilder();
            for (String line : e.split("\\R"))
                if (!line.startsWith("Picked up ") && !line.isEmpty())
                    cleanErr.append(line).append("\n");
            return new Object[]{p.exitValue(), o, cleanErr.toString()};
        } catch (Exception ex) {
            return new Object[]{-1, "", "tester could not start the program: " + ex};
        }
    } // end of runCapture

    // Finds res/I90_WA_ID.json from the project folder (or one level up)
    static Path findRes(String name) {
        for (String dir : new String[]{"res", "../res", "Team5_HiChan_Project/res"}) {
            Path p = Paths.get(dir, name);
            if (Files.exists(p))
                return p;
        }
        return null;
    } // end of findRes

    // ---------- getValue ----------
    static void testGetValue() {
        System.out.println("getValue");
        String chunk = "{\"SIGN1\":\"I90\",\"STFIPS\":\"53\",\"BEGMP\":1.5,\"ENDMP\":2.5\n}";

        checkEq("reads quoted string value", "I90", Preprocessor.getValue(chunk, "SIGN1"));
        checkEq("reads quoted number-string", "53", Preprocessor.getValue(chunk, "STFIPS"));
        checkEq("reads number value", "1.5", Preprocessor.getValue(chunk, "BEGMP"));
        checkEq("reads last value (no trailing comma)", "2.5", Preprocessor.getValue(chunk, "ENDMP"));
        checkEq("missing key returns null", null, Preprocessor.getValue(chunk, "NOPE"));
        checkEq("null literal returns null", null, Preprocessor.getValue("{\"SIGN1\":null}", "SIGN1"));
        checkEq("empty value returns null", null, Preprocessor.getValue("{\"SIGN1\":\"\"}", "SIGN1"));
        checkEq("key with no colon returns null", null, Preprocessor.getValue("\"SIGN1\"", "SIGN1"));
        checkEq("handles \\r line ending", "2.5", Preprocessor.getValue("{\"ENDMP\": 2.5\r\n", "ENDMP"));
    } // end of testGetValue

    // ---------- parseFeatures (FR-01) ----------
    static void testParseFeatures() {
        System.out.println("parseFeatures");

        String json = collection(
                feature("[[-117.5,47.5],[-117.4,47.6],[-117.3,47.7]]", "I90", "53", "280.12", "282.5"),
                feature("[[-116.0,47.0],[-115.9,47.1]]", "I90", "16", "0.0", "6.825"));
        List<Preprocessor.RoadFeature> f = Preprocessor.parseFeatures(json);

        checkEq("two features parsed", 2, f.size());
        checkEq("route parsed", "I90", f.get(0).route);
        checkEq("stfips parsed (WA)", "53", f.get(0).stfips);
        checkEq("stfips parsed (ID)", "16", f.get(1).stfips);
        checkEq("begMp parsed", 280.12, f.get(0).begMp);
        checkEq("endMp parsed", 282.5, f.get(0).endMp);
        checkEq("coordinate count (feature 1)", 3, f.get(0).coordinates.size());
        checkEq("first value is LONGITUDE", -117.5, f.get(0).coordinates.get(0)[0]);
        checkEq("second value is LATITUDE", 47.5, f.get(0).coordinates.get(0)[1]);
        checkEq("coordinate order preserved", -117.3, f.get(0).coordinates.get(2)[0]);

        List<Preprocessor.RoadFeature> z = Preprocessor.parseFeatures(
                collection(feature("[[-117.5,47.5,600.2],[-117.4,47.6,601.0]]", "I90", "53", "1", "2")));
        checkEq("3D points still give 2 coords", 2, z.get(0).coordinates.size());

        List<Preprocessor.RoadFeature> sci = Preprocessor.parseFeatures(
                collection(feature("[[-1.175E2,4.75E1]]", "I90", "53", "1", "2")));
        checkEq("scientific notation lon", -117.5, sci.get(0).coordinates.get(0)[0]);

        String nullGeom = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":null,"
                + "\"properties\":{\"SIGN1\":\"I90\",\"STFIPS\":\"53\",\"BEGMP\":1.0,\"ENDMP\":2.0}}]}";
        checkEq("null geometry has 0 coordinates", 0, Preprocessor.parseFeatures(nullGeom).get(0).coordinates.size());

        String missing = "{\"features\":[{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-117.5,47.5]]},"
                + "\"properties\":{\"SIGN1\":\"I90\",\"STFIPS\":\"53\",\"BEGMP\":1.0}}]}";
        checkEq("feature missing ENDMP is skipped", 0, Preprocessor.parseFeatures(missing).size());
        checkEq("empty string gives empty list", 0, Preprocessor.parseFeatures("").size());

        boolean threw = false;
        try {
            Preprocessor.parseFeatures(collection(feature("[[-117.5,47.5]]", "I90", "53", "abc", "2")));
        } catch (RuntimeException e) {
            threw = true;
        }
        check("non-numeric BEGMP throws RuntimeException", threw);
    } // end of testParseFeatures

    // ---------- filterSegments (FR-02) ----------
    static void testFilterSegments() {
        System.out.println("filterSegments");

        Preprocessor.RoadFeature wa = lineAt("53", 1, 2, 0, 1);
        Preprocessor.RoadFeature id = lineAt("16", 1, 2, 0, 1);
        Preprocessor.RoadFeature us2 = new Preprocessor.RoadFeature("US2", "53", 1, 2, null);
        Preprocessor.RoadFeature mt = new Preprocessor.RoadFeature("I90", "30", 1, 2, null);

        PrintStream old = System.out;
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        System.setOut(new PrintStream(report, true));
        List<Preprocessor.RoadFeature> kept;
        try {
            kept = Preprocessor.filterSegments(Arrays.asList(wa, us2, id, mt));
        } finally {
            System.setOut(old);
        }

        checkEq("keeps I-90 WA and ID only", 2, kept.size());
        check("WA kept", kept.contains(wa));
        check("ID kept", kept.contains(id));
        check("original order kept", kept.get(0) == wa && kept.get(1) == id);
        check("US-2 record in skip report", report.toString().contains("Skipped: US2 53"));
        check("other-state I-90 record in skip report", report.toString().contains("Skipped: I90 30"));
        checkEq("skip report has one line per skipped record", 2, report.toString().trim().split("\\R").length);
        checkEq("empty list stays empty", 0, Preprocessor.filterSegments(new ArrayList<>()).size());
    } // end of testFilterSegments

    // ---------- splitAtChannelBoundaries (FR-07) ----------
    static int[] channels(List<HighwaySegment> rows) {
        return rows.stream().mapToInt(HighwaySegment::getChannel).toArray();
    } // end of channels

    static void testSplitter() {
        System.out.println("splitAtChannelBoundaries");

        // no 10-mile mark inside the segment: channel = next multiple of 10 at or above ENDMP
        checkEq("BEGMP 0.0 ENDMP 6.825 gets 10", "[10, 10, 10]",
                Arrays.toString(channels(Preprocessor.splitAtChannelBoundaries(List.of(lineAt("16", 0.0, 6.825, 0, 3, 6.8))))));
        checkEq("ENDMP 10 gets 10", "[10, 10]",
                Arrays.toString(channels(Preprocessor.splitAtChannelBoundaries(List.of(lineAt("53", 6, 10, 0, 4))))));
        checkEq("ENDMP 10.1 gets 20", "[20, 20]",
                Arrays.toString(channels(Preprocessor.splitAtChannelBoundaries(List.of(lineAt("53", 10, 10.1, 0, 0.1))))));
        checkEq("ENDMP 29.9 gets 30", "[30, 30]",
                Arrays.toString(channels(Preprocessor.splitAtChannelBoundaries(List.of(lineAt("53", 25, 29.9, 0, 4.9))))));

        // ST-07: BEGMP 9.0 ENDMP 15.0 splits at the point closest to 1 mile from the first point
        List<HighwaySegment> split = Preprocessor.splitAtChannelBoundaries(
                List.of(lineAt("53", 9.0, 15.0, 0, 0.5, 0.9, 1.2, 3.0, 6.0)));
        checkEq("split at the 0.9-mile point: _10 then _20", "[10, 10, 10, 20, 20, 20]", Arrays.toString(channels(split)));

        // AD-4: pieces sharing BEGMP/ENDMP are measured together, west to east,
        // even when listed out of order and drawn east-to-west
        Preprocessor.RoadFeature east = lineAt("53", 9.0, 15.0, 0.8, 1.1, 3.0);
        Preprocessor.RoadFeature westReversed = lineAt("53", 9.0, 15.0, 0.8, 0.4, 0.0);
        List<HighwaySegment> joined = Preprocessor.splitAtChannelBoundaries(List.of(east, westReversed));
        checkEq("pieces measured as one segment", "[10, 10, 10, 10, 10, 20]", Arrays.toString(channels(joined)));
        check("first row is the westernmost point", joined.get(0).getLon() < joined.get(1).getLon());

        // two marks inside one segment
        List<HighwaySegment> two = Preprocessor.splitAtChannelBoundaries(
                List.of(lineAt("53", 5.0, 25.0, 0, 4.8, 6, 14, 15.2, 19)));
        checkEq("BEGMP 5 ENDMP 25 gives 10, 20 and 30", "[10, 10, 20, 20, 20, 30]", Arrays.toString(channels(two)));

        // row contents
        HighwaySegment r = split.get(0);
        checkEq("one row per point", 6, split.size());
        checkEq("geohash not set yet", null, r.getGeohash());
        check("LAT is the latitude (not swapped)", Math.abs(r.getLat() - 47.0) < 1e-9);
        check("LON is the longitude (not swapped)", Math.abs(r.getLon() + 117.0) < 1e-9);
        checkEq("route kept", "I90", r.getRoute());
        checkEq("state kept", "53", r.getStfips());
        checkEq("BEGMP kept", 9.0, r.getBegMp());
        checkEq("ENDMP kept", 15.0, r.getEndMp());
        checkEq("feature with no points gives no rows", 0,
                Preprocessor.splitAtChannelBoundaries(List.of(new Preprocessor.RoadFeature("I90", "53", 1, 2, null))).size());
    } // end of testSplitter

    // ---------- computeGeohashes + buildSegments (FR-03, FR-04) ----------
    static void testIndexAndSort() {
        System.out.println("computeGeohashes and buildSegments");

        List<HighwaySegment> rows = new ArrayList<>(Arrays.asList(
                new HighwaySegment(null, "I90", "53", 1, 2, 47.6588, -117.4260, 10),
                new HighwaySegment(null, "I90", "16", 0, 6.825, 47.709889, -116.993985, 10),
                new HighwaySegment(null, "I90", "53", 3, 4, 47.6588, -117.4260, 10)));
        Preprocessor.computeGeohashes(rows, 5);

        checkEq("geohash uses LAT then LON", GeoHash.encode(47.6588, -117.4260, 5), rows.get(0).getGeohash());
        checkEq("spec example point is c2kz3", "c2kz3", rows.get(1).getGeohash());
        checkEq("precision 5", 5, rows.get(0).getGeohash().length());

        List<HighwaySegment> sorted = Preprocessor.buildSegments(rows);
        checkEq("same number of rows", 3, sorted.size());
        boolean ascending = true;
        for (int i = 1; i < sorted.size(); i++)
            if (sorted.get(i - 1).getGeohash().compareTo(sorted.get(i).getGeohash()) > 0)
                ascending = false;
        check("ascending geohash order", ascending);
        check("equal geohashes ordered by BEGMP",
                sorted.get(0).getBegMp() == 1 && sorted.get(1).getBegMp() == 3);
        check("input list not changed", rows.get(1).getStfips().equals("16"));
    } // end of testIndexAndSort

    // ---------- writeCsv / readJsonFile ----------
    static void testFiles() throws IOException {
        System.out.println("writeCsv and readJsonFile");

        Path dir = Files.createTempDirectory("pre_test_");
        try {
            Path csv = dir.resolve("segments.csv");
            List<HighwaySegment> rows = List.of(
                    new HighwaySegment("c2kz3", "I90", "16", 0.0, 6.825, 47.709889, -116.993985, 10));
            Preprocessor.writeCsv(rows, csv.toString());
            List<String> lines = Files.readAllLines(csv);
            checkEq("header (FR-03)", HEADER, lines.get(0));
            checkEq("spec example row written", "c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10", lines.get(1));
            checkEq("header + one line per row", 2, lines.size());

            Path json = dir.resolve("crlf.json");
            Files.write(json, collection(feature("[[-117.5,47.5]]", "I90", "53", "1", "2")).replace(",", ",\r\n").getBytes());
            checkEq("CRLF JSON parses (NFR-D1)", 1, Preprocessor.parseFeatures(Preprocessor.readJsonFile(json.toString())).size());

            boolean notFound = false;
            try {
                Preprocessor.readJsonFile(dir.resolve("nope.json").toString());
            } catch (IOException e) {
                notFound = true;
            }
            check("missing JSON file throws IOException", notFound);

            boolean badDir = false;
            try {
                Preprocessor.writeCsv(rows, dir.resolve("no_such_dir/out.csv").toString());
            } catch (IOException e) {
                badDir = true;
            }
            check("unwritable output throws IOException", badDir);
        } finally {
            deleteTree(dir);
        }
    } // end of testFiles

    // ---------- run(): usage and error messages (NFR-U1, NFR-U2) ----------
    static void testRunErrors() throws IOException {
        System.out.println("run errors");

        Path dir = Files.createTempDirectory("pre_test_");
        try {
            String out = dir.resolve("segments.csv").toString();

            Object[] r = runCapture("only_one_arg.json");
            checkEq("wrong argument count exits 1", 1, r[0]);
            check("usage message printed", ((String) r[2]).startsWith("Usage:"));

            String missing = dir.resolve("missing.json").toString();
            r = runCapture(missing, out);
            checkEq("missing file exits 1", 1, r[0]);
            checkEq("missing file message", "ERROR:" + missing + ":file not found", ((String) r[2]).trim());
            check("no stack trace", !((String) r[2]).contains("Exception"));
            check("no output file written", !Files.exists(Paths.get(out)));

            Path empty = dir.resolve("empty.json");
            Files.write(empty, new byte[0]);
            r = runCapture(empty.toString(), out);
            checkEq("empty file message", "ERROR:" + empty + ":file empty", ((String) r[2]).trim());

            Path bad = dir.resolve("bad.json");
            Files.write(bad, collection(feature("[[-117.5,47.5]]", "I90", "53", "abc", "2")).getBytes());
            r = runCapture(bad.toString(), out);
            checkEq("bad number message", "ERROR:" + bad + ":bad value in JSON", ((String) r[2]).trim());

            checkEq("every error is exactly one line", 1, ((String) r[2]).trim().split("\\R").length);
            checkEq("bad JSON exits 1", 1, r[0]);
        } finally {
            deleteTree(dir);
        }
    } // end of testRunErrors

    // ---------- sample_non_i90.json (AT-02, ST-02) ----------
    static void testNonI90Sample() throws IOException {
        System.out.println("sample with a non-I-90 record");

        Path dir = Files.createTempDirectory("pre_test_");
        try {
            String good = feature("[[-117.45,47.66],[-117.40,47.66]]", "I90", "53", "280.12", "282.5");
            Path withUs2 = dir.resolve("sample_non_i90.json");
            Path without = dir.resolve("sample_i90_only.json");
            Files.write(withUs2, collection(good, feature("[[-120.3,47.4]]", "US2", "53", "100", "101"),
                    feature("[[-111.0,46.0]]", "I90", "30", "1", "2")).getBytes());
            Files.write(without, collection(good).getBytes());

            Object[] a = runCapture(withUs2.toString(), dir.resolve("a.csv").toString());
            Object[] b = runCapture(without.toString(), dir.resolve("b.csv").toString());
            checkEq("run succeeds", 0, a[0]);
            check("US-2 record appears in the skip report", ((String) a[1]).contains("Skipped: US2"));
            check("other-state record appears in the skip report", ((String) a[1]).contains("Skipped: I90 30"));
            check("lookup file same as a run without those records",
                    Files.readAllLines(dir.resolve("a.csv")).equals(Files.readAllLines(dir.resolve("b.csv"))));
            check("summary line printed", ((String) b[1]).contains("Read 1 features, wrote 2 rows"));
        } finally {
            deleteTree(dir);
        }
    } // end of testNonI90Sample

    // ---------- full run on the provided NHPN file (AT-01, AT-03, ST-03, ST-04) ----------
    static void testRealData() throws IOException {
        System.out.println("full run on res/I90_WA_ID.json");

        Path json = findRes("I90_WA_ID.json");
        if (json == null) {
            System.out.println("  SKIP  res/I90_WA_ID.json not found (run from the project folder)");
            return;
        }

        Path dir = Files.createTempDirectory("pre_test_");
        try {
            Path csv = dir.resolve("segments.csv");
            long start = System.nanoTime();
            Object[] r = runCapture(json.toString(), csv.toString());
            double seconds = (System.nanoTime() - start) / 1e9;

            checkEq("exit status 0", 0, r[0]);
            check("console: Read 1275 features, wrote 8041 rows", ((String) r[1]).contains("Read 1275 features, wrote 8041 rows"));
            check("runs in under 30 s (ADD section 10)", seconds < 30);
            check("nothing printed to stderr", ((String) r[2]).trim().isEmpty());

            List<String> lines = Files.readAllLines(csv);
            checkEq("header", HEADER, lines.get(0));
            checkEq("one row per coordinate point kept (FR-03)", 8041, lines.size() - 1);

            boolean sorted = true, eightFields = true, channelsOk = true, statesOk = true;
            String prev = "";
            for (String line : lines.subList(1, lines.size())) {
                HighwaySegment s = HighwaySegment.fromCsvLine(line);
                if (s.getGeohash().compareTo(prev) < 0) sorted = false;
                prev = s.getGeohash();
                if (line.split(",", -1).length != 8) eightFields = false;
                if (s.getChannel() % 10 != 0
                        || s.getChannel() < HiChan.computeChannelNumber(s.getBegMp())
                        || s.getChannel() > HiChan.computeChannelNumber(s.getEndMp()))
                    channelsOk = false;
                if (!s.getStfips().equals("53") && !s.getStfips().equals("16")) statesOk = false;
                if (!s.getGeohash().equals(GeoHash.encode(s.getLat(), s.getLon(), 5))) sorted = false;
            }
            check("ascending geohash order and geohash matches LAT/LON (FR-04)", sorted);
            check("every row has 8 fields", eightFields);
            check("every channel is a multiple of 10 within its segment (FR-07)", channelsOk);
            check("only WA and ID rows", statesOk);
            check("spec example row present (c2kz3 ... ,10)", lines.stream().anyMatch(
                    l -> l.startsWith("c2kz3,I90,16,0.0,6.825,47.709889") && l.endsWith(",10")));
            check("Washington reaches channel 300 at the state line",
                    lines.stream().anyMatch(l -> l.contains(",I90,53,") && l.endsWith(",300")));

            Path again = dir.resolve("segments2.csv");
            runCapture(json.toString(), again.toString());
            check("same input gives identical output (NFR-R2)",
                    Arrays.equals(Files.readAllBytes(csv), Files.readAllBytes(again)));
        } finally {
            deleteTree(dir);
        }
    } // end of testRealData

    // ---------- RoadFeature ----------
    static void testRoadFeature() {
        System.out.println("RoadFeature");

        Preprocessor.RoadFeature nullCoords = new Preprocessor.RoadFeature("I90", "16", 0.0, 6.825, null);
        check("null coordinates become empty list", nullCoords.coordinates != null && nullCoords.coordinates.isEmpty());
        checkEq("toString with null coords", "I90 16 MP 0.0-6.825 (0 points)", nullCoords.toString());
        checkEq("toString format", "I90 16 MP 0.0-6.825 (2 points)",
                lineAt("16", 0.0, 6.825, 0, 1).toString());
    } // end of testRoadFeature

    // deletes a temp folder and everything in it
    static void deleteTree(Path dir) throws IOException {
        if (!Files.exists(dir))
            return;
        try (var walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    } // end of deleteTree

    interface Test {
        void run() throws Exception;
    } // end of Test

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== PreprocessorTester ===");
        Test[] tests = {
                PreprocessorTester::testGetValue,
                PreprocessorTester::testParseFeatures,
                PreprocessorTester::testFilterSegments,
                PreprocessorTester::testSplitter,
                PreprocessorTester::testIndexAndSort,
                PreprocessorTester::testFiles,
                PreprocessorTester::testRunErrors,
                PreprocessorTester::testNonI90Sample,
                PreprocessorTester::testRealData,
                PreprocessorTester::testRoadFeature
        };
        for (Test t : tests) {
            try {
                t.run();
            } catch (Throwable e) {
                failed++;
                System.out.println("  FAIL  test crashed: " + e);
            }
        }
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        return failed;
    } // end of runAll

    public static void main(String[] args) {
        if (runAll() > 0)
            System.exit(1);
    } // end of main
} // end of PreprocessorTester
