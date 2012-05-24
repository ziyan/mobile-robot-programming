package voronoiPlanner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Voronoi based path planner.
 *
 * @author Kevin Cheek
 */
public class PathPlanner {
	private int[][] dM; //Distance Matrix - Distance point is away from an obstacle
	private PlannerGridMap gr; //The grid map
	private PointStorage m_vPoints; //Voronoi point storage
	private int m_xLength; //Map X length
	private int m_yLength; //Map Y Length
	private static Color[] colorMap; //The way to colorize the map
	private int[][] m_overwrite; //Number of times the points have been overwritten


	/**
	 * A main function so the voronoi generator can be tested. To operate change the path variable to the raw input map then uncomment the brushfire
	 * implementation you would like to try.
	 *
	 * @param args
	 */
	public static void main(final String[] args) {


		final String path = "/Users/kevincheek/workspace/PathPlanning/bin/3large.raw" ;
		final PathPlanner pp = new PathPlanner(1600, 500, path, 60);

		//The bad but connected voronoi
		pp.brushFire();

		//The correct looking but with smaller gaps and problems voronoi.
	//	pp.brushFire2();


		//Test out dijkstra
	//	pp.createPath(200, 300, 800, 300);

	}


	/**
	 * Initializes the path planner
	 *
	 * @param xLength - Maximum width of map
	 * @param yLength - Maximum height of map
	 * @param file - The path to the input map
	 */
	public PathPlanner(final int xLength, final int yLength, final String file, final int maxDistance) {
		try{
			m_xLength = xLength;
			m_yLength = yLength;
			dM = new int[m_xLength][m_yLength];
			final FileInputStream in = new FileInputStream(file);
			m_vPoints = new PointStorage(m_xLength, m_yLength);
			gr = new PlannerGridMap(m_xLength, m_yLength);
			m_overwrite = new int[xLength][yLength];

			int b;
			for( int y = 0; y < 500; y++ ){
				for( int x = 0; x < 1600; x++ ){
					b = in.read();
					if( b >= 0 ){
						if( b == 255 ){
							gr.setVal(x, y, Color.black.getRGB());
							dM[ x ][ y ] = 1;
						}else{
							gr.setVal(x, y, Color.white.getRGB());
							dM[ x ][ y ] = -1;
						}
					}else{
						System.err.println("err reading raw");
						return;
					}
				}
			}
			gr.repaint();
			initializeColors(maxDistance);
			markEdges();

			System.out.println("done");

		}catch( final FileNotFoundException e ){
			e.printStackTrace();
		}catch( final IOException e ){
			e.printStackTrace();
		}
	}

