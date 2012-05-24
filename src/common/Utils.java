package common;
/**
 * 
 */

/**
 * Utilities
 * @author ziyan
 *
 */
public class Utils {
	/**
	 * Normalize an angle in radian
	 * @param angle
	 * @return
	 */
	public static double normalize(double angle) {
		while(angle >= Math.PI) angle -= Math.PI + Math.PI;
		while(angle < -Math.PI) angle += Math.PI + Math.PI;
		return angle;
	}

	/**
	 * Convert degree to radian
	 * @param degree
	 * @return
	 */
	public static double dtor(final double degree) {
		return degree / 180.0 * Math.PI;
	}

	/**
	 * Convert radian to degree
	 * @param radian
	 * @return
	 */
	public static double rtod(final double radian) {
		return radian / Math.PI * 180.0;
	}

	/**
	 * Calculate euclidean distance
	 * @param dx
	 * @param dy
	 * @return
	 */
	public static double elength(final double dx, final double dy) {
		return Math.sqrt(dx*dx + dy*dy);
	}
}
