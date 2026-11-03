### Hi_Chan
## Team 5 (Angel M, Mahara D, Gabe W). CSCD 350, Fall 2026.
Labels each truck GPS reading on I-90 in Washington or Idaho with a Channel ID,
e.g. `#I90WAEB_290` (highway, state, direction, 10-mile segment).
Requirements
Java 25 (JDK 21+ works). Standard library only; no database or network.
Build
```
javac -d out src/Hi_Chan/*.java src/Testers/*.java
```
Use
Build the lookup file (run once):
```
   java -cp out Hi_Chan.Preprocessor res/I90_WA_ID.json segments.csv
   ```
Label a GPS log:
```
   java -cp out Hi_Chan.HiChan segments.csv "res/20260922-160725 - I90EB.csv" I90EB_out.csv
   ```
The output is the GPS log with `channel_id` added as the last field.
`??` means direction is not known yet. A bad line ends with `SKIPPED-Error:<reason>`.
Errors print `ERROR:<file>:<reason>` and exit with status 1.
Test
Run from the project folder:
```
java -cp out Testers.AllTesters
```
Files
Path	Contents
`src/Hi_Chan`	Preprocessor, HiChan, GeoHash, HighwaySegment, GpsRecord, HiChanConfig
`src/Testers`	One tester per class, plus AllTesters
`res`	NHPN I-90 JSON (data: NHPN, U.S. DOT) and the EB/WB GPS logs
