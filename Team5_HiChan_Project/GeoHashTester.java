package Testers;

import Hi_Chan.GeoHash;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Plain-Java tester for GeoHash (no JUnit needed).
 * Checks encoding at precision 5 (AD-2), the 8 neighbor cells and Haversine miles.
 *
 *   Run: Testers.GeoHashTester
 */
public class GeoHashTester {

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

    static void checkNear(String name, double expected, double actual, double tolerance) {
        boolean ok = Math.abs(expected - actual) <= tolerance;
        if (!ok)
            System.out.println("        expected: " + expected + "  actual: " + actual);
        check(name, ok);
    } // end of checkNear

    // Test helper: the cell a geohash names, as {minLat, maxLat, minLon, maxLon}
    static double[] cell(String hash) {
        double minLat = -90, maxLat = 90, minLon = -180, maxLon = 180;
        boolean even = true;
        for (char c : hash.toCharArray()) {
            int v = GeoHash.BASE32.indexOf(c);
            for (int b = 4; b >= 0; b--) {
                int bit = (v >> b) & 1;
                if (even) {
                    double m = (minLon + maxLon) / 2;
                    if (bit == 1) minLon = m; else maxLon = m;
                } else {
                    double m = (minLat + maxLat) / 2;
                    if (bit == 1) minLat = m; else maxLat = m;
                }
                even = !even;
            }
        }
        return new double[]{minLat, maxLat, minLon, maxLon};
    } // end of cell

    // ---------- encode ----------
    static void testEncode() {
        System.out.println("encode");

        checkEq("Spokane at precision 6", "c2krpu", GeoHash.encode(47.6588, -117.4260, 6));
        checkEq("Spokane at precision 5", "c2krp", GeoHash.encode(47.6588, -117.4260, 5));
        checkEq("first NHPN point is c2kz3 (spec example row)", "c2kz3", GeoHash.encode(47.709889, -116.993985, 5));
        checkEq("origin", "s0000", GeoHash.encode(0, 0, 5));
        checkEq("south-west corner", "00000", GeoHash.encode(-90, -180, 5));
        checkEq("north-east corner", "zzzzz", GeoHash.encode(90, 180, 5));
        checkEq("precision 1", "c", GeoHash.encode(47.6588, -117.4260, 1));
        checkEq("length matches precision 9", 9, GeoHash.encode(47.6588, -117.4260, 9).length());
        check("longer hash starts with the shorter one",
                GeoHash.encode(47.6588, -117.4260, 8).startsWith(GeoHash.encode(47.6588, -117.4260, 5)));
        check("only Base32 characters",
                GeoHash.encode(47.6588, -117.4260, 12).chars().allMatch(c -> GeoHash.BASE32.indexOf(c) >= 0));
        check("swapping lat and lon gives a different hash",
                !GeoHash.encode(47.6588, -117.4260, 5).equals(GeoHash.encode(-117.4260, 47.6588, 5)));

        double[] box = cell(GeoHash.encode(47.709889, -116.993985, 5));
        check("the cell contains the point", box[0] <= 47.709889 && 47.709889 < box[1]
                && box[2] <= -116.993985 && -116.993985 < box[3]);
    } // end of testEncode

    // ---------- default precision ----------
    static void testDefaultPrecision() {
        System.out.println("default precision");

        checkEq("DEFAULT_PRECISION is 5", 5, GeoHash.DEFAULT_PRECISION);
        checkEq("2-argument encode uses precision 5", "c2krp", GeoHash.encode(47.6588, -117.4260));
        checkEq("2-argument encode matches 3-argument", GeoHash.encode(47.709889, -116.993985, 5),
                GeoHash.encode(47.709889, -116.993985));
    } // end of testDefaultPrecision

    // ---------- neighbors ----------
    static void testNeighbors() {
        System.out.println("neighbors");

        String center = GeoHash.encode(47.6588, -117.4260, 5);
        List<String> n = GeoHash.neighbors(center);
        checkEq("8 neighbors", 8, n.size());
        checkEq("all different", 8, new HashSet<>(n).size());
        check("center not included", !n.contains(center));
        check("same precision", !n.isEmpty() && n.stream().allMatch(h -> h.length() == center.length()));

        // the expected 8 cells, built from the center cell's size
        double[] c = cell(center);
        double h = c[1] - c[0], w = c[3] - c[2], lat = (c[0] + c[1]) / 2, lon = (c[2] + c[3]) / 2;
        Set<String> expected = new HashSet<>();
        for (int dLat = -1; dLat <= 1; dLat++)
            for (int dLon = -1; dLon <= 1; dLon++)
                if (dLat != 0 || dLon != 0)
                    expected.add(GeoHash.encode(lat + dLat * h, lon + dLon * w, 5));
        checkEq("exactly the 8 surrounding cells", expected, new HashSet<>(n));

        checkEq("a cell at the north pole has 5 neighbors", 5,
                new HashSet<>(GeoHash.neighbors(GeoHash.encode(89.99, 0, 5))).size());
        check("works at precision 6", GeoHash.neighbors(GeoHash.encode(47.6588, -117.4260, 6)).stream()
                .allMatch(x -> x.length() == 6));
    } // end of testNeighbors

    // ---------- haversineMiles ----------
    static void testHaversine() {
        System.out.println("haversineMiles");

        checkNear("same point is 0", 0.0, GeoHash.haversineMiles(47.6588, -117.4260, 47.6588, -117.4260), 1e-12);
        checkNear("1 degree of latitude is about 69.09 miles", 69.09,
                GeoHash.haversineMiles(47.0, -117.0, 48.0, -117.0), 0.05);
        double ab = GeoHash.haversineMiles(47.6588, -117.4260, 47.7130, -116.9488);
        double ba = GeoHash.haversineMiles(47.7130, -116.9488, 47.6588, -117.4260);
        checkNear("symmetric", ab, ba, 1e-12);
        checkNear("Spokane to Post Falls is about 22.6 miles", 22.6, ab, 0.5);
        double step = GeoHash.haversineMiles(47.65384674, -117.3701775, 47.65388518, -117.3680228);
        check("one 10-second GPS step is between 0.05 and 0.25 mile", step > 0.05 && step < 0.25);
    } // end of testHaversine

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== GeoHashTester ===");
        Runnable[] tests = {
                GeoHashTester::testEncode,
                GeoHashTester::testDefaultPrecision,
                GeoHashTester::testNeighbors,
                GeoHashTester::testHaversine
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
} // end of GeoHashTester
