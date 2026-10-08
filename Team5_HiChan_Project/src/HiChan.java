import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HiChan.java ("Highway Channel")
 *
 * Reads a GPS log, finds which I-90 segment each point is on using the
 * Preprocessor's CSV, works out direction of travel, and writes the GPS
 * log back out with a Channel ID appended as the last field.
 *
 * Channel ID format:  I90 + STATE + DIR + "_" + CHANNEL
 *     I90WAEB_10  -> eastbound I-90 in Washington, milepost 0-10
 *     I90IDWB_30  -> westbound I-90 in Idaho, milepost 20-30
 *
 * Rules from the vision statement:
 *   - Mileposts INCREASE going east and DECREASE going west (within a state).
 *   - Crossing the WA/ID line resets mileposts (Idaho starts at 0).
 *     WA -> ID means eastbound; ID -> WA means westbound.
 *   - Direction needs at least 3 GPS points, so the first couple can't be
 *     labeled right away. Go back and fill them in once direction is known.
 *   - If the GPS point repeats (truck stopped), reuse the prior Channel ID.
 *   - If a point matches no segment, use the next match and go back to fill
 *     in the missed record(s).
 *   - Every point is on I-90, so every record should get an answer in the end.
 *
 * Usage: java HiChan segments.csv "20260922-160725 - I90EB.csv" I90EB_out.csv
 */
public class HiChan {

    /*
     * How to code it:
     *   1. Check args.length >= 3 (index file, GPS log, output file); print usage if not.
     *   2. Map<String, List<HighwaySegment>> index = loadIndex(args[0]);
     *   3. List<GpsRecord> records = readGpsLog(args[1]);
     *   4. processRecords(records, index);
     *   5. writeOutput(records, args[2]);
     *   6. Print a short summary (how many records, how many got a Channel ID).
     *   7. Catch IOException and print an error.
     *   Run it once for the EB file and once for the WB file.
     */
    public static void main(String[] args) {
        // TODO: wire the steps together
    } // end of main

    /*
     * What it does: reads the Preprocessor CSV into a HashMap so lookups by
     * geohash are fast:  geohash -> every segment that touches that cell.
     *
     * How to code it:
     *   1. Map<String, List<HighwaySegment>> index = new HashMap<>();
     *   2. Read the file line by line (BufferedReader or Files.readAllLines).
     *      Skip the header line.
     *   3. HighwaySegment seg = HighwaySegment.fromCsvLine(line);
     *   4. index.computeIfAbsent(seg.getGeohash(), k -> new ArrayList<>()).add(seg);
     *   5. return index;
     *   A List is needed because several segments (e.g. both ends of two
     *   neighboring segments) can share one geohash cell.
     */
    public static Map<String, List<HighwaySegment>> loadIndex(String path) throws IOException {
        Map<String, List<HighwaySegment>> index = new HashMap<>();
        // TODO: read CSV and build geohash -> segments map
        return index;
    } // end of loadIndex

    /*
     * What it does: reads a GPS Logger CSV into a list of GpsRecord objects.
     *
     * How to code it:
     *   1. Read all lines. Skip the first (header) line:
     *        date time,latitude,longitude,altitude(m),speed(m/s),sat_used
     *   2. Skip blank lines.
     *   3. records.add(GpsRecord.fromCsvLine(line));
     *   4. return records;
     *   Hint: keep the header string somewhere (e.g. a static field) so
     *   writeOutput can reuse it with ",channel_id" added.
     */
    public static List<GpsRecord> readGpsLog(String path) throws IOException {
        List<GpsRecord> records = new ArrayList<>();
        // TODO: parse every data line
        return records;
    } // end of readGpsLog

    /*
     * What it does: the main algorithm. Matches every record to a segment,
     * then assigns Channel IDs.
     *
     * How to code it (one workable plan in passes, which is easier to debug):
     *
     *   PASS 1: match segments
     *     for i = 0 .. records.size()-1:
     *        r = records.get(i)
     *        r.setGeohash(GeoHash.encode(r.getLatitude(), r.getLongitude()))
     *        previous = (i > 0) ? records.get(i-1).getSegment() : null
     *        r.setSegment(findBestSegment(r, index, previous))   // may be null
     *
     *   PASS 2: fill gaps
     *     fillMissingSegments(records)   // copy the next/previous good match into nulls
     *
     *   PASS 3: direction + channel
     *     for i = 0 .. size-1:
     *        r = records.get(i)
     *        if i > 0 and r.isSameLocation(records.get(i-1)) and previous
     *            channelId != null -> reuse it and continue
     *        String dir = determineDirection(records, i)   // "EB", "WB" or null
     *        if dir != null:
     *            r.setChannelId(buildChannelId(r.getSegment(), dir))
     *
     *   PASS 4: back-fill early records
     *     backfillChannelIds(records)   // first 1-2 records had no direction yet
     */
    public static void processRecords(List<GpsRecord> records, Map<String, List<HighwaySegment>> index) {
        // TODO: implement the passes described above
    } // end of processRecords

