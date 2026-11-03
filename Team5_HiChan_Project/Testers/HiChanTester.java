package Testers;

import Hi_Chan.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/*
 * Plain-Java tester for HiChan (no JUnit needed).
 * Unit tests for each method (FR-05 to FR-14), the error messages (NFR-U1, NFR-U2), and
 * end-to-end runs on the two provided GPS logs that check the Acceptance Test Plan
 * (AT-04 to AT-14) and NFR-R1, NFR-R2, NFR-S1, NFR-P2, NFR-D1.
 * main is tested by starting the real program, so its exit status and stderr are checked.
 * Run from the project folder so res/ can be found.
 *
 *   Run: Testers.HiChanTester
 */
public class HiChanTester {

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
        if (!ok)
            System.out.println("        expected: " + expected + "  actual: " + actual);
        check(name, ok);
    } // end of checkEq

    // ---------- helpers that build test data ----------
    static final String HEADER = "date time,latitude,longitude,altitude(m),speed(m/s),sat_used";
    static final String LOOKUP_HEADER = "GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL";

    // a highway point; geohash comes from its position
    static HighwaySegment point(String stfips, double beg, double end, double lat, double lon, int channel) {
        return new HighwaySegment(GeoHash.encode(lat, lon, 5), "I90", stfips, beg, end, lat, lon, channel);
    } // end of point

    // a parsed GPS record already matched to the given point
    static GpsRecord matched(HighwaySegment seg, String channelId) {
        GpsRecord r = GpsRecord.fromCsvLine("2026-09-22 23:00:00.000," + seg.getLat() + "," + seg.getLon() + ",600,25,20");
        r.setSegment(seg);
        r.setChannelId(channelId);
        return r;
    } // end of matched

    static Map<String, List<HighwaySegment>> indexOf(HighwaySegment... points) {
        Map<String, List<HighwaySegment>> index = new HashMap<>();
        for (HighwaySegment p : points)
            index.computeIfAbsent(p.getGeohash(), k -> new ArrayList<>()).add(p);
        return index;
    } // end of indexOf

    static String gpsLine(String time, double lat, double lon) {
        return "2026-09-22 " + time + "," + lat + "," + lon + ",600.0,25.0,20";
    } // end of gpsLine

    // Starts "java <mainClass> args" as its own program and returns
    // {exit status, stdout, stderr}; JVM notices ("Picked up ...") are dropped
    static Object[] exec(String mainClass, String... args) {
        try {
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            List<String> cmd = new ArrayList<>(List.of(javaBin, "-cp", System.getProperty("java.class.path"), mainClass));
            cmd.addAll(Arrays.asList(args));
            Path out = Files.createTempFile("hc_out_", ".txt"), err = Files.createTempFile("hc_err_", ".txt");
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
    } // end of exec

    // runs HiChan as a program
    static Object[] runCapture(String... args) {
        return exec("Hi_Chan.HiChan", args);
    } // end of runCapture

    // builds the lookup file with the Preprocessor program; returns its exit status
    static int quietPreprocess(String json, String csv) {
        return (Integer) exec("Hi_Chan.Preprocessor", json, csv)[0];
    } // end of quietPreprocess

    // a record for a line that couldn't be read (FR-14)
    static GpsRecord bad(String line, String reason) {
        GpsRecord r = new GpsRecord(line, null, 0, 0, 0, 0, 0);
        r.setErrorText(reason);
        return r;
    } // end of bad

    static Path findRes(String name) {
        for (String dir : new String[]{"res", "../res", "Team5_HiChan_Project/res"}) {
            Path p = Paths.get(dir, name);
            if (Files.exists(p))
                return p;
        }
        return null;
    } // end of findRes

    static String sha256(Path p) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p));
        StringBuilder sb = new StringBuilder();
        for (byte b : d)
            sb.append(String.format("%02x", b));
        return sb.toString();
    } // end of sha256

    static void deleteTree(Path dir) throws IOException {
        if (!Files.exists(dir))
            return;
        try (var walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
        }
    } // end of deleteTree

    // ---------- computeChannelNumber (FR-08) ----------
    static void testComputeChannelNumber() {
        System.out.println("computeChannelNumber");

        checkEq("milepost 0 -> 10", 10, HiChan.computeChannelNumber(0));
        checkEq("milepost 6.8 -> 10", 10, HiChan.computeChannelNumber(6.8));
        checkEq("milepost 10 -> 10 (inclusive upper bound)", 10, HiChan.computeChannelNumber(10));
        checkEq("milepost 10.1 -> 20", 20, HiChan.computeChannelNumber(10.1));
        checkEq("milepost 29.9 -> 30", 30, HiChan.computeChannelNumber(29.9));
        checkEq("milepost 297.52 -> 300 (WA state line)", 300, HiChan.computeChannelNumber(297.52));
        checkEq("milepost 6.825 -> 10", 10, HiChan.computeChannelNumber(6.825));
    } // end of testComputeChannelNumber

    // ---------- buildChannelId (FR-10, FR-13) ----------
    static void testBuildChannelId() {
        System.out.println("buildChannelId");

        HighwaySegment wa = point("53", 280, 290, 47.66, -117.3, 290);
        HighwaySegment id = point("16", 0, 6.825, 47.71, -116.99, 10);

        checkEq("WA eastbound", "#I90WAEB_290", HiChan.buildChannelId(wa, "EB"));
        checkEq("ID westbound", "#I90IDWB_10", HiChan.buildChannelId(id, "WB"));
        checkEq("unknown direction gives ?? (FR-10)", "#I90WA??_290", HiChan.buildChannelId(wa, "??"));
        checkEq("null direction gives ??", "#I90ID??_10", HiChan.buildChannelId(id, null));
        checkEq("null point gives null", null, HiChan.buildChannelId(null, "EB"));
        check("matches ^#I90(WA|ID)(EB|WB|\\?\\?)_\\d+$ (FR-13)",
                Pattern.matches("^#I90(WA|ID)(EB|WB|\\?\\?)_\\d+$", HiChan.buildChannelId(wa, "??")));

    } // end of testBuildChannelId

    // ---------- determineDirection (FR-09, FR-10) ----------
    static void testDetermineDirection() {
        System.out.println("determineDirection");

        HighwaySegment mp10 = point("53", 10, 11, 47.0, -118.0, 20);
        HighwaySegment mp11 = point("53", 11, 12, 47.0, -117.9, 20);
        HighwaySegment mp12 = point("53", 12, 13, 47.0, -117.8, 20);
        HighwaySegment waEdge = point("53", 296, 297.52, 47.69, -117.05, 300);
        HighwaySegment idStart = point("16", 0, 6.825, 47.70, -117.03, 10);

        List<GpsRecord> up = List.of(matched(mp10, null), matched(mp11, null), matched(mp12, null));
        checkEq("entry 0 is ??", "??", HiChan.determineDirection(up, 0));
        checkEq("entry 1 is ?? (fewer than 3 readings)", "??", HiChan.determineDirection(up, 1));
        checkEq("rising mileposts are EB", "EB", HiChan.determineDirection(up, 2));

        List<GpsRecord> down = List.of(matched(mp12, null), matched(mp11, null), matched(mp10, null));
        checkEq("falling mileposts are WB", "WB", HiChan.determineDirection(down, 2));

        List<GpsRecord> east = List.of(matched(waEdge, null), matched(waEdge, null), matched(idStart, null));
        checkEq("WA -> ID is EB even though milepost drops", "EB", HiChan.determineDirection(east, 2));

        List<GpsRecord> west = List.of(matched(idStart, null), matched(idStart, null), matched(waEdge, null));
        checkEq("ID -> WA is WB even though milepost rises", "WB", HiChan.determineDirection(west, 2));

        // compares i with i-2, so a jittery middle reading doesn't matter (AD-6)
        List<GpsRecord> jitter = List.of(matched(mp10, null), matched(mp12, null), matched(mp11, null));
        checkEq("entry i vs entry i-2 (middle ignored)", "EB", HiChan.determineDirection(jitter, 2));

        // same segment keeps the previous direction
        List<GpsRecord> same = List.of(matched(mp11, "#I90WAWB_20"), matched(mp11, "#I90WAWB_20"), matched(mp11, null));
        checkEq("same segment keeps previous direction", "WB", HiChan.determineDirection(same, 2));
        List<GpsRecord> sameUnknown = List.of(matched(mp11, null), matched(mp11, "#I90WA??_20"), matched(mp11, null));
        checkEq("same segment with no known direction stays ??", "??", HiChan.determineDirection(sameUnknown, 2));

        // malformed lines are skipped when counting back
        List<GpsRecord> withBad = List.of(matched(mp10, null), bad("bad", "x"),
                matched(mp11, null), matched(mp12, null));
        checkEq("malformed entries are skipped", "EB", HiChan.determineDirection(withBad, 3));
        checkEq("only 2 valid entries is ?? ", "??", HiChan.determineDirection(withBad, 2));
    } // end of testDetermineDirection

    // ---------- findBestSegment (FR-06) ----------
    static void testFindBestSegment() {
        System.out.println("findBestSegment");

        HighwaySegment near = point("53", 280, 282, 47.6600, -117.3000, 290);
        HighwaySegment far = point("53", 282, 284, 47.6610, -117.2900, 290);
        Map<String, List<HighwaySegment>> index = indexOf(near, far);

        GpsRecord r = GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 47.6601, -117.3001));
        r.setGeohash(GeoHash.encode(r.getLatitude(), r.getLongitude(), 5));
        check("nearest point in the reading's own cell", HiChan.findBestSegment(r, index, null) == near);

        GpsRecord noHash = GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 47.6601, -117.3001));
        check("works before the geohash is set", HiChan.findBestSegment(noHash, index, null) == near);

        // reading in an empty cell next to a cell with a point: neighbor fallback (AD-2)
        double cellWidth = 360.0 / 8192;  // width of a precision-5 cell in degrees
        GpsRecord edge = GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 47.6600, -117.3000 + cellWidth));
        Map<String, List<HighwaySegment>> only = indexOf(near);
        check("neighbor cells searched when own cell is empty", HiChan.findBestSegment(edge, only, null) == near);

        GpsRecord nowhere = GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 10.0, 10.0));
        checkEq("no point nearby gives null", null, HiChan.findBestSegment(nowhere, index, null));

        // exact tie: same state as the previous point wins
        HighwaySegment waTie = point("53", 297, 297.52, 47.6966, -117.0417, 300);
        HighwaySegment idTie = point("16", 0, 0.5, 47.6966, -117.0417, 10);
        GpsRecord atLine = GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 47.6966, -117.0417));
        check("tie goes to the previous state (ID)", HiChan.findBestSegment(atLine, indexOf(waTie, idTie), idTie) == idTie);
        check("tie goes to the previous state (WA)", HiChan.findBestSegment(atLine, indexOf(idTie, waTie), waTie) == waTie);
    } // end of testFindBestSegment

    // ---------- backfillChannelIds (FR-12, FR-10) ----------
    static void testBackfill() {
        System.out.println("backfillChannelIds");

        HighwaySegment a = point("53", 280, 290, 47.66, -117.3, 290);
        HighwaySegment b = point("53", 290, 297.52, 47.69, -117.1, 300);

        List<GpsRecord> recs = new ArrayList<>(List.of(matched(a, "#I90WA??_290"), matched(a, "#I90WA??_290"),
                matched(b, "#I90WAEB_300"), matched(b, "#I90WA??_300"), bad("bad", "x")));
        HiChan.backfillChannelIds(recs);
        checkEq("entry 0 back-filled with first known direction", "#I90WAEB_290", recs.get(0).getChannelId());
        checkEq("entry 1 back-filled", "#I90WAEB_290", recs.get(1).getChannelId());
        checkEq("known ID unchanged", "#I90WAEB_300", recs.get(2).getChannelId());
        checkEq("later ?? takes the most recent direction", "#I90WAEB_300", recs.get(3).getChannelId());
        check("malformed line left alone", recs.get(4).getErrorText() != null && recs.get(4).getChannelId() == null);

        List<GpsRecord> twoOnly = new ArrayList<>(List.of(matched(a, "#I90WA??_290"), matched(a, "#I90WA??_290")));
        HiChan.backfillChannelIds(twoOnly);
        checkEq("direction never known: ?? stays (FR-10)", "#I90WA??_290", twoOnly.get(1).getChannelId());
    } // end of testBackfill

    // ---------- processRecords (FR-06, FR-09, FR-11) ----------
    static void testProcessRecords() {
        System.out.println("processRecords");

        HighwaySegment p1 = point("53", 280, 281, 47.6600, -117.3000, 290);
        HighwaySegment p2 = point("53", 281, 282, 47.6600, -117.2700, 290);
        HighwaySegment p3 = point("53", 282, 283, 47.6600, -117.2400, 290);
        HighwaySegment p4 = point("53", 283, 284, 47.6600, -117.2100, 290);
        Map<String, List<HighwaySegment>> index = indexOf(p1, p2, p3, p4);

        List<GpsRecord> recs = new ArrayList<>();
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:00:00.000", 47.6600, -117.3000)));
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:00:10.000", 47.6600, -117.2700)));
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:00:20.000", 47.6600, -117.2400)));
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:00:30.000", 47.6600, -117.2400)));   // stopped
        recs.add(bad("2026-09-22 23:00:40.000,bad", "expected 6 fields but found 2"));
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:00:50.000", 47.6600, -117.2100)));
        recs.add(GpsRecord.fromCsvLine(gpsLine("23:01:00.000", 10.0, 10.0)));           // nowhere near I-90

        HiChan.processRecords(recs, index);
        checkEq("geohash set (precision 5)", GeoHash.encode(47.66, -117.3, 5), recs.get(0).getGeohash());
        check("segment matched", recs.get(1).getSegment() == p2);
        checkEq("first entry is ?? before back-fill", "#I90WA??_290", recs.get(0).getChannelId());
        checkEq("third entry sets EB", "#I90WAEB_290", recs.get(2).getChannelId());
        checkEq("stopped truck reuses the ID (FR-11)", recs.get(2).getChannelId(), recs.get(3).getChannelId());
        check("malformed line untouched", recs.get(4).getErrorText() != null && recs.get(4).getSegment() == null);
        checkEq("direction continues after a bad line", "#I90WAEB_290", recs.get(5).getChannelId());
        checkEq("reading far from I-90 is marked", "no I-90 point near reading", recs.get(6).getErrorText());

        HiChan.backfillChannelIds(recs);
        checkEq("after back-fill no ?? remains", "#I90WAEB_290", recs.get(0).getChannelId());
    } // end of testProcessRecords

    // ---------- loadIndex (FR-04, NFR-U2) ----------
    static void testLoadIndex() throws IOException {
        System.out.println("loadIndex");

        Path dir = Files.createTempDirectory("hichan_test_");
        try {
            Path good = dir.resolve("segments.csv");
            Files.write(good, (LOOKUP_HEADER + "\n"
                    + "c2kq9,I90,53,280.0,282.0,47.65,-117.37,290\n"
                    + "c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10\n"
                    + "c2kz3,I90,16,0.0,6.825,47.7099,-116.9936,10\n").getBytes());
            Map<String, List<HighwaySegment>> index = HiChan.loadIndex(good.toString());
            checkEq("two geohash cells", 2, index.size());
            checkEq("two points in cell c2kz3", 2, index.get("c2kz3").size());
            checkEq("point parsed", 290, index.get("c2kq9").get(0).getChannel());

            Path crlf = dir.resolve("crlf.csv");
            Files.write(crlf, new String(Files.readAllBytes(good)).replace("\n", "\r\n").getBytes());
            checkEq("CRLF lookup file loads (NFR-D1)", 2, HiChan.loadIndex(crlf.toString()).size());

            checkEq("missing file", "file not found", reasonOf(dir.resolve("nope.csv")));
            Path empty = dir.resolve("empty.csv");
            Files.write(empty, new byte[0]);
            checkEq("empty file", "file empty", reasonOf(empty));
            Path headerOnly = dir.resolve("header.csv");
            Files.write(headerOnly, (LOOKUP_HEADER + "\n").getBytes());
            checkEq("header with no rows", "file empty", reasonOf(headerOnly));
            Path oldHeader = dir.resolve("old.csv");
            Files.write(oldHeader, "GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP\nc2kx9p,I90,53,280.12,282.5\n".getBytes());
            checkEq("old 5-column header", "wrong header line", reasonOf(oldHeader));
            Path unsorted = dir.resolve("unsorted.csv");
            Files.write(unsorted, (LOOKUP_HEADER + "\n"
                    + "c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10\n"
                    + "c2kq9,I90,53,280.0,282.0,47.65,-117.37,290\n").getBytes());
            checkEq("unsorted file", "lookup file not sorted", reasonOf(unsorted));
            Path badRow = dir.resolve("badrow.csv");
            Files.write(badRow, (LOOKUP_HEADER + "\nc2kz3,I90,16,x,6.825,47.7,-116.9,10\n").getBytes());
            checkEq("bad row", "bad row at line 2", reasonOf(badRow));
        } finally {
            deleteTree(dir);
        }
    } // end of testLoadIndex

    static String reasonOf(Path lookup) {
        try {
            HiChan.loadIndex(lookup.toString());
            return "no error";
        } catch (IOException e) {
            return e.getMessage();
        }
    } // end of reasonOf

    // ---------- readGpsLog + writeOutput (FR-05, FR-12, FR-14, NFR-D1) ----------
    static void testReadAndWrite() throws IOException {
        System.out.println("readGpsLog and writeOutput");

        Path dir = Files.createTempDirectory("hichan_test_");
        try {
            String l1 = gpsLine("23:00:00.000", 47.66, -117.30);
            String bad = "2026-09-22 23:00:10.000,47.66";
            String l3 = gpsLine("23:00:20.000", 47.66, -117.29);
            Path log = dir.resolve("log.csv");
            Files.write(log, (HEADER + "\r\n" + l1 + "\r\n" + bad + "\r\n\r\n" + l3 + "\r\n").getBytes());

            List<GpsRecord> recs = HiChan.readGpsLog(log.toString());
            checkEq("3 records (blank line skipped)", 3, recs.size());
            checkEq("first record parsed", l1, recs.get(0).getOriginalLine());
            check("bad line kept in its position (FR-14)", recs.get(1).getErrorText() != null);
            checkEq("bad line reason", "expected 6 fields but found 2", recs.get(1).getErrorText());

            recs.get(0).setChannelId("#I90WAEB_290");
            recs.get(2).setChannelId("#I90WAEB_290");
            Path out = dir.resolve("out.csv");
            HiChan.writeOutput(recs, out.toString());
            String written = new String(Files.readAllBytes(out), StandardCharsets.UTF_8);
            String[] lines = written.split("\r\n");
            checkEq("header + ,channel_id", HEADER + ",channel_id", lines[0]);
            checkEq("line + Channel ID", l1 + ",#I90WAEB_290", lines[1]);
            checkEq("bad line ends with SKIPPED-Error", bad + ",SKIPPED-Error:expected 6 fields but found 2", lines[2]);
            check("CRLF input gives CRLF output (NFR-D1)", written.endsWith("\r\n") && !written.replace("\r\n", "").contains("\n"));

            Path lf = dir.resolve("lf.csv");
            Files.write(lf, (HEADER + "\n" + l1 + "\n").getBytes());
            List<GpsRecord> lfRecs = HiChan.readGpsLog(lf.toString());
            HiChan.writeOutput(lfRecs, out.toString());
            check("LF input gives LF output", !new String(Files.readAllBytes(out)).contains("\r"));

            Path wrong = dir.resolve("wrong.csv");
            Files.write(wrong, ("time,lat,lon\n" + l1 + "\n").getBytes());
            String why = null;
            try {
                HiChan.readGpsLog(wrong.toString());
            } catch (IOException e) {
                why = e.getMessage();
            }
            checkEq("wrong header rejected", "wrong header line", why);
        } finally {
            deleteTree(dir);
        }
    } // end of testReadAndWrite

    // ---------- run(): usage and errors (NFR-U1, NFR-U2, NFR-S1) ----------
    static void testRunErrors() throws IOException {
        System.out.println("run errors");

        Path dir = Files.createTempDirectory("hichan_test_");
        try {
            Path lookup = dir.resolve("segments.csv");
            Files.write(lookup, (LOOKUP_HEADER + "\n"
                    + point("53", 280.0, 282.0, 47.65, -117.37, 290).toCsvLine() + "\n").getBytes());
            Path log = dir.resolve("log.csv");
            Files.write(log, (HEADER + "\n" + gpsLine("23:00:00.000", 47.65, -117.37) + "\n").getBytes());
            String out = dir.resolve("out.csv").toString();

            Object[] r = runCapture(lookup.toString(), log.toString());
            checkEq("two arguments exits 1", 1, r[0]);
            check("usage message", ((String) r[2]).startsWith("Usage:"));

            String missing = dir.resolve("missing.csv").toString();
            r = runCapture(missing, log.toString(), out);
            checkEq("missing lookup exits 1", 1, r[0]);
            checkEq("missing lookup message", "ERROR:" + missing + ":file not found", ((String) r[2]).trim());
            check("one line, no stack trace", ((String) r[2]).trim().split("\\R").length == 1 && !((String) r[2]).contains("Exception"));
            check("no output file written", !Files.exists(Paths.get(out)));

            r = runCapture(lookup.toString(), missing, out);
            checkEq("missing GPS log message", "ERROR:" + missing + ":file not found", ((String) r[2]).trim());

            Path empty = dir.resolve("empty.csv");
            Files.write(empty, new byte[0]);
            r = runCapture(lookup.toString(), empty.toString(), out);
            checkEq("empty GPS log message", "ERROR:" + empty + ":file empty", ((String) r[2]).trim());

            String before = new String(Files.readAllBytes(log));
            r = runCapture(lookup.toString(), log.toString(), log.toString());
            checkEq("output = input exits 1 (NFR-S1)", 1, r[0]);
            check("output = input gives one ERROR line", ((String) r[2]).startsWith("ERROR:") && ((String) r[2]).trim().split("\\R").length == 1);
            checkEq("input left unchanged", before, new String(Files.readAllBytes(log)));

            r = runCapture(lookup.toString(), log.toString(), out);
            checkEq("valid run exits 0", 0, r[0]);
            check("output file written", Files.exists(Paths.get(out)));
            check("nothing printed to stderr", ((String) r[2]).trim().isEmpty());
        } finally {
            deleteTree(dir);
        }
    } // end of testRunErrors

    // ---------- end to end on the provided data (Acceptance Test Plan) ----------
    static void testAcceptance() throws Exception {
        System.out.println("acceptance scenarios on res/ data");

        Path json = findRes("I90_WA_ID.json");
        Path eb = findRes("20260922-160725 - I90EB.csv");
        Path wb = findRes("20260922-131715 - I90WB.csv");
        if (json == null || eb == null || wb == null) {
            System.out.println("  SKIP  res/ files not found (run from the project folder)");
            return;
        }

        Path dir = Files.createTempDirectory("hichan_test_");
        try {
            String lookup = dir.resolve("segments.csv").toString();
            checkEq("Preprocessor builds the lookup file", 0, quietPreprocess(json.toString(), lookup));

            String ebHash = sha256(eb), wbHash = sha256(wb);
            Path ebOut = dir.resolve("I90EB_out.csv"), wbOut = dir.resolve("I90WB_out.csv");

            long start = System.nanoTime();
            Object[] r = runCapture(lookup, eb.toString(), ebOut.toString());
            double seconds = (System.nanoTime() - start) / 1e9;
            checkEq("EB run exits 0", 0, r[0]);
            check("EB log labeled in under 5 s (NFR-P2)", seconds < 5);
            checkEq("WB run exits 0", 0, runCapture(lookup, wb.toString(), wbOut.toString())[0]);

            List<String> ebIn = Files.readAllLines(eb), ebLines = Files.readAllLines(ebOut);
            List<String> wbIn = Files.readAllLines(wb), wbLines = Files.readAllLines(wbOut);

            // AT-04, AT-05 (FR-05, FR-12)
            checkEq("AT-04 EB: 128 entries plus header", 129, ebLines.size());
            checkEq("AT-05 WB: 124 entries plus header", 125, wbLines.size());
            check("AT-04 each EB line is the original plus one field", sameLinesPlusOne(ebIn, ebLines));
            check("AT-05 each WB line is the original plus one field", sameLinesPlusOne(wbIn, wbLines));
            checkEq("AT-04 original EB log unchanged (NFR-S1)", ebHash, sha256(eb));
            checkEq("AT-05 original WB log unchanged (NFR-S1)", wbHash, sha256(wb));

            // AT-07, AT-08 (FR-09)
            check("AT-07 every EB Channel ID is EB", ids(ebLines).stream().allMatch(id -> id.contains("EB_")));
            checkEq("AT-07 EB sequence", "[#I90WAEB_290, #I90WAEB_300, #I90IDEB_10]", runs(ids(ebLines)).toString());
            check("AT-08 every WB Channel ID is WB", ids(wbLines).stream().allMatch(id -> id.contains("WB_")));
            checkEq("AT-08 WB sequence", "[#I90IDWB_10, #I90WAWB_300, #I90WAWB_290]", runs(ids(wbLines)).toString());

            // AT-09, AT-10: state-line crossings. In the NHPN data the WA/ID line on I-90 is at
            // longitude -117.0417, so the EB crossing falls between 23:24:35 and 23:24:45.
            checkEq("AT-09 23:24:25 is #I90WAEB_300", "#I90WAEB_300", idAt(ebLines, "23:24:25"));
            checkEq("AT-09 23:24:35 is #I90WAEB_300 (still west of the line)", "#I90WAEB_300", idAt(ebLines, "23:24:35"));
            checkEq("AT-09 23:24:45 is #I90IDEB_10 (direction stays EB)", "#I90IDEB_10", idAt(ebLines, "23:24:45"));
            checkEq("AT-10 20:21:31 is #I90IDWB_10", "#I90IDWB_10", idAt(wbLines, "20:21:31"));
            checkEq("AT-10 20:21:41 is #I90WAWB_300 (direction stays WB)", "#I90WAWB_300", idAt(wbLines, "20:21:41"));

            // AT-13 (FR-13), NFR-R1
            Pattern full = Pattern.compile("^#I90(WA|ID)(EB|WB)_\\d+$");
            check("AT-13 every ID matches #I90<WA|ID><EB|WB>_<NN>, none contain ??",
                    ids(ebLines).stream().allMatch(id -> full.matcher(id).matches())
                            && ids(wbLines).stream().allMatch(id -> full.matcher(id).matches()));
            checkEq("NFR-R1 100% of EB entries labeled", 128, ids(ebLines).size());
            checkEq("NFR-R1 100% of WB entries labeled", 124, ids(wbLines).size());

            // NFR-R2
            Path again = dir.resolve("again.csv");
            runCapture(lookup, eb.toString(), again.toString());
            check("NFR-R2 same input gives identical output", Arrays.equals(Files.readAllBytes(ebOut), Files.readAllBytes(again)));

            // AT-11 (FR-10): two_entries.csv
            Path two = dir.resolve("two_entries.csv");
            Files.write(two, String.join("\n", ebIn.subList(0, 3)).concat("\n").getBytes());
            Path twoOut = dir.resolve("two_out.csv");
            runCapture(lookup, two.toString(), twoOut.toString());
            List<String> twoLines = Files.readAllLines(twoOut);
            check("AT-11 both entries end with #I90WA??_290",
                    twoLines.get(1).endsWith(",#I90WA??_290") && twoLines.get(2).endsWith(",#I90WA??_290"));

            // AT-12 (FR-11): stopped.csv, 5 repeated readings
            List<String> stopped = new ArrayList<>(ebIn.subList(0, 51));
            String[] p = ebIn.get(50).split(",");
            for (int k = 1; k <= 5; k++)
                stopped.add("2026-09-22 23:15:2" + k + ".000," + p[1] + "," + p[2] + "," + p[3] + ",0.0," + p[5]);
            stopped.addAll(ebIn.subList(51, 60));
            Path stop = dir.resolve("stopped.csv");
            Files.write(stop, (String.join("\n", stopped) + "\n").getBytes());
            Path stopOut = dir.resolve("stopped_out.csv");
            runCapture(lookup, stop.toString(), stopOut.toString());
            List<String> stopIds = ids(Files.readAllLines(stopOut));
            String before = stopIds.get(49);
            boolean allSame = true;
            for (int k = 50; k < 55; k++)
                if (!stopIds.get(k).equals(before))
                    allSame = false;
            check("AT-12 all 5 repeated entries keep the ID of the reading before the stop", allSame);

            // AT-14 (FR-14): bad_line.csv
            List<String> badLog = new ArrayList<>(ebIn);
            badLog.set(20, "2026-09-22 23:10:35.000,47.65368873,,582.083,28.12,24");
            Path badFile = dir.resolve("bad_line.csv");
            Files.write(badFile, (String.join("\n", badLog) + "\n").getBytes());
            Path badOut = dir.resolve("bad_out.csv");
            checkEq("AT-14 HiChan finishes", 0, runCapture(lookup, badFile.toString(), badOut.toString())[0]);
            List<String> badLines = Files.readAllLines(badOut);
            check("AT-14 bad line stays in place ending SKIPPED-Error:<description>",
                    badLines.get(20).startsWith(badLog.get(20) + ",SKIPPED-Error:")
                            && badLines.get(20).length() > (badLog.get(20) + ",SKIPPED-Error:").length());
            long labeled = badLines.stream().skip(1).filter(l -> l.matches(".*,#I90(WA|ID)(EB|WB)_\\d+$")).count();
            checkEq("AT-14 every other entry labeled", 127L, labeled);
        } finally {
            deleteTree(dir);
        }
    } // end of testAcceptance

    // Channel IDs (last field) of every data line that has one
    static List<String> ids(List<String> lines) {
        List<String> out = new ArrayList<>();
        for (String l : lines.subList(1, lines.size())) {
            String last = l.substring(l.lastIndexOf(',') + 1);
            if (last.startsWith("#"))
                out.add(last);
        }
        return out;
    } // end of ids

    // the distinct IDs in order, with repeats collapsed
    static List<String> runs(List<String> ids) {
        List<String> out = new ArrayList<>();
        for (String id : ids)
            if (out.isEmpty() || !out.get(out.size() - 1).equals(id))
                out.add(id);
        return out;
    } // end of runs

    static String idAt(List<String> lines, String time) {
        for (String l : lines)
            if (l.contains(" " + time + "."))
                return l.substring(l.lastIndexOf(',') + 1);
        return null;
    } // end of idAt

    static boolean sameLinesPlusOne(List<String> in, List<String> out) {
        if (in.size() != out.size())
            return false;
        if (!out.get(0).equals(in.get(0) + ",channel_id"))
            return false;
        for (int i = 1; i < in.size(); i++) {
            String o = out.get(i);
            if (!o.startsWith(in.get(i) + ",") || o.substring(in.get(i).length() + 1).contains(","))
                return false;
        }
        return true;
    } // end of sameLinesPlusOne

    interface Test {
        void run() throws Exception;
    } // end of Test

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== HiChanTester ===");
        Test[] tests = {
                HiChanTester::testComputeChannelNumber,
                HiChanTester::testBuildChannelId,
                HiChanTester::testDetermineDirection,
                HiChanTester::testFindBestSegment,
                HiChanTester::testBackfill,
                HiChanTester::testProcessRecords,
                HiChanTester::testLoadIndex,
                HiChanTester::testReadAndWrite,
                HiChanTester::testRunErrors,
                HiChanTester::testAcceptance
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
} // end of HiChanTester