	/**
	 * Brushfire algorithm
	 *
	 * This algorithm is supposed to generate a Voronoi diagram based on distances from obstacles.
	 * However, no matter what variation on the algorithm I use it is not functional.
	 * Either the graph will not be connected or the the graph is too thick and does not
	 * work properly.
	 *
	 * The algorithm seen here is from this book:
	 * http://books.google.com/books?id=1_rMmj9O-xYC&pg=PA257
	 *
	 */
	public void brushFire() {
		int curDist = 2;
		boolean done = false;
		while( !done ){
			done = true;
			for( int y = 1; y < m_yLength - 1; y++ ){
				for( int x = 1; x < m_xLength - 1; x++ ){
					if( dM[ x ][ y ] == curDist ){
						if( dM[ x ][ y + 1 ] != -1 ){
							if( gr.getVal(x, y + 1) != gr.getVal(x, y) ){
								m_overwrite[ x ][ y + 1 ]++;
							}
							done = false;
						}else if( dM[ x ][ y + 1 ] != 1 ){
							gr.setVal(x, y + 1, gr.getVal(x, y));
							dM[ x ][ y + 1 ] = curDist + 1;
						}

						if( dM[ x ][ y - 1 ] != -1 ){
							if( gr.getVal(x, y - 1) != gr.getVal(x, y) ){
								m_overwrite[ x ][ y - 1 ]++;
							}
							done = false;
						}else if( dM[ x ][ y + 1 ] != 1 ){
							gr.setVal(x, y - 1, gr.getVal(x, y));
							dM[ x ][ y - 1 ] = curDist + 1;
						}

						if( dM[ x + 1 ][ y ] != -1 ){
							if( gr.getVal(x + 1, y) != gr.getVal(x, y) ){
								m_overwrite[ x + 1 ][ y ]++;
							}
							done = false;
						}else{
							gr.setVal(x + 1, y, gr.getVal(x, y));
							dM[ x + 1 ][ y ] = curDist + 1;
						}

						if( dM[ x - 1 ][ y ] != -1 ){
							if( gr.getVal(x - 1, y) != gr.getVal(x, y) ){
								m_overwrite[ x - 1 ][ y ]++;
								gr.repaint();
							}
							done = false;
						}else{
							gr.setVal(x - 1, y, gr.getVal(x, y));
							dM[ x - 1 ][ y ] = curDist + 1;
						}

						if( dM[ x - 1 ][ y - 1 ] != -1 ){
							if( gr.getVal(x - 1, y - 1) != gr.getVal(x, y) ){
								m_overwrite[ x - 1 ][ y - 1 ]++;
								gr.repaint();
							}
							done = false;
						}else{
							gr.setVal(x - 1, y - 1, gr.getVal(x, y));
							dM[ x - 1 ][ y - 1 ] = curDist + 1;

						}

						if( dM[ x - 1 ][ y + 1 ] != -1 ){
							if( gr.getVal(x - 1, y - 1) != gr.getVal(x, y) ){
								m_overwrite[ x - 1 ][ y ]++;
								gr.repaint();
							}
							done = false;
						}else{
							gr.setVal(x - 1, y + 1, gr.getVal(x, y));
							dM[ x - 1 ][ y + 1 ] = curDist + 1;
						}

						if( dM[ x + 1 ][ y - 1 ] != -1 ){
							if( gr.getVal(x + 1, y - 1) != gr.getVal(x, y) ){
								m_overwrite[ x + 1 ][ y ]++;
							}
							done = false;
						}else{

							gr.setVal(x + 1, y - 1, gr.getVal(x, y));
							dM[ x + 1 ][ y - 1 ] = curDist + 1;

						}

						if( dM[ x + 1 ][ y + 1 ] != -1 ){
							if( gr.getVal(x + 1, y + 1) != gr.getVal(x, y) ){
								m_overwrite[ x + 1 ][ y + 1 ]++;
								gr.repaint();
							}
							done = false;
						}else{
							gr.setVal(x + 1, y + 1, gr.getVal(x, y));
							dM[ x + 1 ][ y + 1 ] = curDist + 1;
						}
						gr.repaint();

					}
				}
			}
			curDist++;
		}


		//Add the vornoi points to the vPoints storage class.
		for( int y = 1; y < m_yLength - 1; y++ ){
			for( int x = 1; x < m_xLength - 1; x++ ){
				if( m_overwrite[ x + 1 ][ y ] > 1 ){
					gr.setVal(x, y, Color.blue.getRGB());
					gr.repaint();
					m_vPoints.add(x, y);
				}else if( m_overwrite[ x + 1 ][ y + 1 ] > 1 ){
					gr.setVal(x, y, Color.blue.getRGB());
					gr.repaint();
					m_vPoints.add(x, y);
				}else if( m_overwrite[ x ][ y + 1 ] > 1 ){
					gr.setVal(x, y, Color.blue.getRGB());
					gr.repaint();
					m_vPoints.add(x, y);

				}else if( m_overwrite[ x ][ y ] > 3 ){
					gr.setVal(x, y, Color.blue.getRGB());
					gr.repaint();
					m_vPoints.add(x, y);

				}

			}
		}

		//Add neighbors to voronoi point to build the graph.
		for( final VPoint p: m_vPoints.getVPoints().values() ){
			addNeighbors(p);
		}
	}

	public void brushFire2() {
		int curDist = 1;
		boolean done = false;
		//	int max = curDist;
		while( !done ){
			done = true;
			for( int y = 1; y < m_yLength -1; y++ ){
				for( int x = 1; x < m_xLength -1; x++ ){

					if( dM[ x ][ y ] == curDist ){

						done = false;

						if( dM[ x ][ y - 1 ] == -1 ){
							dM[ x ][ y - 1 ] = curDist + 1;
							if( dM[ x ][ y ] != 1 )
								gr.setVal(x, y, colorVal(dM[ x ][ y ]));

						}
						if( dM[ x ][ y + 1 ] == -1 ){
							dM[ x ][ y + 1 ] = curDist + 1;
							if( dM[ x ][ y ] != 1 )
								gr.setVal(x, y, colorVal(dM[ x ][ y ]));

						}

						if( dM[ x - 1 ][ y ] == -1 ){
							dM[ x - 1 ][ y ] = curDist + 1;
							if( dM[ x ][ y ] != 1 )
								gr.setVal(x, y, colorVal(dM[ x ][ y ]));

						}

						if( dM[ x + 1 ][ y ] == -1 ){
							dM[ x + 1 ][ y ] = curDist + 1;
							if( dM[ x ][ y ] != 1 )
								gr.setVal(x, y, colorVal(dM[ x ][ y ]));

						}


					}
				}

			}
			System.out.println("Current Val: " + curDist);
			gr.repaint();
			curDist++;
		}
	}

