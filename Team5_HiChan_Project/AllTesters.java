package Testers;

/*
 * Runs every tester and prints one summary.
 * Run from the project folder so the testers can find res/.
 *
 *   Run: Testers.AllTesters
 */
public class AllTesters {

    public static void main(String[] args) {
        String[] names = {"HiChanConfig", "GeoHash", "HighwaySegment", "GpsRecord", "Preprocessor", "HiChan"};
        int[] failures = {
                HiChanConfigTester.runAll(),
                GeoHashTester.runAll(),
                HighwaySegmentTester.runAll(),
                GpsRecordTester.runAll(),
                PreprocessorTester.runAll(),
                HiChanTester.runAll()
        };

        System.out.println();
        System.out.println("=== Summary ===");
        int total = 0;
        for (int i = 0; i < names.length; i++) {
            System.out.println("  " + (failures[i] == 0 ? "PASS" : "FAIL") + "  " + names[i] + "Tester"
                    + (failures[i] == 0 ? "" : " (" + failures[i] + " failed)"));
            total += failures[i];
        }
        System.out.println(total == 0 ? "All testers passed." : total + " check(s) failed.");
        if (total > 0)
            System.exit(1);
    } // end of main
} // end of AllTesters
