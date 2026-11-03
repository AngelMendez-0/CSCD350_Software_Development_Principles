package Testers;

import Hi_Chan.GpsRecord;
import Hi_Chan.HighwaySegment;

/*
 * Plain-Java tester for GpsRecord (no JUnit needed).
 * Checks parsing of GPS Logger lines (FR-05), rejecting bad lines (FR-14),
 * stopped-truck detection (FR-11) and the output line (FR-12, FR-14).
 *
 *   Run: Testers.GpsRecordTester
 */
public class GpsRecordTester {

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

    // the rejection reason, or null if fromCsvLine accepted the line
    static String reason(String line) {
        try {
            GpsRecord r = GpsRecord.fromCsvLine(line);
            return r == null ? "returned null" : null;
        } catch (IllegalArgumentException e) {
            return e.getMessage() == null ? "" : e.getMessage();
        }
    } // end of reason

    // true if the line is rejected with a short, comma-free reason (FR-14)
    static boolean rejected(String line) {
        String why = reason(line);
        return why != null && !why.equals("returned null") && !why.trim().isEmpty() && !why.contains(",");
    } // end of rejected

    // first line of the eastbound development log
    static final String LINE = "2026-09-22 23:07:25.000,47.65384674,-117.3701775,565.534,15.03,25";

    // ---------- constructor ----------
    static void testConstructor() {
        System.out.println("constructor");

        GpsRecord r = new GpsRecord(LINE, "2026-09-22 23:07:25.000", 47.65384674, -117.3701775, 565.534, 15.03, 25);
        checkEq("original line", LINE, r.getOriginalLine());
        checkEq("date time", "2026-09-22 23:07:25.000", r.getDateTime());
        checkNear("latitude", 47.65384674, r.getLatitude());
        checkNear("longitude", -117.3701775, r.getLongitude());
        checkNear("altitude", 565.534, r.getAltitude());
        checkNear("speed", 15.03, r.getSpeed());
        checkEq("sat_used", 25, r.getSatUsed());
        checkEq("geohash starts null", null, r.getGeohash());
        checkEq("segment starts null", null, r.getSegment());
        checkEq("channel ID starts null", null, r.getChannelId());
        checkEq("error text starts null", null, r.getErrorText());
    } // end of testConstructor

    // ---------- fromCsvLine (FR-05) ----------
    static void testFromCsvLine() {
        System.out.println("fromCsvLine");

        GpsRecord r = GpsRecord.fromCsvLine(LINE);
        check("parses a log line", r != null);
        if (r == null)
            return;
        checkEq("date time", "2026-09-22 23:07:25.000", r.getDateTime());
        checkNear("latitude", 47.65384674, r.getLatitude());
        checkNear("longitude", -117.3701775, r.getLongitude());
        checkNear("altitude", 565.534, r.getAltitude());
        checkNear("speed", 15.03, r.getSpeed());
        checkEq("sat_used", 25, r.getSatUsed());
        checkEq("original line kept exactly", LINE, r.getOriginalLine());
        checkEq("no error text", null, r.getErrorText());

        GpsRecord crlf = GpsRecord.fromCsvLine(LINE + "\r");
        checkEq("CRLF: \\r dropped from the original line (NFR-D1)", LINE, crlf.getOriginalLine());
        checkEq("CRLF: sat_used still parses", 25, crlf.getSatUsed());

        GpsRecord spaced = GpsRecord.fromCsvLine("2026-09-22 23:07:25.000, 47.5 , -117.5 ,565,15,25");
        checkNear("spaces around numbers tolerated", 47.5, spaced.getLatitude());

        GpsRecord wb = GpsRecord.fromCsvLine("2026-09-22 20:17:15.000,47.71314203,-116.9488803,646.743,13.05,22");
        checkNear("westbound first line latitude", 47.71314203, wb.getLatitude());
        checkEq("westbound first line sat_used", 22, wb.getSatUsed());
    } // end of testFromCsvLine

