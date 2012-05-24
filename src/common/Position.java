/**
 * 
 */

package common;

/**
 * Position candidates
 * @author ziyan
 *
 */
public class Position implements Comparable<Position> {
	private final String name;
	private final double x, y, yaw;
	private double weight;
	/**
	 * Localization Position Candiates
	 * @param name
	 * @param x
	 * @param y
	 * @param yaw
	 */
	public Position(final String name, final double x, final double y, final double yaw) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.yaw = yaw;
		this.weight = 0.0;
	}

	/**
	 * Get point name
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * X position in real player coordinate
	 * @return
	 */
	public double getX() {
		return x;
	}

	/**
	 * Y position in real player coordinate
	 * @return
	 */
	public double getY() {
		return y;
	}

	/**
	 * Yaw
	 * @return
	 */
	public double getYaw() {
		return yaw;
	}

	/**
	 * Weight or likelihood of this position
	 * @return
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Set weight
	 * @param weight
	 */
	public void setWeight(final double weight) {
		this.weight = weight;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Position o) {
		if(this.weight > o.weight) return -1;
		if(this.weight < o.weight) return 1;
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.name + " (" + this.x + ", " + this.y + ", " + this.yaw + ") with prob = " + this.weight;
	}
}