	/**
	 * This will actually execute the dijkstra algorithm to find the shortest path from
	 * the start to the finish. This function does work, but dijkstra takes longer than
	 * anticipated due to the vonoroni not being generated correctly.
	 *
	 * @param startX - The x coordinate of the starting point
	 * @param startY - The y coordinate of the starting point
	 * @param endX	 - The x coordinate of the ending point
	 * @param endY	 - The y coordinate of the ending point
	 *
	 * @return 	An array list of waypoints to follow.
	 */
	public ArrayList<VPoint> createPath(final int startX, final int startY, final int endX,
			final int endY) {
		Graphics2D gc = gr.getMap().createGraphics();
		gc.setColor(Color.green);
		gc.fillOval(startX, startY, 15, 15);
		final Graphics2D gj = gr.getMap().createGraphics();
		gj.setColor(Color.red);
		gj.fillOval(endX, endY, 15, 15);
		gr.repaint();
		gr.repaint();
		VPoint p = dijkstra(connect(startX, startY, -1), connect(endX, endY, -1));
		final ArrayList<VPoint> path = new ArrayList<VPoint>();
		int count = 0;
		while( p.getPrevious() != null ){
			//only get every 10th point
			if( count++ % 10 == 0 ){
				path.add(p);
				gc = gr.getMap().createGraphics();
				gc.setColor(Color.red);
				gc.drawLine(p.getPrevious().getX(), p.getPrevious().getY(),
						p.getX(), p.getY());
				gr.repaint();
			}
			p = p.getPrevious();
		}

		return path;
	}

	/**
	 * Connect an arbitrary point to the voronoi path
	 *
	 * @param x - y coordinate
	 * @param y - y coordinate
	 * @param maxDist - Maximum distance away the line can be.
	 * @return The point object of the
	 */
	private VPoint connect(final int x, final int y, final int maxDist) {
		final VPoint start = new VPoint(x, y);
		final VPoint end = m_vPoints.findClosest(start, maxDist);
		if( end != null && validLine(start, end) ){
			m_vPoints.add(start);
			start.addNeighbor(end);
			end.addNeighbor(start);
			return start;
		}
		return null;
	}

	/**
	 * Determines if a line will intersect with an object by taking samples along the line.
	 * This function is not used.
	 *
	 * @param start Start point of the line
	 * @param end	Ending point of the line
	 * @return		True if the line is valid, false if it is not.
	 */
	public boolean validLine(final VPoint start, final VPoint end) {
		if( start.getPointDistance(end.getX(), end.getY()) < 2 )
			return true;
		final double step = start.getPointDistance(end.getX(), end.getY());
		final double mX = end.getX() - start.getX();
		final double xStep = mX / step;
		final double mY = end.getY() - start.getY();
		final double yStep = mY / step;
		double curX = start.getX();
		double curY = start.getY();

		for( int i = 0; i < step; i++ ){
			if( dM[ (int) Math.ceil(curX) ][ (int) Math.ceil(curY) ] == 1 ){
				return false;
			}
			curX += xStep;
			curY += yStep;
		}

		return true;
	}

	/**
	 * This function will color the edges of the map to help with the creation of the vornoi
	 * variations of this theme were tried repeatedly with everything from constant values, to
	 * flood filling shapes and walls, to this which colors every type of obstacle a different
	 * color.
	 */
	public void markEdges() {
		int instance = 2;
		for( int y = 1; y < m_yLength - 1; y++ ){
			for( int x = 1; x < m_xLength - 1; x++ ){
				if( dM[ x ][ y ] == 1 ){
					if( dM[ x ][ y + 1 ] == -1 ){
						gr.setVal(x, y + 1, Color.white.getRGB() + instance);
						dM[ x ][ y + 1 ] = 2;
					}
				}
			}
			instance++;
		}

		for( int y = 1; y < m_yLength - 1; y++ ){
			for( int x = 1; x < m_xLength - 1; x++ ){
				if( dM[ x ][ y ] == 1 ){
					if( dM[ x ][ y - 1 ] == -1 ){
						dM[ x ][ y - 1 ] = 2;
						gr.setVal(x, y - 1, Color.white.getRGB() + instance);
					}
				}
			}
			instance++;
		}

		for( int x = 1; x < m_xLength - 1; x++ ){
			for( int y = 1; y < m_yLength - 1; y++ ){
				if( dM[ x ][ y ] == 1 ){
					if( dM[ x + 1 ][ y ] == -1 ){
						dM[ x + 1 ][ y ] = 2;
						gr.setVal(x + 1, y, Color.white.getRGB() + instance);
					}
				}

			}
			instance++;
		}

		for( int x = 1; x < m_xLength - 1; x++ ){
			for( int y = 1; y < m_yLength - 1; y++ ){
				if( dM[ x ][ y ] == 1 ){
					if( dM[ x - 1 ][ y ] == -1 ){
						dM[ x - 1 ][ y ] = 2;
						gr.setVal(x - 1, y, Color.white.getRGB() + instance);
					}
				}

			}
			instance++;
		}
		System.out.println("instance: " + instance);
		gr.repaint();
	}

