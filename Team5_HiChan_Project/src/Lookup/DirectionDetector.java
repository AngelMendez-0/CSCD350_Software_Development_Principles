package Lookup;

/*
tracks the estimated mileposts from recent GPS points to decide whether the truck is heading east or west
it needs to handle:
waiting for at least three entries before committing to a direction
reusing the previous channel id when the truck is stopped(repeat location)
the milepost reset when crossing the borders which could otherwise look like a direction change
 */

public class DirectionDetector {

} // end ofDirectionDetector
