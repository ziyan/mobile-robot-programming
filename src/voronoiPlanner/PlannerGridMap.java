package voronoiPlanner;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A simple map for displaying data.
 *
 * @author Kevin Cheek
 *
 * @author zjb 3/09
 */
public class PlannerGridMap extends JFrame {
	private static final long serialVersionUID = -7536213683002408224L;
	private BufferedImage theMap;
	private int imwidth, imheight;


	/**
	 * Initializes the image.
	 *
	 * @param width The width of the raw image.
	 * @param height The heigh of the raw image.
	 */
	public PlannerGridMap(int width, int height) {
		this.setVisible(true);
		imwidth = width;
		imheight = height;
		theMap = new BufferedImage(imwidth, imheight, BufferedImage.TYPE_INT_ARGB);
		int midgray = (0xff << 24) | (180 << 16) | (180 << 8) | (180);
		for( int x = 0; x < imwidth; x++ )
			for( int y = 0; y < imheight; y++ )
				theMap.setRGB(x, y, midgray);

		MapPanel mp = new MapPanel();
		mp.setVisible(true);
		add(mp);
		this.setSize(imwidth, imheight);
	}


	/**
	 * Return a copy of the buffered image (Map).
	 *
	 * @return A reference to the image.
	 */
	public BufferedImage getMap() {
		return theMap;
	}


	/**
	 * Set the rgb color of the cell.
	 *
	 * @param x - X coordinate
	 * @param y - Y coordinate
	 * @param rgb - The color code
	 */
	void setVal(int x, int y, int rgb) {
		theMap.setRGB(x, y, rgb);
	}

	/**
	 * Get the rgb color at this point
	 *
	 * @param x - X coordinate
	 * @param y  - Y coordinate
	 * @return The RGB value
	 */
	int getVal(int x, int y) {

		return theMap.getRGB(x, y);
	}

	/**
	 * Redraw the image.
	 */
	void reDraw() {
		getGraphics().drawImage(theMap, 0, 0, null);
	}

	class MapPanel extends JPanel {
		/**
		 * Randomly Generated serialNumber since it extends a serializable
		 */
		private static final long serialVersionUID = 3524455568145773574L;

		protected void paintComponent(Graphics g) {
			g.drawImage(theMap, 0, 0, null);
		}

		public Dimension getPreferredSize() {
			return new Dimension(imwidth, imheight);
		}
	}
}
