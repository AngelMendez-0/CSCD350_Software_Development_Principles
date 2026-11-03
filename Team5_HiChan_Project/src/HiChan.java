package Hi_Chan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hi_Chan.HiChan.java ("Highway Channel")
 *
 * Reads a GPS log, finds the nearest I-90 point for each reading using the
 * Hi_Chan.Preprocessor's CSV, works out direction of travel, and writes the GPS
 * log back out with a Channel ID appended as the last field.
 *
 * Channel ID format (FR-13):  # + I90 + STATE + DIR + "_" + CHANNEL
 *     #I90WAEB_290  -> eastbound I-90 in Washington, milepost 280-290
 *     #I90IDWB_10   -> westbound I-90 in Idaho, milepost 0-10
 *     #I90WA??_290  -> direction not known yet (FR-10)
 *
 * Rules from the vision statement:
 *   - Mileposts INCREASE going east and DECREASE going west (within a state).
 *   - Crossing the WA/ID line resets mileposts (Idaho starts at 0).
 *     WA -> ID means eastbound; ID -> WA means westbound.
 *   - Direction needs 3 GPS points: entry i is compared with entry i-2 (AD-6).
 *     Earlier entries get ?? and are back-filled once direction is known (FR-12).
 *   - If the GPS point repeats (truck stopped), reuse the prior Channel ID (FR-11).
 *   - A bad line stays in place ending with SKIPPED-Error:<reason> (FR-14).
 *
 * Usage: java Hi_Chan.HiChan segments.csv "20260922-160725 - I90EB.csv" I90EB_out.csv
 */
public class HiChan {

    /*
     * How to code it:
     *   1. If args.length != 3, print
     *        Usage: java Hi_Chan.HiChan <lookup.csv> <gps.csv> <out.csv>
     *      to System.err and System.exit(1).
     *   2. If args[2] is the same file as args[0] or args[1], refuse it (NFR-S1).
     *   3. Map<String, List<Hi_Chan.HighwaySegment>> index = loadIndex(args[0]);
     *   4. List<Hi_Chan.GpsRecord> records = readGpsLog(args[1]);
     *   5. processRecords(records, index);
     *   6. backfillChannelIds(records);
     *   7. writeOutput(records, args[2]);
     *   8. Print a short summary, e.g. "Labeled 128 of 128 entries".
     *   9. Catch IOException: print ONE line to System.err,
     *        ERROR:<file name>:<e.getMessage()>
     *      with no stack trace, and System.exit(1) (NFR-U2).
     *   Run it once for the EB file and once for the WB file.
     */
    public static void main(String[] args) {
        // TODO: wire the steps together
    } // end of main

    /*
     * What it does: reads the Hi_Chan.Preprocessor CSV into a HashMap so lookups by
     * geohash are fast:  geohash -> every highway point in that cell.
     *
     * How to code it:
     *   1. Throw new IOException(reason) when the file can't be used, with reason:
     *        "file not found", "file not readable", "file empty" (no rows),
     *        "wrong header line" (must be GEOHASH,ROUTE,STFIPS,BEGMP,ENDMP,LAT,LON,CHANNEL),
     *        "lookup file not sorted" (a geohash smaller than the one before it).
     *   2. Read the file line by line; trim each line so CRLF files work. Skip the header.
     *   3. Hi_Chan.HighwaySegment seg = Hi_Chan.HighwaySegment.fromCsvLine(line);
     *   4. index.computeIfAbsent(seg.getGeohash(), k -> new ArrayList<>()).add(seg);
     *   5. return index;
     */
    public static Map<String, List<HighwaySegment>> loadIndex(String path) throws IOException {
        Map<String, List<HighwaySegment>> index = new HashMap<>();
        // TODO: read CSV and build geohash -> points map
        return index;
    } // end of loadIndex

    /*
     * What it does: reads a GPS Logger CSV into a list of Hi_Chan.GpsRecord objects.
     *
     * How to code it:
     *   1. Throw new IOException(reason) with "file not found", "file not readable",
     *      "file empty" or "wrong header line". The header must be:
     *        date time,latitude,longitude,altitude(m),speed(m/s),sat_used
     *   2. Remember the header and the line ending ("\r\n" if the file has one, else "\n")
     *      in static fields so writeOutput can reuse them (NFR-D1).
     *   3. Skip blank lines.
     *   4. records.add(Hi_Chan.GpsRecord.fromCsvLine(line));
     *      If it throws IllegalArgumentException, keep the line in its place instead:
     *        GpsRecord bad = new GpsRecord(line, null, 0, 0, 0, 0, 0);
     *        bad.setErrorText(e.getMessage());   // FR-14
     *   5. return records;
     */
    public static List<GpsRecord> readGpsLog(String path) throws IOException {
        List<GpsRecord> records = new ArrayList<>();
        // TODO: parse every data line
        return records;
    } // end of readGpsLog

