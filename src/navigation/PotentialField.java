package navigation;

import java.util.concurrent.Semaphore;

import javaclient2.Position2DInterface;

import common.Pioneer;
import common.Point;
import common.Utils;

/**
 * A class for navigating to a point using artifical potential fields.
 * 
 * @author ziyan, zach
 */
public class PotentialField extends Thread {
	//constants
	private static final double K = 1.0;
	private static final double FORWARD_FORCE = 3.5;
	private static final double SPEED_LIMIT = 0.5;
	private static final double TURNRATE_LIMIT = Math.PI / 8.0;
	private static final double SPEED_SCALE = 1.0;
	private static final double TURNRATE_SCALE = 1.0;
	private static final double CLOSE_DISTANCE = 0.1;

	//robot info (sonar readings and position)
	private float[] ranges;
	private double x;
	private double y;
	private double yaw;
	private Point waypoint;

	private boolean done;
	private final Position2DInterface p2d;

	//controls concurrency
	private final Semaphore semaphore;

	/**
	 * Creates a new PotentialField navigator.
	 * 
	 * @param p2d The motor interface for the robot.
	 */
	public PotentialField(final Position2DInterface p2d) {
		this.p2d = p2d;
		this.done = false;
		this.waypoint = null; // null just means random walk
		this.semaphore = new Semaphore(0);
		this.start();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		double yaw;
		float[] ranges;
		Point waypoint, current;

		while ( !done ) {
			try {
				//block until there's something to do
				semaphore.acquire();
				while(semaphore.availablePermits() > 0)
					semaphore.acquire();
			} catch (final InterruptedException e) { }
			if(done) return;

			//pull in class variables into this thread's stack, because they'll be
			//overwritten if update() is called before we finish.
			synchronized(this) {
				yaw = this.yaw;
				ranges = this.ranges;
				waypoint = this.waypoint;
				current = new Point(this.x, this.y);
			}

			//stop if we're sufficiently close to the waypoint
			if(waypoint != null && Point.distance(current, waypoint) < 0.25) {
				p2d.setSpeed(0.0f, 0.0f);
				continue;
			}

			//initial forces
			double x = 0.0, y = 0.0, f;
			int count = 0;
			boolean close = false;
			for(int i = 0; i < ranges.length && i < Pioneer.SONAR_COUNT; i++) {
				if(ranges[i] > Pioneer.SONAR_RANGE) continue;
				ranges[i] = Math.abs(ranges[i]) + 0.00001f;
				//compute the magnitude of the force using an inverse cube and
				//linear inverse term
				f = K / (ranges[i] * ranges[i] * ranges[i] * 8.0) + K * 3.0 / ranges[i];
				//compute the x and y components of this force and update the
				//overall force acting on the robot
				x -= f * Math.cos(Pioneer.SONAR_BEARINGS[i]);
				y -= f * Math.sin(Pioneer.SONAR_BEARINGS[i]);
				count ++;
				//check if we're about to bang into something
				if(ranges[i] < CLOSE_DISTANCE)
					close = true;
			}
			if(count > 0) {
				x /= (double)count;
				y /= (double)count;
			}
			//System.err.println("APF: sonar force = (" + x + ", " + y + ")");

			//are we going somewhere or doing random walk?
			if(waypoint != null) {
				//compute the angle of the force attracting the robot to the goal
				final double angle = Utils.normalize(Point.angle(current, waypoint) - yaw);
				//System.err.println("APF: direction = " + angle + " distance = " + Point.distance(current,waypoint));
				if(!close) {
					x += Math.cos(angle) * FORWARD_FORCE;
					y += Math.sin(angle) * FORWARD_FORCE;
				}
				//System.err.println("APF: total force = (" + x + ", " + y + ")");
			} else {
				// random walk
				x += FORWARD_FORCE;
			}

			double speed = Utils.elength(x,y) * SPEED_SCALE;
			double turnrate = Utils.normalize(Math.atan2(y, x)) * TURNRATE_SCALE;
			//if we need to turn a lot, slow down a lot
			if(Math.abs(turnrate) > Math.PI / 2.0)
				speed = 0.03;
			//LEFT WHEEL VELOCITY THRESHOLDED
			if(turnrate > TURNRATE_LIMIT) {
				turnrate = TURNRATE_LIMIT;
			} else if (turnrate < -TURNRATE_LIMIT) {
				turnrate = -TURNRATE_LIMIT;
			}

			//don't go too fast
			if(speed > SPEED_LIMIT)
				speed = SPEED_LIMIT;
			else if(speed < 0.05)
				speed = 0.05;
			p2d.setSpeed((float)speed, (float)turnrate);
		}
	}

	/**
	 * Stops this navigator.
	 */
	public void shutdown() {
		this.done = true;
		this.interrupt();
	}

	/**
	 * Updates the waypoint that this navigator will attempt to travel to.  Use
	 * null to indicate a random walk.
	 *
	 * @param waypoint The point to attempt to reach.
	 */
	public void update( final Point waypoint ) {
		synchronized(this) {
			this.waypoint = waypoint;
		}
	}

	/**
	 * Update with readings from the robot.
	 * 
	 * @param ranges The sonar readings.
	 * @param x The x coord of the robot.
	 * @param y The y coord of the robot.
	 * @param yaw The angle of the robot.
	 */
	public void update( final float[] ranges, final double x, final double y, final double yaw) {
		update(ranges, x, y, yaw, waypoint);
	}

	/**
	 * Update with readings from the robot.
	 * 
	 * @param ranges The sonar readings.
	 * @param x The x coord of the robot.
	 * @param y The y coord of the robot.
	 * @param yaw The angle of the robot.
	 * @param waypoint The point to attempt to reach.
	 */
	public void update( final float[] ranges, final double x, final double y, final double yaw, final Point waypoint ) {
		synchronized(this) {
			this.ranges = ranges;
			this.x = x;
			this.y = y;
			this.yaw = yaw;
			this.waypoint = waypoint;
		}
		semaphore.release();
	}
}
