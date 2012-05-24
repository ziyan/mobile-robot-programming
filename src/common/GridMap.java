/**
 * 
 */

package common;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 * GridMap (using byte as storage)
 * @author ziyan
 *
 */
public class GridMap {
	/**
	 * Minimum area that should be covered before we localize
	 */
	public static final int DIFF_MIN_AREA = 1000;

	/**
	 * Used in scoring, basically more area is better than less area
	 */
	public static final int DIFF_ADEQUATE_AREA = 2000;

	/**
	 * Lower limit of the map data represeting traversable ground
	 * value is -100
	 */
	public static final byte TRAVERSABLE = -100;

	/**
	 * Upper limit of the map data representing intraversable ground
	 * value is 100
	 */
	public static final byte INTRAVERSABLE = 100;

	private final int width, height;
	private final byte[][] data;
	private final double mpp;
	private int top, left, bottom, right;
	private double x, y, yaw;
	private Point[] path;

	/**
	 * GridMap class
	 * @param width pixel width
	 * @param height pixel height
	 * @param mpp resolution (meter per pixel)
	 */
	public GridMap(final int width, final int height, final double mpp) {
		this.width = width;
		this.height = height;
		this.data = new byte[height][width];
		this.mpp = mpp;
		this.top = height / 2;
		this.left = width / 2;
		this.bottom = height / 2;
		this.right = width / 2;
	}

	/**
	 * Width in pixel
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Height in pixel
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Stored path (to be painted on the viewer)
	 * @return
	 */
	public Point[] getPath() {
		return path;
	}

	/**
	 * Store a path to be painted on the GUI
	 * @param path
	 */
	public void setPath(final Point[] path) {
		this.path = path;
	}

	/**
	 * Robot locaation X
	 * @return
	 */
	public double getX() {
		return x;
	}

	/**
	 * Robot location Y
	 * @return
	 */
	public double getY() {
		return y;
	}

	/**
	 * Robot location Yaw
	 * @return
	 */
	public double getYaw() {
		return yaw;
	}

	/**
	 * Set robot location X
	 * @param x
	 */
	public void setX(final double x) {
		this.x = x;
	}

	/**
	 * Set robot location Y
	 * @param y
	 */
	public void setY(final double y) {
		this.y = y;
	}

	/**
	 * Set robot location Yaw
	 * @param yaw
	 */
	public void setYaw(final double yaw) {
		this.yaw = yaw;
	}

	/**
	 * Get data value at map coordinate
	 * @param r
	 * @param c
	 * @return
	 */
	public byte getData(final int r, final int c) {
		return data[r][c];
	}

	/**
	 * Convert player coordinate to map coordinate
	 * @param y
	 * @return
	 */
	public int getRow(final double y) {
		return height - ((int)(y / mpp) + height / 2);
	}

	/**
	 * Convert player coordinate to map coordinate
	 * @param x
	 * @return
	 */
	public int getCol(final double x) {
		return (int)(x / mpp) + width / 2;
	}

	/**
	 * Set data value at map coordinate
	 * @param r
	 * @param c
	 * @param d
	 */
	public void setData(final int r, final int c, byte d) {
		if(d > INTRAVERSABLE) d = INTRAVERSABLE;
		if(d < TRAVERSABLE) d = TRAVERSABLE;

		// keep track of a boundary
		if(r < top) top = r;
		if(r > bottom) bottom = r;
		if(c < left) left = c;
		if(c > right) right = c;

		data[r][c] = d;
	}

	/**
	 * Resolution (meter per pixel)
	 * @return
	 */
	public double getMPP() {
		return mpp;
	}

	/**
	 * Is it sufficient mapping to do localization?
	 * @return
	 */
	public boolean isSufficient() {
		return (right - left) * (bottom - top) > DIFF_MIN_AREA;
	}

	/**
	 * Bounding box
	 * @return
	 */
	public int getTop() {
		return top;
	}

	/**
	 * Bounding box
	 * @return
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * Bounding box
	 * @return
	 */
	public int getBottom() {
		return bottom;
	}

	/**
	 * Bounding box
	 * @return
	 */
	public int getRight() {
		return right;
	}