    /*
     * What it does: picks the most likely segment for a GPS point.
     *
     * How to code it:
     *   1. List<HighwaySegment> candidates = index.get(record.getGeohash());
     *   2. If candidates is null or empty:
     *        - (optional) try GeoHash.neighbors(record.getGeohash()) and collect
     *          candidates from those cells instead
     *        - if still nothing, return null (fillMissingSegments handles it later)
     *   3. If there's exactly one candidate, return it.
     *   4. If there are several, choose with a tie-breaker:
     *        a. If previous != null, prefer candidates in the same state
     *           (same getStfips()).
     *        b. Among those, choose the one whose getMidMp() is closest to
     *           previous.getMidMp(). Trucks don't jump many miles in 10 seconds.
     *        c. If previous == null, just return the first candidate.
     *   5. Return the chosen segment.
     */
    public static HighwaySegment findBestSegment(GpsRecord record,
                                                 Map<String, List<HighwaySegment>> index,
                                                 HighwaySegment previous) {
        // TODO: look up candidates and choose the best one
        return null;
    } // end of findBestSegment

    /*
     * What it does: any record whose segment is still null gets the segment of
     * the NEXT record that does have one (spec: "read the next geolocation and
     * try again then go back to the previous record(s) and update").
     *
     * How to code it:
     *   1. Loop i from the END of the list backwards to 0, keeping
     *      HighwaySegment nextGood = null.
     *   2. If records.get(i).getSegment() != null -> nextGood = that segment.
     *      Else if nextGood != null -> records.get(i).setSegment(nextGood).
     *   3. After that, if records at the very end are still null (no "next"),
     *      do a forward pass copying the previous good segment.
     *   Looping backwards fills every gap in a single pass.
     */
    public static void fillMissingSegments(List<GpsRecord> records) {
        // TODO: back-fill null segments
    } // end of fillMissingSegments

    /*
     * What it does: returns "EB", "WB", or null (not enough info yet) for
     * record i by comparing it with the two records before it (3 points total).
     *
     * How to code it:
     *   1. If i < 2 return null (fewer than 3 points so far).
     *   2. Get segments a = records[i-2], b = records[i-1], c = records[i].
     *   3. STATE CHANGE CHECK first (mileposts reset at the border):
     *        - WA ("53") earlier and ID ("16") later -> "EB"
     *        - ID earlier and WA later               -> "WB"
     *   4. Otherwise compare mileposts (use getMidMp()):
     *        - if a <= b <= c and a < c -> "EB" (increasing)
     *        - if a >= b >= c and a > c -> "WB" (decreasing)
     *   5. If all three are the SAME segment (milepost didn't change), it's
     *      unclear. Return the previous record's direction if known
     *      (parse it out of records[i-1].getChannelId()), otherwise null.
     *   Tip: comparing a and c only (ignoring b) is more forgiving if b is noisy.
     */
    public static String determineDirection(List<GpsRecord> records, int i) {
        // TODO: decide EB / WB / null
        return null;
    } // end of determineDirection

    /*
     * What it does: converts a milepost into its 10-mile channel number.
     *   0 to 10  -> 10
     *   10 to 20 -> 20
     *   20 to 30 -> 30   ... and so on
     *
     * How to code it:
     *   1. int channel = (int) Math.ceil(milepost / 10.0) * 10;
     *   2. if channel == 0 (milepost exactly 0) return 10.
     *   3. return channel;
     *   Note: the spec says ranges are "inclusive", so milepost exactly 10.0
     *   could be 10 or 20. Using ceil puts it in 10. Pick one rule and note it
     *   in a comment.
     */
    public static int computeChannelNumber(double milepost) {
        // TODO: round milepost up to the next multiple of 10
        return 0;
    } // end of computeChannelNumber

    /*
     * What it does: assembles the final string, e.g. "I90WAEB_290".
     *
     * How to code it:
     *   1. If segment == null or direction == null, return null.
     *   2. route = segment.getRoute()            -> "I90"
     *      state = segment.getStateAbbrev()      -> "WA" / "ID"
     *      num   = computeChannelNumber(segment.getMidMp())
     *   3. return route + state + direction + "_" + num;
     *   (The spec examples show a leading "#". Ask your instructor whether the
     *   output should include it; it's easy to add here.)
     */
    public static String buildChannelId(HighwaySegment segment, String direction) {
        // TODO: build the Channel ID string
        return null;
    } // end of buildChannelId

    /*
     * What it does: gives a Channel ID to records that couldn't get one when
     * they were processed (the first two records, plus any spot where
     * direction was unclear).
     *
     * How to code it:
     *   1. Find the first record that HAS a channelId and get its direction
     *      (the two letters before "_", e.g. "EB").
     *   2. Walk backwards from there to index 0. For each record with a null
     *      channelId, set buildChannelId(itsOwnSegment, thatDirection).
     *      (Use the record's own segment so the state/channel number stays correct.)
     *   3. Do a forward pass for any remaining nulls, using the most recent
     *      known direction.
     */
    public static void backfillChannelIds(List<GpsRecord> records) {
        // TODO: fill in channel IDs that were left null
    } // end of backfillChannelIds

    /*
     * What it does: writes the updated GPS file. Same rows as the input,
     * with the Channel ID as the last field.
     *
     * How to code it:
     *   1. Open a PrintWriter with try-with-resources.
     *   2. Write the original header + ",channel_id".
     *   3. For each record: writer.println(record.toOutputLine());
     */
    public static void writeOutput(List<GpsRecord> records, String path) throws IOException {
        // TODO: write header + every record
    } // end of writeOutput
} // end of Hichan