package voronoiPlanner;
import java.util.ArrayList;


/**
 * A simple object for storing voronoi points with some extra helper functions.
 *
 * @author Kevin Cheek
 */
public class VPoint implements Comparable<VPoint> {
	public static final int INFINITY=1000000;
	private int x;
	private int y;
	private int dDistance; //Dijkstra distance
	private VPoint previous; //Dijkstra previous
	private ArrayList<VPoint> neighbors;

	/**
	 * The constructor for the VPoint class
	 *
	 * @param x The points x Coordinate
	 * @param y The points y Coordinate
	 */
	public VPoint(int x, int y) {
		super();
		this.x = x;
		this.y = y;
		neighbors = new ArrayList<VPoint>();
		this.dDistance = INFINITY;
		previous = null;
	}

	/**
	 * Return the distance for dijkstra.
	 *
	 * @return Dijkstra distance
	 */
	public int getDDistance(){
		return dDistance;
	}

	/**
	 * Set the distance for the dijkstra algorithm.
	 *
	 * @param distance
	 */
	public void setDDistance( int distance ){
		dDistance = distance;
	}

	/**
	 * Return the previous node in the dijkstrra path.
	 * @param previous
	 */
	public void setPrevious( VPoint previous ) {
		this.previous = previous;
	}

	/**
	 * Return the previous neighbor in the dijkstra
	 *
	 * @return
	 */
	public VPoint getPrevious() {
		return previous;
	}

	/**
	 * Add a neighbor to the neighbor list
	 *
	 * @param p The new neighbor to add
	 */
	public void addNeighbor( VPoint p ){
		if( p != null){
			neighbors.add(p) ;
		}
	}

	/**
	 * Remove a point from the neighbor list.
	 *
	 * @param p The point to remove
	 */
	public void removeNeighbor( VPoint p ){
		for( VPoint neighbor : neighbors){
			if(  neighbor.equals( p ) ){
				neighbors.remove(p);
				break;
			}
		}
	}

	/**
	 * Compares this point to another point.
	 */
	public boolean equals(Object obj) {
		if( ((VPoint)obj).getX() == x && ((VPoint)obj).getY() == y)
			return true;
		return false;
	}


	/**
	 * Get the x coordinate
	 *
	 * @return The x coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set the x coordinate
	 *
	 * @param The new x coordinate
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Get the y coordinate
	 *
	 * @return  y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set the y coordinate.
	 *
	 * @param y coordinate
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Compares points based on the distance for dijkstra.
	 */
	public int compareTo(VPoint o) {
		return dDistance - ((VPoint)o).getDDistance() ;
	}


	/**
	 * Return the nodes connected to this node in the graph.
	 * @return The list of neighbors
	 */
	public ArrayList<VPoint> getNeighbors() {
		return neighbors;
	}

	/**
	 * Return the euclidean distance rounded to the greatest integer. (Due to using pixels)
	 *
	 * @param x - The x coordinate of the point for comparison.
	 * @param y - The y coordinate fo the point for comparison.
	 * @return
	 */
	public int getPointDistance( int x, int y){
		return (int)(Math.ceil(Math.sqrt( ((this.x - x)*(this.x - x)) + ((this.y - y) * (this.y - y)) ) ) );
	}

	/**
	 * Returns a string representation of this voronoi point.
	 */
	public String toString(){
		return "X: " + x + " Y: " + y;
	}

}
