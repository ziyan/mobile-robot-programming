/**
 * 
 */
package planning;

import java.util.concurrent.Semaphore;

import common.GridMap;
import common.Point;

/**
 * Planner thread
 * @author ziyan
 *
 */
public class Planner extends Thread{
	/**
	 * Maximum distance the robot can be away from its next sub-goal
	 * before the replan kicks in
	 */
	private static final double DEVIATION_LIMIT = 2.5;

	/**
	 * How close we need to get to a sub-goal, should be smaller than
	 * <code>WAYPOINT_REACH_LIMIT</code>
	 */
	private static final double SUBGOAL_REACH_LIMIT = 0.5;

	/**
	 * How close we need to get to the waypoint, should be larger than
	 * <code>SUBGOAL_REACH_LIMIT</code>
	 */
	private static final double WAYPOINT_REACH_LIMIT = 0.5;

	/**
	 * How long do we pause when reaching the waypoint
	 */
	private static final long WAYPOINT_REACH_PAUSE = 5000;

	/**
	 * The distance of the carrot on the stick
	 */
	private static final int SUBGOAL_STEP = 10;

	private final GridMap cspace;
	private double x;
	private double y;
	private final Point[] waypoints;
	private int waypointIndex;
	private final AStar astar;
	private boolean done;
	private Point next;

	private final Semaphore semaphore;

	/**
	 * Planner thread (automatically starts)
	 * @param waypoints waypoints to be reached in order
	 * @param cspace cspace grid map
	 */
	public Planner(final Point[] waypoints, final GridMap cspace) {
		this.cspace = cspace;
		this.waypoints = waypoints;
		this.waypointIndex = 0;
		this.done = false;
		this.semaphore = new Semaphore(0);
		this.astar = new AStar(cspace);
		this.next = null;
		this.start();
	}

	/**
	 * Next sub-goal
	 * @return
	 */
	public Point getNext() {
		return next;
	}

	/**
	 * Flag to signal when the final goal is reached
	 * @return
	 */
	public boolean isDone() {
		return done;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Point[] path = null;
		int pathIndex = 0;
		Point current;
		while ( !done ) {
			try {
				semaphore.acquire();
				while(semaphore.availablePermits() > 0)
					semaphore.acquire();
			} catch (final InterruptedException e) { }

			synchronized(this) {
				// current location
				current = new Point(x, y);
			}
			if(done) return;

			// check goal
			if(Point.distance(current, waypoints[waypointIndex]) < WAYPOINT_REACH_LIMIT) {
				System.err.println("Planner: Waypoint #"+(waypointIndex+1)+" Reached!");
				// we are done, yaaaaaaay!
				waypointIndex++;
				path = null;
				pathIndex = 0;
				next = current;
				try {
					// stop at goal for a while to enjoy ourselves
					Thread.sleep(WAYPOINT_REACH_PAUSE);
				} catch (final InterruptedException e) { }
				if(waypointIndex >= waypoints.length)
					break;
			}

			// replan
			if(path == null || Point.distance(path[pathIndex], current) > DEVIATION_LIMIT) {
				System.err.println("Planner: Replanning! from "+current + " to "+waypoints[waypointIndex]);
				next = null;
				// no path, we need to compute new path
				path = astar.compute(current, waypoints[waypointIndex]);
				pathIndex = 0;
				if(path == null) {
					// this may happen if you really screw up in the waypoint file
					// or temporarily the robot localizes to a wrong starting point.
					System.err.println("Planner: path impossible for waypoint #" + (waypointIndex+1));
				}
				cspace.setPath(path);
			}

			// find next sub-goal
			if(path != null)
				// this allows the robot to skip some sub-goal
				// we don't want to be too exact about our path
				for(int i = path.length - 1; i >= pathIndex; i--) {
					// found closest path
					if(Point.distance(path[i], current) < SUBGOAL_REACH_LIMIT) {
						pathIndex = i + SUBGOAL_STEP;
						if(pathIndex >= path.length)
							pathIndex = path.length - 1;
						next = path[pathIndex];
						System.out.println("Planner: approach to next sub-goal: " + next);
					}
				}
		}
		done = true;
	}

	/**
	 * Shutdown planner thread (gracefully)
	 */
	public void shutdown() {
		if(done) return;
		this.done = true;
		this.interrupt();
	}


	/**
	 * Update planner with newest current location
	 * @param x
	 * @param y
	 * @return
	 */
	public Point update(final double x, final double y) {
		synchronized(this) {
			this.x = x;
			this.y = y;
		}
		semaphore.release();
		return next;
	}
}
