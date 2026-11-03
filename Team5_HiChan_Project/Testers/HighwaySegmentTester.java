package Testers;

import Hi_Chan.HighwaySegment;

/*
 * Plain-Java tester for HighwaySegment (no JUnit needed).
 * One lookup-file row: GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL (FR-03).
 *
 *   Run: Testers.HighwaySegmentTester
 */
public class HighwaySegmentTester {

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

    static void checkNear(String name, double expected, double actual) {
        boolean ok = Math.abs(expected - actual) < 1e-9;
        if (!ok)
            System.out.println("        expected: " + expected + "  actual: " + actual);
        check(name, ok);
    } // end of checkNear

    // true if fromCsvLine throws IllegalArgumentException (NumberFormatException counts, it extends it)
    static boolean rejects(String line) {
        try {
            HighwaySegment.fromCsvLine(line);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    } // end of rejects

    // the spec's example row (Requirements Specification v2.1, section 11)
    static final String SPEC_ROW = "c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10";

    static HighwaySegment wa() {
        return new HighwaySegment("c2kx9", "I90", "53", 280.12, 282.5, 47.66, -117.40, 290);
    } // end of wa

    // ---------- constructor + getters ----------
    static void testConstructorAndGetters() {
        System.out.println("constructor and getters");
        HighwaySegment s = wa();

        checkEq("getGeohash", "c2kx9", s.getGeohash());
        checkEq("getRoute", "I90", s.getRoute());
        checkEq("getStfips", "53", s.getStfips());
        checkNear("getBegMp", 280.12, s.getBegMp());
        checkNear("getEndMp", 282.5, s.getEndMp());
        checkNear("getLat", 47.66, s.getLat());
        checkNear("getLon", -117.40, s.getLon());
        checkEq("getChannel", 290, s.getChannel());

        s.setGeohash("c2kx8");
        checkEq("setGeohash", "c2kx8", s.getGeohash());

        HighwaySegment noHash = new HighwaySegment(null, "I90", "16", 0.0, 6.825, 47.7, -117.0, 10);
        checkEq("geohash may be null before indexing", null, noHash.getGeohash());
    } // end of testConstructorAndGetters

    // ---------- getMidMp ----------
    static void testGetMidMp() {
        System.out.println("getMidMp");

        checkNear("midpoint of 280.12 and 282.5", 281.31, wa().getMidMp());
        checkNear("midpoint of 0 and 6.825", 3.4125,
                new HighwaySegment("a", "I90", "16", 0.0, 6.825, 0, 0, 10).getMidMp());
        checkNear("midpoint when beg == end", 5.0,
                new HighwaySegment("a", "I90", "53", 5.0, 5.0, 0, 0, 10).getMidMp());
    } // end of testGetMidMp

    // ---------- getStateAbbrev ----------
    static void testGetStateAbbrev() {
        System.out.println("getStateAbbrev");

        checkEq("53 is WA", "WA", new HighwaySegment("a", "I90", "53", 0, 1, 0, 0, 10).getStateAbbrev());
        checkEq("16 is ID", "ID", new HighwaySegment("a", "I90", "16", 0, 1, 0, 0, 10).getStateAbbrev());
        checkEq("unknown code gives ??", "??", new HighwaySegment("a", "I90", "99", 0, 1, 0, 0, 10).getStateAbbrev());
        checkEq("empty code gives ??", "??", new HighwaySegment("a", "I90", "", 0, 1, 0, 0, 10).getStateAbbrev());
        checkEq("null code gives ?? (no crash)", "??", new HighwaySegment("a", "I90", null, 0, 1, 0, 0, 10).getStateAbbrev());
    } // end of testGetStateAbbrev

    // ---------- toCsvLine ----------
    static void testToCsvLine() {
        System.out.println("toCsvLine");

        checkEq("spec example row", SPEC_ROW,
                new HighwaySegment("c2kz3", "I90", "16", 0.0, 6.825, 47.709889, -116.993985, 10).toCsvLine());
        checkEq("WA row", "c2kx9,I90,53,280.12,282.5,47.66,-117.4,290", wa().toCsvLine());
        checkEq("has exactly 8 fields", 8, wa().toCsvLine().split(",", -1).length);
    } // end of testToCsvLine

    // ---------- fromCsvLine ----------
    static void testFromCsvLine() {
        System.out.println("fromCsvLine");

        HighwaySegment s = HighwaySegment.fromCsvLine(SPEC_ROW);
        checkEq("geohash parsed", "c2kz3", s.getGeohash());
        checkEq("route parsed", "I90", s.getRoute());
        checkEq("stfips parsed", "16", s.getStfips());
        checkNear("begMp parsed", 0.0, s.getBegMp());
        checkNear("endMp parsed", 6.825, s.getEndMp());
        checkNear("lat parsed", 47.709889, s.getLat());
        checkNear("lon parsed", -116.993985, s.getLon());
        checkEq("channel parsed", 10, s.getChannel());

        HighwaySegment spaced = HighwaySegment.fromCsvLine("  c2kz3 , I90 , 16 , 0.0 , 6.825 , 47.709889 , -116.993985 , 10  ");
        checkEq("spaces trimmed (geohash)", "c2kz3", spaced.getGeohash());
        checkEq("spaces trimmed (channel)", 10, spaced.getChannel());

        HighwaySegment crlf = HighwaySegment.fromCsvLine(SPEC_ROW + "\r");
        checkEq("CRLF line end handled (NFR-D1)", 10, crlf.getChannel());

        // bad input
        check("null line rejected", rejects(null));
        check("empty line rejected", rejects(""));
        check("old 5-column row rejected", rejects("c2kx9p,I90,53,280.12,282.5"));
        check("too few parts rejected", rejects("c2kz3,I90,16,0.0,6.825,47.709889,-116.993985"));
        check("too many parts rejected", rejects(SPEC_ROW + ",extra"));
        check("trailing comma rejected", rejects(SPEC_ROW + ","));
        check("non-numeric begMp rejected", rejects("c2kz3,I90,16,abc,6.825,47.709889,-116.993985,10"));
        check("non-numeric lat rejected", rejects("c2kz3,I90,16,0.0,6.825,north,-116.993985,10"));
        check("decimal channel rejected", rejects("c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10.5"));
        check("header line rejected", rejects("GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL"));

        try {
            HighwaySegment.fromCsvLine(null);
        } catch (IllegalArgumentException e) {
            checkEq("null line message", "line is null", e.getMessage());
        }
    } // end of testFromCsvLine

    // ---------- round trip ----------
    static void testRoundTrip() {
        System.out.println("round trip");

        HighwaySegment original = wa();
        HighwaySegment copy = HighwaySegment.fromCsvLine(original.toCsvLine());

        checkEq("geohash survives", original.getGeohash(), copy.getGeohash());
        checkEq("stfips survives", original.getStfips(), copy.getStfips());
        checkEq("begMp survives exactly", original.getBegMp(), copy.getBegMp());
        checkEq("lat survives exactly", original.getLat(), copy.getLat());
        checkEq("lon survives exactly", original.getLon(), copy.getLon());
        checkEq("channel survives", original.getChannel(), copy.getChannel());
        checkEq("csv line identical after round trip", original.toCsvLine(), copy.toCsvLine());
        checkEq("spec row survives round trip", SPEC_ROW, HighwaySegment.fromCsvLine(SPEC_ROW).toCsvLine());
    } // end of testRoundTrip

    // ---------- toString ----------
    static void testToString() {
        System.out.println("toString");

        checkEq("WA row", "I90 WA MP 280.12-282.5 [c2kx9]", wa().toString());
        checkEq("ID row", "I90 ID MP 0.0-6.825 [c2kz3]", HighwaySegment.fromCsvLine(SPEC_ROW).toString());
    } // end of testToString

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== HighwaySegmentTester ===");
        Runnable[] tests = {
                HighwaySegmentTester::testConstructorAndGetters,
                HighwaySegmentTester::testGetMidMp,
                HighwaySegmentTester::testGetStateAbbrev,
                HighwaySegmentTester::testToCsvLine,
                HighwaySegmentTester::testFromCsvLine,
                HighwaySegmentTester::testRoundTrip,
                HighwaySegmentTester::testToString
        };
        for (Runnable t : tests) {
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
} // end of HighwaySegmentTester