    /*
     * What it does: the main algorithm. Matches every record to a highway point,
     * then builds its Channel ID. (main calls backfillChannelIds afterwards.)
     *
     * How to code it:
     *     for i = 0 .. records.size()-1:
     *        r = records.get(i); skip it if r.getErrorText() != null
     *        r.setGeohash(Hi_Chan.GeoHash.encode(r.getLatitude(), r.getLongitude()))
     *        seg = findBestSegment(r, index, previous point)
     *        if seg == null: r.setErrorText("no I-90 point near reading") and continue
     *        r.setSegment(seg)
     *        if r.isSameLocation(previous good record) and that record's Channel ID
     *           has a known direction (not ??) -> reuse its segment and Channel ID (FR-11)
     *        else r.setChannelId(buildChannelId(seg, determineDirection(records, i)))
     */
    public static void processRecords(List<GpsRecord> records, Map<String, List<HighwaySegment>> index) {
        // TODO: match and label every record
    } // end of processRecords

    /*
     * What it does: picks the highway point nearest the GPS reading (FR-06).
     *
     * How to code it:
     *   1. List<Hi_Chan.HighwaySegment> candidates = index.get(record's geohash)
     *      (encode it yourself if record.getGeohash() is null).
     *   2. If candidates is null or empty, collect the points in the 8 cells from
     *      Hi_Chan.GeoHash.neighbors(...) instead (fallback only, AD-2).
     *      If there are still none, return null.
     *   3. Return the candidate with the smallest
     *      Hi_Chan.GeoHash.haversineMiles(reading lat, lon, point lat, lon).
     *      On an exact tie, prefer the point in the same state as `previous`.
     */
    public static HighwaySegment findBestSegment(GpsRecord record,
                                                 Map<String, List<HighwaySegment>> index,
                                                 HighwaySegment previous) {
        // TODO: look up candidates and choose the nearest
        return null;
    } // end of findBestSegment

    /*
     * What it does: returns "EB", "WB" or "??" for record i (FR-09, FR-10).
     *
     * How to code it:
     *   1. Find the two good records before i (skip any with errorText or no segment).
     *      If there aren't two, return "??".
     *   2. a = segment of the record two back, c = segment of record i.
     *   3. STATE CHANGE CHECK first (mileposts reset at the border):
     *        - a is WA ("53") and c is ID ("16") -> "EB"
     *        - a is ID and c is WA               -> "WB"
     *   4. Otherwise compare mileposts:
     *        - c.getBegMp() > a.getBegMp() (or equal BEGMP and bigger ENDMP) -> "EB"
     *        - c.getBegMp() < a.getBegMp() (or equal BEGMP and smaller ENDMP) -> "WB"
     *   5. Same segment: return the direction in the previous good record's Channel ID
     *      (the two letters before "_"), or "??" if it has none.
     */
    public static String determineDirection(List<GpsRecord> records, int i) {
        // TODO: decide EB / WB / ??
        return null;
    } // end of determineDirection

    /*
     * What it does: converts a milepost into its 10-mile channel number (FR-08).
     *   0 to 10  -> 10
     *   10.1 to 20 -> 20
     *   20.1 to 30 -> 30   ... and so on
     *
     * How to code it:
     *   1. int channel = (int) Math.ceil(milepost / HiChanConfig.CHANNEL_MILES) * HiChanConfig.CHANNEL_MILES;
     *   2. if channel == 0 (milepost exactly 0) return 10.
     *   3. return channel;
     *   Milepost exactly 10.0 gives 10 (inclusive upper bound).
     */
    public static int computeChannelNumber(double milepost) {
        // TODO: round milepost up to the next multiple of 10
        return 0;
    } // end of computeChannelNumber

    /*
     * What it does: assembles the Channel ID, e.g. "#I90WAEB_290" (FR-13).
     *
     * How to code it:
     *   1. If point == null, return null. If direction == null, use "??".
     *   2. return HiChanConfig.CHANNEL_PREFIX + point.getRoute() + point.getStateAbbrev()
     *             + direction + "_" + point.getChannel();
     */
    public static String buildChannelId(HighwaySegment point, String direction) {
        // TODO: build the Channel ID string
        return null;
    } // end of buildChannelId

    /*
     * What it does: replaces "??" once direction is known (FR-12).
     *
     * How to code it:
     *   1. Find the first record whose Channel ID has a known direction
     *      (the two letters before "_" are "EB" or "WB"). If none, leave everything
     *      as is: a log with only two entries keeps ?? (FR-10).
     *   2. Every earlier "??" record gets buildChannelId(itsOwnSegment, thatDirection).
     *   3. Any later "??" record takes the most recent known direction.
     *   Skip records with errorText.
     */
    public static void backfillChannelIds(List<GpsRecord> records) {
        // TODO: fill in ?? once direction is known
    } // end of backfillChannelIds

    /*
     * What it does: writes the updated GPS file. Same lines as the input, in the
     * same order, with the Channel ID as the last field (FR-12).
     *
     * How to code it:
     *   1. Open a PrintWriter with try-with-resources.
     *   2. Write the saved header + ",channel_id", using the saved line ending.
     *   3. For each record: write record.toOutputLine() + the saved line ending.
     */
    public static void writeOutput(List<GpsRecord> records, String path) throws IOException {
        // TODO: write header + every record
    } // end of writeOutput
} // end of Hichan