	/**
	 * Generate C-space grid map
	 * @return
	 */
	public GridMap getCSpace() {
		// clone the map
		final GridMap cspace = new GridMap(width, height, mpp);
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				cspace.data[r][c] = (data[r][c] == TRAVERSABLE) ? TRAVERSABLE : INTRAVERSABLE;
			}
		}

		// distance matrix
		final int buffer = (int)Math.ceil(Pioneer.RADIUS / mpp);
		int level = INTRAVERSABLE;
		while(level > INTRAVERSABLE - buffer) {
			for(int r = 1; r < height - 1; r++) {
				for(int c = 1; c < width - 1; c++) {
					if(cspace.data[r][c] == level) {
						if(cspace.data[r][c - 1] == TRAVERSABLE)
							cspace.data[r][c - 1] = (byte)(level - 1);
						if(cspace.data[r][c + 1] == TRAVERSABLE)
							cspace.data[r][c + 1] = (byte)(level - 1);
						if(cspace.data[r - 1][c] == TRAVERSABLE)
							cspace.data[r - 1][c] = (byte)(level - 1);
						if(cspace.data[r + 1][c] == TRAVERSABLE)
							cspace.data[r + 1][c] = (byte)(level - 1);
					}
				}
			}
			level --;
		}

		// c-space generation
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				cspace.data[r][c] = (cspace.data[r][c] == TRAVERSABLE) ? TRAVERSABLE : INTRAVERSABLE;
			}
		}

		// distance matrix to provide cost for A*
		level = INTRAVERSABLE;
		boolean done = false;
		while(!done) {
			done = true;
			for(int r = 1; r < height - 1; r++) {
				for(int c = 1; c < width - 1; c++) {
					if(cspace.data[r][c] == level) {
						done = false;
						if(cspace.data[r][c - 1] == TRAVERSABLE)
							cspace.data[r][c - 1] = (byte)(level - 1);
						if(cspace.data[r][c + 1] == TRAVERSABLE)
							cspace.data[r][c + 1] = (byte)(level - 1);
						if(cspace.data[r - 1][c] == TRAVERSABLE)
							cspace.data[r - 1][c] = (byte)(level - 1);
						if(cspace.data[r + 1][c] == TRAVERSABLE)
							cspace.data[r + 1][c] = (byte)(level - 1);
					}
				}
			}
			level --;
		}

		// max bounding box
		cspace.top = 0;
		cspace.left = 0;
		cspace.right = cspace.width - 1;
		cspace.bottom = cspace.height - 1;
		return cspace;
	}


	/**
	 * Load grid map from a raw file
	 * @param width
	 * @param height
	 * @param mpp
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static GridMap loadFromRaw(final int width, final int height, final double mpp, final String filename) throws IOException {
		final FileInputStream in = new FileInputStream(filename);
		final GridMap map = new GridMap(width, height, mpp);
		final byte[] buffer = new byte[102400];
		int r = 0, c = 0, read, i;
		while(r < height) {
			read = in.read(buffer, 0, buffer.length);
			if(read < 0) throw new IOException();
			for(i = 0; i < read; i++) {
				map.data[r][c] = buffer[i] == 0 ? TRAVERSABLE : INTRAVERSABLE;
				c++;
				if(c >= width) {
					c = 0;
					r++;
				}
			}
		}
		return map;
	}

	/**
	 * Save grid map to a PPM
	 * @param map
	 * @param filename
	 * @throws IOException
	 */
	public static void saveAsPPM(final GridMap map, final String filename) throws IOException {
		final PrintStream out = new PrintStream(new FileOutputStream(filename));
		out.println("P2");
		out.println("# Created by the awesome MRP project");
		out.println("" + map.width + " " + map.height);
		out.println("255");
		for(int r = 0; r < map.height; r++)
			for(int c = 0; c < map.width; c++)
				out.print("" + (128 - map.data[r][c]) + " ");
		out.println();
		out.flush();
		out.close();
	}

	/**
	 * Score the local map (used by localizer)
	 * @param floorplan
	 * @param map
	 * @param position
	 * @return
	 */
	public static double diff(final GridMap floorplan, final GridMap map, final Position position) {
		if(!map.isSufficient()) return 0.0;
		int count = 0;
		int diff = 0;
		double x, y, a, t;
		int fr, fc;
		for(int r = map.top; r <= map.bottom; r++) {
			for(int c = map.left; c <= map.right; c++) {
				if(map.data[r][c] == 0) continue;
				x = map.mpp * (c - map.width / 2);
				y = map.mpp * ((map.height - r) - map.height / 2);
				a = Utils.normalize(Math.atan2(y, x) + position.getYaw());
				t = Utils.elength(x, y);
				fc = (int)((position.getX() + t * Math.cos(a)) / floorplan.mpp) + floorplan.width / 2;
				fr = floorplan.height - ((int)((position.getY() + t * Math.sin(a)) / floorplan.mpp) + floorplan.height / 2);
				if(fc < 0 || fr < 0 || fc >= floorplan.width || fr >= floorplan.height) continue;
				count++;
				diff += Math.abs(map.data[r][c] - floorplan.data[fr][fc]);
			}
		}
		if(count < DIFF_MIN_AREA) return 0.0;
		return (1.0 - (double)diff / ((double)count * (double)(INTRAVERSABLE - TRAVERSABLE))) * Math.exp(-(double)DIFF_ADEQUATE_AREA / (double)count);
	}
}
