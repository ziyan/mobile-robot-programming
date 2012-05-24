package common;

/**
 * This is a simple class for the storage of points.
 *
 */
public class Point {
	public double x;
	public double y;

	/**
	 * Defaut constructor for the point class.
	 */
	public Point() {
		this.x = 0.0;
		this.y = 0.0;
	}

	/**
	 * Construct the point class.
	 *
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public Point( final double x, final double y ) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns a string representation of the point class.
	 */
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}


	/**
	 * Finds the angle between two Points, treated as vectors from the origin
	 *
	 * @param start The starting point
	 * @param dest  The ending point
	 * @return		The angle in radians
	 */
	public static double angle( final Point start, final Point dest ) {
		//translate the point so that we have a "vector" pointing from the start,
		//which we treat as the origin
		final Point real = new Point( dest.x - start.x, dest.y - start.y );
		//normalize
		final double length = Utils.elength(real.x, real.y);
		real.x = real.x / length;
		real.y = real.y / length;
		//hooray for atan2
		final double angle = Math.atan2( real.y, real.x );
		return angle;
	}

	/**
	 * Return the euclidean distance between two points
	 *
	 * @param a	 The first point
	 * @param b  The second point
	 * @return 	 The distance.
	 */
	public static double distance( final Point a, final Point b ) {
		return Utils.elength(a.x - b.x, a.y - b.y);
	}
}
