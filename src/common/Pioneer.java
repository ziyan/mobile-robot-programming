/**
 * 
 */
package common;

/**
 * Pioneer hardware settings
 * @author ziyan
 *
 */
public class Pioneer {
	/**
	 * Pioneer dimension, used to create C-space map
	 */
	public static final double RADIUS = 0.3;
	/**
	 * Number of sonar sensors
	 */
	public static final int SONAR_COUNT = 8;

	/**
	 * Sonar effective range
	 */
	public static final double SONAR_RANGE = 4.0;

	/**
	 * Bearing of each sonar sensors
	 */
	public static final double[] SONAR_BEARINGS = new double[] {
		Utils.dtor(90.0),
		Utils.dtor(50.0),
		Utils.dtor(30.0),
		Utils.dtor(10.0),
		Utils.dtor(-10.0),
		Utils.dtor(-30.0),
		Utils.dtor(-50.0),
		Utils.dtor(-90.0)
	};

	/**
	 * Distances from each sonar sensors to the center of the robot
	 */
	public static final double[] SONAR_OFFSETS = new double[] {
		Utils.elength(0.075, 0.13),
		Utils.elength(0.115, 0.115),
		Utils.elength(0.15, 0.08),
		Utils.elength(0.17, 0.025),
		Utils.elength(0.17, 0.025),
		Utils.elength(0.15, 0.08),
		Utils.elength(0.115, 0.115),
		Utils.elength(0.075, 0.13)
	};

	/**
	 * Sonar field of view
	 */
	public static final double SONAR_FOV = Utils.dtor(15.0);
}