    // ---------- bad lines (FR-14) ----------
    static void testRejects() {
        System.out.println("bad lines rejected with a short reason");

        check("missing field", rejected("2026-09-22 23:07:25.000,47.65384674,-117.3701775,565.534,15.03"));
        check("extra field", rejected(LINE + ",extra"));
        check("empty line", rejected(""));
        check("null line", rejected(null));
        check("latitude not a number", rejected("2026-09-22 23:07:25.000,abc,-117.3701775,565.534,15.03,25"));
        check("longitude missing", rejected("2026-09-22 23:07:25.000,47.6,,565.534,15.03,25"));
        check("altitude not a number", rejected("2026-09-22 23:07:25.000,47.6,-117.3,x,15.03,25"));
        check("speed not a number", rejected("2026-09-22 23:07:25.000,47.6,-117.3,565,fast,25"));
        check("sat_used not a whole number", rejected("2026-09-22 23:07:25.000,47.6,-117.3,565,15,2.5"));
        check("latitude over 90", rejected("2026-09-22 23:07:25.000,91,-117.3,565,15,25"));
        check("longitude under -180", rejected("2026-09-22 23:07:25.000,47.6,-181,565,15,25"));
        check("empty date time", rejected(",47.6,-117.3,565,15,25"));
        check("header line", rejected("date time,latitude,longitude,altitude(m),speed(m/s),sat_used"));
        checkEq("missing-field reason names the count", "expected 6 fields but found 5",
                reason("2026-09-22 23:07:25.000,47.65384674,-117.3701775,565.534,15.03"));
    } // end of testRejects

    // ---------- isSameLocation (FR-11) ----------
    static void testIsSameLocation() {
        System.out.println("isSameLocation");

        GpsRecord a = GpsRecord.fromCsvLine(LINE);
        GpsRecord same = GpsRecord.fromCsvLine("2026-09-22 23:07:35.000,47.65384674,-117.3701775,565.534,0,25");
        GpsRecord jitter = GpsRecord.fromCsvLine("2026-09-22 23:07:35.000,47.65384900,-117.3701760,565.534,0,25");
        GpsRecord moved = GpsRecord.fromCsvLine("2026-09-22 23:07:35.000,47.65388518,-117.3680228,567.175,16.51,25");
        GpsRecord bad = new GpsRecord("bad", null, 47.65384674, -117.3701775, 0, 0, 0);
        bad.setErrorText("expected 6 fields but found 1");

        check("identical coordinates", a.isSameLocation(same));
        check("within 0.00001 degrees (GPS jitter)", a.isSameLocation(jitter));
        check("a moving truck is not the same spot", !a.isSameLocation(moved));
        check("null other is false", !a.isSameLocation(null));
        check("a skipped line is never the same spot", !a.isSameLocation(bad));
        check("symmetric", same.isSameLocation(a) && a.isSameLocation(same));
    } // end of testIsSameLocation

    // ---------- toOutputLine (FR-12, FR-14) ----------
    static void testToOutputLine() {
        System.out.println("toOutputLine");

        GpsRecord r = new GpsRecord(LINE, "2026-09-22 23:07:25.000", 47.65384674, -117.3701775, 565.534, 15.03, 25);
        r.setChannelId("#I90WAEB_290");
        checkEq("original line + Channel ID", LINE + ",#I90WAEB_290", r.toOutputLine());
        checkEq("exactly one field added", 7, r.toOutputLine().split(",", -1).length);

        r.setChannelId("#I90WA??_290");
        checkEq("?? written as is until back-filled", LINE + ",#I90WA??_290", r.toOutputLine());

        GpsRecord none = new GpsRecord(LINE, "2026-09-22 23:07:25.000", 47.65384674, -117.3701775, 565.534, 15.03, 25);
        checkEq("no ID yet gives an empty last field", LINE + ",", none.toOutputLine());

        GpsRecord bad = new GpsRecord("bad,line", null, 0, 0, 0, 0, 0);
        bad.setErrorText("expected 6 fields but found 2");
        checkEq("skipped line keeps its text and ends with SKIPPED-Error",
                "bad,line,SKIPPED-Error:expected 6 fields but found 2", bad.toOutputLine());
    } // end of testToOutputLine

    // ---------- setters ----------
    static void testSetters() {
        System.out.println("getters and setters");

        GpsRecord r = new GpsRecord(LINE, "2026-09-22 23:07:25.000", 47.65384674, -117.3701775, 565.534, 15.03, 25);
        HighwaySegment seg = new HighwaySegment("c2kq9", "I90", "53", 280.0, 282.0, 47.65, -117.37, 290);
        r.setGeohash("c2kq9");
        r.setSegment(seg);
        r.setChannelId("#I90WAEB_290");
        r.setErrorText("no I-90 point near reading");
        checkEq("geohash", "c2kq9", r.getGeohash());
        check("segment", r.getSegment() == seg);
        checkEq("channel ID read back (no duplicate field)", "#I90WAEB_290", r.getChannelId());
        checkEq("error text", "no I-90 point near reading", r.getErrorText());
    } // end of testSetters

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== GpsRecordTester ===");
        Runnable[] tests = {
                GpsRecordTester::testConstructor,
                GpsRecordTester::testFromCsvLine,
                GpsRecordTester::testRejects,
                GpsRecordTester::testIsSameLocation,
                GpsRecordTester::testToOutputLine,
                GpsRecordTester::testSetters
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
} // end of GpsRecordTester
