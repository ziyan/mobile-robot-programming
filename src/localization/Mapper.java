/**
 * 
 */

package localization;

import java.util.concurrent.Semaphore;

import common.GridMap;
import common.Pioneer;
import common.Utils;


/**
 * Mapper thread
 * @author ziyan
 *
 */
public class Mapper extends Thread {
	/**
	 * Extened sonar range used to calculate bounding box
	 */
	private static final double SONAR_EDGE = Pioneer.SONAR_RANGE / Math.cos(Pioneer.SONAR_FOV / 2.0);
	/**
	 * Sonar model setting
	 */
	private static final double D1 = 0.25;
	/**
	 * Sonar model setting
	 */
	private static final double D2 = 0.5;
	/**
	 * Sonar model setting
	 */
	private static final double D3 = 0.75;

	private final GridMap map;

	private float[] ranges;
	private double x;
	private double y;
	private double yaw;
	private final Semaphore semaphore;

	private boolean done;

	/**
	 * Mapper thread
	 * @param map local map to be updated
	 */
	public Mapper(final GridMap map) {
		this.map = map;
		this.done = false;
		this.semaphore = new Semaphore(0);
		this.setPriority(4);
		this.start();
	}

	/**
	 * Update robot location in local odometry (not the real)
	 * @param ranges sonar readings
	 * @param x
	 * @param y
	 * @param yaw
	 */
	public void update(final float[] ranges, final double x, final double y, final double yaw) {
		synchronized(this) {
			this.ranges = ranges;
			this.x = x;
			this.y = y;
			this.yaw = yaw;
		}
		semaphore.release();
	}

	/**
	 * Shutdown gracefully
	 */
	public void shutdown() {
		this.done = true;
		this.interrupt();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		double x, y, yaw;
		float[] ranges;
		while(!done) {
			try {
				semaphore.acquire();
				while(semaphore.availablePermits() > 0)
					semaphore.acquire();
			} catch (final InterruptedException e) { }
			if(done) return;

			synchronized(this) {
				x = this.x;
				y = this.y;
				yaw = this.yaw;
				ranges = this.ranges;
			}

			map(map, x, y);
			map(map, ranges, x, y, yaw);
		}

	}

	public static void map(final GridMap map, final double x, final double y) {
		final int left = (int)((x - Pioneer.RADIUS) / map.getMPP());
		final int right = (int)((x + Pioneer.RADIUS) / map.getMPP());
		final int top = (int)((y - Pioneer.RADIUS) / map.getMPP());
		final int bottom = (int)((y + Pioneer.RADIUS) / map.getMPP());
		for(int w = left; w <= right; w++) {
			for(int h = top; h <= bottom; h++) {
				final int ww = w + map.getWidth()/2;
				final int hh = map.getHeight() - (h + map.getHeight()/2);
				if(ww < 0 || hh < 0 || ww >= map.getWidth() || hh >= map.getHeight()) continue;

				// convert to precise cordinates
				final double dw = map.getMPP() * w;
				final double dh = map.getMPP() * h;
				final double t = Utils.elength(x - dw, y - dh);

				// discard those out of range
				if(t > Pioneer.RADIUS) continue;

				map.setData(hh, ww, GridMap.TRAVERSABLE);
			}
		}
	}

	public static void map(final GridMap map, final float[] ranges, final double x, final double y, final double yaw) {
		for(int i = 0; i < ranges.length && i < Pioneer.SONAR_COUNT; i++) {
			// sonar bearing
			final double bearing = Utils.normalize(Pioneer.SONAR_BEARINGS[i] + yaw);

			// calculate sonar absolute position
			final double sx = x + Pioneer.SONAR_OFFSETS[i] * Math.cos(bearing);
			final double sy = y + Pioneer.SONAR_OFFSETS[i] * Math.sin(bearing);

			final double x1 = sx + SONAR_EDGE * Math.cos(bearing + Pioneer.SONAR_FOV/2.0);
			final double y1 = sy + SONAR_EDGE * Math.sin(bearing + Pioneer.SONAR_FOV/2.0);
			final double x2 = sx + SONAR_EDGE * Math.cos(bearing - Pioneer.SONAR_FOV/2.0);
			final double y2 = sy + SONAR_EDGE * Math.sin(bearing - Pioneer.SONAR_FOV/2.0);

			double xMin = ( x1 < x2 ? x1 : x2 );
			xMin = ( xMin < sx ? xMin : sx );
			double xMax = ( x1 > x2 ? x1 : x2 );
			xMax = ( xMax > sx ? xMax : sx );
			double yMin = ( y1 < y2 ? y1 : y2 );
			yMin = ( yMin < sy ? yMin : sy );
			double yMax = ( y1 > y2 ? y1 : y2 );
			yMax = ( yMax > sy ? yMax : sy );

			// calculate bounding box
			final int left = (int)(xMin / map.getMPP());
			final int right = (int)(xMax / map.getMPP());
			final int top = (int)(yMin / map.getMPP());
			final int bottom = (int)(yMax / map.getMPP());

			for(int w = left; w <= right; w++) {
				for(int h = top; h <= bottom; h++) {

					// convert to map coordinate
					final int ww = w + map.getWidth()/2;
					final int hh = map.getHeight() - (h + map.getHeight()/2);
					if(ww < 0 || hh < 0 || ww >= map.getWidth() || hh >= map.getHeight()) continue;

					// convert to precise cordinates
					final double dw = map.getMPP() * w;
					final double dh = map.getMPP() * h;

					// calculate bearing
					final double da = Utils.normalize(Utils.normalize(Math.atan2(dh - sy, dw - sx)) - bearing);

					if(Math.abs(da) > Pioneer.SONAR_FOV / 2.0) continue;

					// calculat range
					final double t = Utils.elength(sx - dw, sy - dh);

					// discard those out of range
					//if(t > Pioneer.SONAR_RANGE) continue;


					int prob = (int)((GridMap.INTRAVERSABLE - GridMap.TRAVERSABLE) *
							(model(ranges[i], da, t) +
									(double)map.getData(hh, ww)/(double)(GridMap.INTRAVERSABLE - GridMap.TRAVERSABLE)));
					if(prob > GridMap.INTRAVERSABLE) prob = GridMap.INTRAVERSABLE;
					if(prob < GridMap.TRAVERSABLE) prob = GridMap.TRAVERSABLE;
					// update probability model
					map.setData(hh, ww, (byte)prob);
				}
			}
		}
	}

	public static double model(final double range, final double a, final double d) {

		final double g = -0.01 * (range > Pioneer.SONAR_RANGE ? Pioneer.SONAR_RANGE : range) + 0.05;
		final double n = 1.0 / (0.05 * 2.0 * Math.PI) * Math.exp(a*a/-2.0);
		final double s = g * n;
		if ( d < range - D1 ) {
			return -s;
		} else if ( d < range + D1 ) {
			return -s + s / D1 * ( d - range + D1 );
		} else if ( d < range + D2 ) {
			return s;
		} else if ( d < range + D3 ) {
			return s - s / (D3 - D2) * ( d - range - D2 );
		} else {
			return 0.0;
		}
	}
}
