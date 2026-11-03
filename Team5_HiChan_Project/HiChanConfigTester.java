package Testers;

import Hi_Chan.HiChanConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/*
 * Plain-Java tester for HiChanConfig (no JUnit needed).
 * Every constant must hold the value from the Requirements Specification v2.1 (NFR-M2).
 *
 *   Run: Testers.HiChanConfigTester
 */
public class HiChanConfigTester {

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

    // ---------- values from the spec ----------
    static void testValues() {
        System.out.println("constant values");

        checkEq("HIGHWAY is I90 (FR-02)", "I90", HiChanConfig.HIGHWAY);
        checkEq("STATE_WA is 53", "53", HiChanConfig.STATE_WA);
        checkEq("STATE_ID is 16", "16", HiChanConfig.STATE_ID);
        checkEq("GEOHASH_PRECISION is 5 (AD-2)", 5, HiChanConfig.GEOHASH_PRECISION);
        checkEq("CHANNEL_MILES is 10 (FR-07)", 10, HiChanConfig.CHANNEL_MILES);
        checkEq("CHANNEL_PREFIX is # (FR-13)", "#", HiChanConfig.CHANNEL_PREFIX);
        checkEq("SAME_LOCATION_TOLERANCE is 0.00001 degrees (FR-11)", 0.00001, HiChanConfig.SAME_LOCATION_TOLERANCE);
    } // end of testValues

    // ---------- constants-only class ----------
    static void testConstantsOnly() {
        System.out.println("constants-only class");

        check("class is final", Modifier.isFinal(HiChanConfig.class.getModifiers()));
        Constructor<?>[] ctors = HiChanConfig.class.getDeclaredConstructors();
        check("only constructor is private", ctors.length == 1 && Modifier.isPrivate(ctors[0].getModifiers()));

        boolean allConstant = true;
        for (Field f : HiChanConfig.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (!(Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m)))
                allConstant = false;
        }
        check("every field is public static final (NFR-M2)", allConstant);
    } // end of testConstantsOnly

    // ---------- run everything ----------
    public static int runAll() {
        passed = 0;
        failed = 0;
        System.out.println("=== HiChanConfigTester ===");
        Runnable[] tests = {HiChanConfigTester::testValues, HiChanConfigTester::testConstantsOnly};
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
} // end of HiChanConfigTester
