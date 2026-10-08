/*
 * One line from a GPS Logger file, for example:
 *
 *     date time,latitude,longitude,altitude(m),speed(m/s),sat_used
 *     2026-09-22 23:07:25.000,47.65384674,-117.3701775,565.534,15.03,25
 *
 * HiChan fills in the extra fields (geohash, matched segment, channelId)
 * while it processes the log.
 */
public class GpsRecord {

    // Keep the original line exactly so the output file can copy it and
    // append the Channel ID as the last field (that's what the spec asks for).
    private String originalLine;

    // Important Data
    private String ChannelID;
    private String dateTime;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private int satUsed;

    // INIT
    private String geohash;            // precision-6 geohash of this point
    private HighwaySegment segment;    // best matching segment, or null if no match
    private String channelId;          // e.g. "I90WAEB_290", or null until known


    /* Constructor
     * What it does: stores the parsed values and the original text line.
     *
     * How to code it:
     *   1. Assign each parameter to its field.
     *   2. Leave geohash, segment, and channelId null for now.
     */
    public GpsRecord(String originalLine, String dateTime, double latitude, double longitude,
                     double altitude, double speed, int satUsed) {
        // TODO: assign fields
    } // end of GpsRecord

    /*
     * What it does: parses one data line of the GPS log into a GpsRecord.
     *
     * How to code it:
     *   1. split(",") gives 6 parts:
     *        [0] date time  [1] latitude  [2] longitude
     *        [3] altitude   [4] speed     [5] sat_used
     *   2. Double.parseDouble() parts 1-4 and Integer.parseInt() part 5.
     *   3. return new GpsRecord(line, parts[0], ...).
     *   NOTE: the caller (HiChan.readGpsLog) skips the header line, so this
     *   method only ever gets data lines.
     */
    public static GpsRecord fromCsvLine(String line) {
        // TODO: parse a GPS log line
        return null;
    } // end of fromCsvLine

    /*
     * What it does: returns true if this point and `other` are (practically)
     * the same spot, meaning the truck isn't moving. The spec says to reuse
     * the prior Channel ID in that case.
     *
     * How to code it:
     *   1. If other is null return false.
     *   2. Compare latitude and longitude with a small tolerance instead of ==,
     *      e.g. Math.abs(lat1 - lat2) < 0.00001 (about 1 meter). GPS jitters.
     *   3. (Alternative) also check speed close to 0.
     */
    public boolean isSameLocation(GpsRecord other) {
        // TODO: compare coordinates with a tolerance
        return false;
    } // end of isSameLocation

    /*
     * What it does: the line written to the output file, which is the original
     * line plus the Channel ID as the last field.
     *
     * How to code it:
     *   1. return originalLine + "," + (channelId == null ? "" : channelId);
     *   2. Decide what to write when the channel is still unknown (empty
     *      string, "UNKNOWN", etc.) and stay consistent.
     */
    public String toOutputLine() {
        return dateTime + " (" + latitude + ", " + longitude + ") "
                + geohash + " -> " + segment + " = " + channelId;
    } // end of toOutputLine

    // Getters / Setters
    public String getOriginalLine() { return originalLine; } // end of getOriginalLine

    public String getDateTime() { return dateTime; } // end of getDateTime

    public double getLatitude() { return latitude; } // end of getLatitude

    public double getLongitude() { return longitude; } // end of getLongitude

    public double getSpeed() { return speed; } // end of getSpeed

    public String getGeohash() { return geohash; } // end of getGeohash

    public void setGeohash(String geohash) { this.geohash = geohash; } // end of setGeohash

    public HighwaySegment getSegment() { return segment; } // end of getSegment

    public void setSegment(HighwaySegment segment) { this.segment = segment; } // end of setSegment

    public String getChannelId() { return ChannelID; } // end of getChannelId

    public void setChannelId(String channelId) { this.channelId = channelId; } // end of setChannelId
} // end of GpsRecord