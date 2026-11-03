package Hi_Chan;

/*
 * One row of the lookup CSV that the Hi_Chan.Preprocessor writes and Hi_Chan.HiChan reads:
 *
 *     GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL
 *     c2kz3,I90,16,0.0,6.825,47.709889,-116.993985,10
 *
 * STFIPS is the state code: 53 = Washington, 16 = Idaho.
 * One row per highway point (FR-03); CHANNEL is the point's 10-mile channel (FR-07).
 */
public class HighwaySegment {

    private String geohash;
    private String route; // always "I90" in this data
    private String stfips; // "53" = WA, "16" = ID
    private double begMp; // beginning milepost
    private double endMp; // ending milepost
    private double lat; // this point's latitude
    private double lon; // this point's longitude
    private int channel; // 10-mile channel number, e.g. 290

    // Constructor
    public HighwaySegment(String geohash, String route, String stfips, double begMp, double endMp,
                          double lat, double lon, int channel) {
        this.geohash = geohash;
        this.route = route;
        this.stfips = stfips;
        this.begMp = begMp;
        this.endMp = endMp;
        this.lat = lat;
        this.lon = lon;
        this.channel = channel;
    } // end of Hi_Chan.HighwaySegment


    // turns one CSV line from the lookup file back into an object
    public static HighwaySegment fromCsvLine(String line) {

        // a null line can't be split so say so clearly
        if(line == null)
            throw new IllegalArgumentException("line is null");

        String[] parts = line.split(",", -1); // splits the line into its 8 parts (-1 keeps empty trailing parts)

        // anything other than the 8 parts means the line is bad
        if(parts.length != 8)
            throw new IllegalArgumentException("expected 8 parts but got " + parts.length);

        // trim each part
        String geohash = parts[0].trim();
        String route = parts[1].trim();
        String stfips = parts[2].trim();

        // two mileposts values
        double begMp = Double.parseDouble(parts[3].trim());
        double endMp = Double.parseDouble(parts[4].trim());

        // point position and channel
        double lat = Double.parseDouble(parts[5].trim());
        double lon = Double.parseDouble(parts[6].trim());
        int channel = Integer.parseInt(parts[7].trim());

        return new HighwaySegment(geohash, route, stfips, begMp, endMp, lat, lon, channel);
    } // end of fromCsvLine

    // the opposite of fromCsvLine
    // used for output file
    public String toCsvLine() {
        return geohash + "," + route + "," + stfips + "," + begMp + "," + endMp + "," + lat + "," + lon + "," + channel;
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
    public double getLat() { return lat; } // end of getLat
    public double getLon() { return lon; } // end of getLon
    public int getChannel() { return channel; } // end of getChannel
    public void setGeohash(String geohash) { this.geohash = geohash; } // end of setGeohash

    @Override
    public String toString() {
        return route + " " + getStateAbbrev() + " MP " + begMp + "-" + endMp + " [" + geohash + "]";

    } // end of toString
} // end of Hi_Chan.HighwaySegment
