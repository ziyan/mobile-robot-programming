/**
 * 
 */
package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import common.GridMap;
import common.Point;


/**
 * Map viewer
 * @author ziyan
 *
 */
public class MapViewer extends JFrame {
	private static final long serialVersionUID = 1L;
	private final BufferedImage image;
	private final MapPanel mp;
	private final GridMap map;
	private boolean done = false;
	private MapUpdater updater = null;
	private final ScrollPane sp;

	/**
	 * Heat color map
	 */
	private static Color[] COLOR_MAP = new Color[256];
	static {
		for( int i = 0; i < 256; i++ ) {
			COLOR_MAP[i] = Color.getHSBColor((float) i / 256.0f,
					0.75f,
					1.0f);
		}
	}

	/**
	 * Map viewer GUI
	 * @param map map to be displayed
	 * @param update whether to constantly refresh the map or not
	 */
	public MapViewer(final GridMap map, final boolean update) {		
		this.map = map;
		this.image = new BufferedImage(map.getWidth(), map.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.mp = new MapPanel();
		this.sp = new ScrollPane();
		this.sp.add(this.mp);
		this.add(this.sp);
		this.update(0, map.getWidth() - 1, 0, map.getHeight() - 1);
		if(update)
			this.updater = new MapUpdater();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		done = true;
		if(updater != null) updater.interrupt();
		super.dispose();
	}

	/**
	 * gracefully shutdown
	 */
	public void shutdown() {
		this.dispose();
	}

	/**
	 * Update a bounding box of map onto the image
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public void update(final int left, final int right, final int top, final int bottom) {
		int value;
		for (int x = left; x <= right; x++)
			for (int y = top; y <= bottom; y++) {
				value = (int)map.getData(y, x) - GridMap.TRAVERSABLE;
				if(map.getData(y, x) == 0)
					image.setRGB(x, y, Color.GRAY.getRGB());
				else if(map.getData(y, x) == GridMap.TRAVERSABLE)
					image.setRGB(x, y, Color.WHITE.getRGB());
				else if(map.getData(y, x) == GridMap.INTRAVERSABLE)
					image.setRGB(x, y, Color.BLACK.getRGB());
				else
					image.setRGB(x, y, COLOR_MAP[value].getRGB());
			}
		final Point[] path = map.getPath();
		if(path != null)
			for(final Point p : path)
				image.setRGB(map.getCol(p.x), map.getRow(p.y), Color.WHITE.getRGB());
		final Graphics g = image.getGraphics();
		g.setColor(Color.RED);
		g.fillOval(map.getCol(map.getX())-3, map.getRow(map.getY())-3, 6, 6);
		mp.invalidate();
		mp.repaint();
		this.repaint();
	}

	/**
	 * Map pannel sits in a scroll pane
	 * @author ziyan
	 *
	 */
	private class MapPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		protected void paintComponent(final Graphics g) {
			g.drawImage(image,0,0,null);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		public Dimension getPreferredSize() {
			return new Dimension(image.getWidth(),image.getWidth());
		}
	}

	/**
	 * Updater thread
	 * @author ziyan
	 *
	 */
	private class MapUpdater extends Thread {
		/**
		 * Updater thread
		 */
		public MapUpdater() {
			this.setPriority(1);
			this.start();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while(!done) {
					update(map.getLeft(), map.getRight(), map.getTop(), map.getBottom());
					Thread.sleep(500);
				}
			} catch(final InterruptedException e) { }
		}
	}


}
