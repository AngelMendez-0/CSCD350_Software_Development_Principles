package Hi_Chan;

/*
 * One line from a GPS Logger file, for example:
 *
 *     date time,latitude,longitude,altitude(m),speed(m/s),sat_used
 *     2026-09-22 23:07:25.000,47.65384674,-117.3701775,565.534,15.03,25
 *
 * Hi_Chan.HiChan fills in the extra fields (geohash, matched segment, channelId)
 * while it processes the log.
 */
public class GpsRecord {

    // Keep the original line exactly so the output file can copy it and
    // append the Channel ID as the last field (that's what the spec asks for).
    private String originalLine;

    // Important Data
    private String dateTime;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private int satUsed;

    // INIT
    private String geohash;            // precision-5 geohash of this point
    private HighwaySegment segment;    // nearest highway point, or null if no match
    private String channelId;          // e.g. "#I90WAEB_290" or "#I90WA??_290", null until built
    private String errorText;          // why the line was skipped (FR-14), or null


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
    } // end of Hi_Chan.GpsRecord

    /*
     * What it does: parses one data line of the GPS log into a Hi_Chan.GpsRecord.
     *
     * How to code it:
     *   1. Drop a trailing "\r" so CRLF and LF files both work (NFR-D1).
     *   2. split(",", -1) must give exactly 6 parts:
     *        [0] date time  [1] latitude  [2] longitude
     *        [3] altitude   [4] speed     [5] sat_used
     *   3. trim() each part, Double.parseDouble() parts 1-4 and Integer.parseInt() part 5.
     *   4. If anything is wrong (null line, wrong field count, empty date time, a value
     *      that isn't a number, latitude outside -90..90, longitude outside -180..180),
     *      throw IllegalArgumentException with a short reason that has NO commas,
     *      e.g. "expected 6 fields but found 5". readGpsLog writes that reason after
     *      SKIPPED-Error: (FR-14).
     *   5. return new Hi_Chan.GpsRecord(line without "\r", parts[0], ...).
     *   NOTE: the caller (Hi_Chan.HiChan.readGpsLog) skips the header line, so this
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
     *   1. If other is null, or either record has errorText set, return false.
     *   2. Compare latitude and longitude with HiChanConfig.SAME_LOCATION_TOLERANCE
     *      instead of ==, e.g. Math.abs(lat1 - lat2) < 0.00001 (about 1 meter). GPS jitters.
     */
    public boolean isSameLocation(GpsRecord other) {
        // TODO: compare coordinates with a tolerance
        return false;
    } // end of isSameLocation

    // the line written to the output file: the original line plus the Channel ID
    // as the last field (FR-12), or SKIPPED-Error:<reason> for a bad line (FR-14)
    public String toOutputLine() {
        if (errorText != null)
            return originalLine + ",SKIPPED-Error:" + errorText;
        return originalLine + "," + (channelId == null ? "" : channelId);
    } // end of toOutputLine

    // Getters / Setters
    public String getOriginalLine() { return originalLine; } // end of getOriginalLine

    public String getDateTime() { return dateTime; } // end of getDateTime

    public double getLatitude() { return latitude; } // end of getLatitude

    public double getLongitude() { return longitude; } // end of getLongitude

    public double getAltitude() { return altitude; } // end of getAltitude

    public double getSpeed() { return speed; } // end of getSpeed

    public int getSatUsed() { return satUsed; } // end of getSatUsed

    public String getGeohash() { return geohash; } // end of getGeohash

    public void setGeohash(String geohash) { this.geohash = geohash; } // end of setGeohash

    public HighwaySegment getSegment() { return segment; } // end of getSegment

    public void setSegment(HighwaySegment segment) { this.segment = segment; } // end of setSegment

    public String getChannelId() { return channelId; } // end of getChannelId

    public void setChannelId(String channelId) { this.channelId = channelId; } // end of setChannelId

    public String getErrorText() { return errorText; } // end of getErrorText

    public void setErrorText(String errorText) { this.errorText = errorText; } // end of setErrorText
} // end of Hi_Chan.GpsRecord
