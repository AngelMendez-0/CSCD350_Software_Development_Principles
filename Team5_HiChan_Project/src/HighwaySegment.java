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
    private String route; // always "I90" in this data
    private String stfips; // "53" = WA, "16" = ID
    private double begMp; // beginning milepost
    private double endMp; // ending milepost

    // Constructor
    public HighwaySegment(String geohash, String route, String stfips, double begMp, double endMp) {
        this.geohash = geohash;
        this.route = route;
        this.stfips = stfips;
        this.begMp = begMp;
        this.endMp = endMp;
    } // end of HighwaySegment


    // turns one CSV line from the lookup file back into an object
    public static HighwaySegment fromCsvLine(String line) {

        // a null line can't be split so say so clearly
        if(line == null)
            throw new IllegalArgumentException("line is null");

        String[] parts = line.split(",", -1); // splits the line into its 5 parts (-1 keeps empty trailing parts)

        // anything other than the 5 parts means the line is bad
        if(parts.length != 5)
            throw new IllegalArgumentException("expected 5 parts but got " + parts.length);

        // trim each part
        String geohash = parts[0].trim();
        String route = parts[1].trim();
        String stfips = parts[2].trim();

        // two mileposts values
        double begMp = Double.parseDouble(parts[3].trim());
        double endMp = Double.parseDouble(parts[4].trim());

        return new HighwaySegment(geohash, route, stfips, begMp, endMp);
    } // end of fromCsvLine

    // the opposite of fromCsvLine
    // used for output file
    public String toCsvLine() {
        return geohash + "," + route + "," + stfips + "," + begMp + "," + endMp;
    } // end of toCsvLine

    // converts the FIPS to a 2 letter state
    // (.equals compares the text, == would only compare object references)
    public String getStateAbbrev() {
        if("53".equals(stfips))
            return "WA";
        else if("16".equals(stfips))
            return "ID";
        else
            return "??";
    } // end of getStateAbbrev

    // Getters
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