	/**
	 * This function would attempt to complete an incomplete voronoi diagram. It is no longer
	 * used.
	 */
	private void createGraph() {
		int count = 0;
		for( int y = 0; y < m_yLength; y++ ){
			for( int x = 0; x < m_xLength; x++ ){
				if( gr.getVal(x, y) == Color.white.getRGB() ){
					if( count == 0 ){
						if( dM[ x ][ y ] > 4 ){
							m_vPoints.add(x, y);
							count++;
						}
					}else{
						if( dM[ x ][ y ] > 4 ){
							final VPoint m = connect(x, y, -1);
							if( m != null )
								addNeighbors(m);
							m_vPoints.add(x, y);
						}
						count++;
					}
				}
			}
		}
	}


	/**
	 * Finds all of the points adjacent to the starting point p.
	 * @param p The input point
	 */
	private void addNeighbors(final VPoint p) {

		//N
		p.addNeighbor(m_vPoints.get(p.getX(), p.getY() + 1));

		//S
		p.addNeighbor(m_vPoints.get(p.getX(), p.getY() - 1));

		//E
		p.addNeighbor(m_vPoints.get(p.getX() + 1, p.getY()));

		//W
		p.addNeighbor(m_vPoints.get(p.getX() - 1, p.getY()));

		//NW
		p.addNeighbor(m_vPoints.get(p.getX() - 1, p.getY() + 1));

		//NE
		p.addNeighbor(m_vPoints.get(p.getX() + 1, p.getY() + 1));

		//SW
		p.addNeighbor(m_vPoints.get(p.getX() - 1, p.getY() - 1));

		//SE
		p.addNeighbor(m_vPoints.get(p.getX() + 1, p.getY() - 1));

	}

	/**
	 * A simple implementation of the dijkstra path finding algorithm. Due to nature of the graph
	 * it was supposed to be spare enough for dijkstra to be able to run rather quickly. However,
	 * the voronoi isn't being generated correctly so in a future revision the a* grid search
	 * will be adapted to this code.
	 *
	 * @param source The source point
	 * @param dest
	 * @return
	 */
	private VPoint dijkstra(final VPoint source, final VPoint dest) {

		m_vPoints.resetDDistance();

		System.out.println("Dijkstra");
		System.out.println(" vPoints size: " + m_vPoints.getSize());
		System.out.println("PSize: " + m_vPoints.getSize() + " vPoints size: "
				+ m_vPoints.getSize());

		int alt = 0;

		VPoint u = m_vPoints.get(source);
		System.out.println("U: " + u);
		u.setDDistance(0);
		while( m_vPoints.getSize() > 0 ){
			u = m_vPoints.getSmallest();
			if( u.getDDistance() == u.INFINITY ){
				System.out.println("Infinity break");
				break;
			}

			m_vPoints.remove(u);

			for( final VPoint neighbor: u.getNeighbors() ){
				alt = u.getDDistance()
						+ neighbor.getPointDistance(u.getX(), u.getY());
						if( alt < neighbor.getDDistance() ){
					neighbor.setDDistance(alt);
					neighbor.setPrevious(u);
				}
				if( neighbor.equals(dest) ){
					return neighbor;
				}
			}
		}
		return null;
	}

	/**
	 * Initialize the array of colors to create a heat map effect
	 * @param max The maximum value for the colormap array.
	 */
	private void initializeColors(final int max) {
		colorMap = new Color[max];
		for( int i = 0; i < max; i++ ){
			colorMap[ i ] = Color.getHSBColor((float) i / (float) max, 0.75f,
					1.0f);
		}
	}

	/**
	 * Convert the distance measurement to a heatmap rgb color.
	 *
	 * @param curDist The current value
	 * @return The rgb color code.
	 */
	private int colorVal( final int curDist ){
		return colorMap[ curDist ].getRGB();
	}

}
