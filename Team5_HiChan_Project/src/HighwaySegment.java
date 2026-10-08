/*
 * One row of the lookup CSV that the Preprocessor writes and HiChan reads:
 *
 *     GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP
 *     c2kx9p,I90,53,280.12,282.5
 *
 * STFIPS is the state code: 53 = Washington, 16 = Idaho.
 * One JSON feature becomes many HighwaySegment rows, one per unique geohash.
 */
public class HighwaySegment {

    private String geohash;
    private String route;     // always "I90" in this data
    private String stfips;    // "53" = WA, "16" = ID
    private double begMp;     // beginning milepost
    private double endMp;     // ending milepost

    /* Constructor
     * What it does: builds a segment row from its five values.
     *
     * How to code it:
     *   1. Assign each parameter to the matching field (this.geohash = geohash; ...).
     */
    public HighwaySegment(String geohash, String route, String stfips, double begMp, double endMp) {
        // TODO: assign fields
    } // end of HighwaySegment

    /*
     * What it does: turns one CSV line from the lookup file back into an object
     * (HiChan uses it when loading the index).
     *
     * How to code it:
     *   1. line.split(",") gives 5 parts: geohash, route, stfips, begmp, endmp.
     *   2. trim() each part.
     *   3. Double.parseDouble() the two milepost values.
     *   4. return new HighwaySegment(...).
     *   (Optional) throw IllegalArgumentException if there aren't exactly 5 parts.
     */
    public static HighwaySegment fromCsvLine(String line) {
        // TODO: parse a CSV line into a HighwaySegment
        return null;
    } // end of fromCsvLine

    /*
     * What it does: the opposite of fromCsvLine. The Preprocessor uses it when
     * writing the output file.
     *
     * How to code it:
     *   1. Return geohash + "," + route + "," + stfips + "," + begMp + "," + endMp
     *      (or use String.join / String.format).
     *   2. Keep the column order identical to the header line the Preprocessor writes.
     */
    public String toCsvLine() {
        // TODO: build the comma-separated line
        return "";
    } // end of toCsvLine

    /*
     * What it does: converts the FIPS code to the 2-letter state used in the
     * Channel ID ("53" -> "WA", "16" -> "ID").
     *
     * How to code it:
     *   1. if stfips equals "53" return "WA"; if "16" return "ID".
     *   2. Otherwise return "??" (or throw) so a bad value shows up in testing.
     */
    public String getStateAbbrev() {
        // TODO: map FIPS code to state abbreviation
        return "";
    } // end of getStateAbbrev

    /*
     * What it does: returns the middle milepost of the segment, (begMp + endMp) / 2.
     * HiChan uses it as an estimate of the truck's milepost and to tell
     * whether mileposts are going up or down between GPS points.
     *
     * How to code it:
     *   1. return (begMp + endMp) / 2.0;
     */

    // Getters / Setters
    public double getMidMp() { return (begMp + endMp) / 2; } // end of getMidMp

    public String getGeohash() { return geohash; } // end of getGeohash

    public String getRoute() { return route; } // end of getRoute

    public String getStfips() { return stfips; } // end of getStfips

    public double getBegMp() { return begMp; } // end of getBegMp

    public double getEndMp() { return endMp; } // end of getEndMp

//    What it does: a readable version for debugging/printing.
//    Return something like "I90 WA MP 280.12-282.5 [c2kx9p]".
    @Override
    public String toString() {
        return route + " " + getStateAbbrev() + " MP " + begMp + "-" + endMp + " [" + geohash + "]";

    } // end of toString
} // end of HighwaySegment