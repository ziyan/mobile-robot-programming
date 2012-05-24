package voronoiPlanner;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * A simple storage class for vornoi points.
 *
 * @author Kevin Cheek
 */
public class PointStorage {
	private TreeMap<Integer, VPoint> vPoints;
	private int xSize;
	private int ySize;

	/**
	 * Constructor for the vornoi class.
	 *
	 * @param xSize The maximum width of the map
	 * @param ySize The maximum heigh of the map
	 */
	PointStorage(final int xSize, final int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;
		this.vPoints = new TreeMap<Integer, VPoint>();
	}

	/**
	 * Return the the smallest voronoi point. The points are sorted in order of the Dijkstra
	 * distance.
	 *
	 * @return The smallest voronoi path.
	 */
	public VPoint getSmallest() {
		return Collections.min(vPoints.values());
	}

	/**
	 * Find the point that is closest to the point given.
	 *
	 * @param start		The point given.
	 * @param maxDistance	The maximum distance away a point can be away. If the distance is
	 * 						negative then the distance is ignored.
	 * @return	The closest voronoi point.
	 */
	public VPoint findClosest(final VPoint start, final int maxDistance) {
		System.out.println("Find Closest");
		VPoint closest = null;
		int distance = start.INFINITY;

		final Collection<VPoint> points = vPoints.values();
		for( final VPoint end: points ){
			if( end != start ){
				final int dist = end.getPointDistance(start.getX(), start.getY());
				if( dist < distance ){
					closest = end;
					distance = dist;
				}
			}
		}

		System.out.println("Closest point: " + closest + " dist: " + distance);
		if( distance < maxDistance || maxDistance == -1)
			return closest;
		return null;
	}

	/**
	 * Set the internal storage class for the voronoi storage.
	 *
	 * @param pts The treemap to store.
	 */
	public void setVPoints(final TreeMap<Integer, VPoint> pts) {
		vPoints = pts;
	}

	/**
	 * Returns the internally stored treemap of voronoi points. This is rarely used.
	 *
	 * @return The internally stored vPoints.
	 */
	public TreeMap<Integer, VPoint> getVPoints() {
		return vPoints;
	}

	/**
	 * Add a new voronoi point to the storage class.
	 *
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public void add(final int x, final int y) {
		vPoints.put((x * xSize) + y, new VPoint(x, y));
	}

	/**
	 * Add a new voronoi point to the storage class.
	 *
	 * @param p The new point.
	 */
	public void add(final VPoint p) {
		vPoints.put((xSize * p.getX()) + p.getY(), p);
	}

	/**
	 * Get a stored voronoi point by object
	 *
	 * @param p Coordinates of the point
	 * @return The reference to the point.
	 */
	public VPoint get(final VPoint p) {
		return get( p.getX(), p.getY() );
	}

	/**
	 * Retreive a stored point by coordinates.
	 *
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @return The reference to the point.
	 */
	public VPoint get(final int x, final int y) {
		return vPoints.get((x * xSize) + y);
	}

	/**
	 * Remove a point.
	 *
	 * @param x The x coordinates
	 * @param y The y coordinates
	 */
	public void remove(final int x, final int y) {
		vPoints.remove((x * xSize) + y);
	}

	/**
	 * Remove a point by reference.
	 *
	 * @param p The point to remove.
	 */
	public void remove(final VPoint p) {
		vPoints.remove((p.getX() * xSize) + p.getY());
	}

	/**
	 * Return an interator to for all of the points stored internally. So we do not
	 * need to ever look at the internal data structure.
	 *
	 * @return An interator of vPoints.
	 */
	public Iterator<VPoint> Iterator() {
		return vPoints.values().iterator();
	}

	/**
	 * Return the number of vPoints stored.
	 *
	 * @return The number of vPoints stored.
	 */
	public int getSize() {
		return vPoints.size();
	}

	/**
	 * Set the maximum width of the map.
	 *
	 * @param xSize The maximum width of the map.
	 */
	public void setXSize(final int xSize) {
		this.xSize = xSize;
	}

	/**
	 * Set the maximum height of the map.
	 *
	 * @param ySize The maximum height of the map.
	 */
	public void setYSize(final int ySize) {
		this.ySize = ySize;
	}

	/**
	 * Reset the dijkstra distance.
	 */
	public void resetDDistance(){
		for( final VPoint p : vPoints.values()){
			p.setDDistance(p.INFINITY);
		}
	}

}
