/**
 * 
 */

package planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import common.GridMap;
import common.Point;
import common.Utils;

/**
 * A* algorithm
 * @author ziyan
 *
 */
public class AStar {
	private final GridMap cspace;

	/**
	 * A* algorithm
	 * @param cspace cspace grid map
	 */
	public AStar(final GridMap cspace) {
		this.cspace = cspace;
	}

	/**
	 * Compute optimal path
	 * @param start start point
	 * @param end end point
	 * @return
	 */
	public Point[] compute(final Point start, final Point end) {
		final Node init = new Node(start);
		final Node goal = new Node(end);
		if(!init.isValid() || !goal.isValid()) return null;
		init.h = Utils.elength(init.x - goal.x, init.y - goal.y);

		final PriorityQueue<Node> queue = new PriorityQueue<Node>();
		final Map<Node, Double> seen = new HashMap<Node, Double>();
		seen.put(init, init.c + init.h);
		queue.add(init);
		Node head;
		while(true) {
			head = queue.poll();
			if(head == null) return null; // no path
			if(head.equals(goal)) break; // found goal
			for(final Node child : head.getSuccessors(goal)) {
				final Double previous = seen.get(child);
				if(previous == null || previous > child.c + child.h) {
					queue.add(child);
					seen.put(child, child.c + child.h);
				}
			}
		}

		final Point[] points = new Point[head.level + 1];
		while(head != null) {
			points[head.level] = head.getPoint();
			head = head.parent;
		}
		return points;
	}

	/**
	 * Search node
	 * @author ziyan
	 *
	 */
	private class Node implements Comparable<Node> {
		public int x, y;
		public double h, c;
		public Node parent;
		public int level;

		/**
		 * Node constructor for start and end point
		 * @param p
		 */
		public Node(final Point p) {
			this.x = (int)(p.x / cspace.getMPP()) + cspace.getWidth() / 2;
			this.y = cspace.getHeight() - ((int)(p.y / cspace.getMPP()) + cspace.getHeight() / 2);
			this.h = 0;
			this.c  = 0;
			this.parent = null;
			this.level = 0;
		}

		/**
		 * Node constructor for successor function
		 * @param parent
		 * @param x
		 * @param y
		 * @param c
		 * @param goal
		 */
		private Node(final Node parent, final int x, final int y, final double c, final Node goal) {
			this.x = parent.x + x;
			this.y = parent.y + y;

			this.parent = parent;
			this.level = parent.level + 1;
			if(isValid()) {
				// potential field to avoid sticking to the wall
				double p = (double)(GridMap.INTRAVERSABLE - (int)cspace.getData(this.y, this.x)) / (double)(GridMap.INTRAVERSABLE - GridMap.TRAVERSABLE);
				p = 1.0 / p;
				this.c = parent.c + p * c;
				this.h = Utils.elength(this.x - goal.x, this.y - goal.y);
			}
		}

		/**
		 * Check if point is outside of grid or part of an obstacle
		 * @return
		 */
		public boolean isValid() {
			if(x < 0 || x >= cspace.getWidth()) return false;
			if(y < 0 || y >= cspace.getHeight()) return false;
			return cspace.getData(y, x) != GridMap.INTRAVERSABLE;
		}

		/**
		 * Get successor of the current point
		 * @param goal goal point used to calculate heurist
		 * @return
		 */
		public Node[] getSuccessors(final Node goal) {
			final List<Node> nodes = new ArrayList<Node>();
			final Node top = new Node(this, 0, -1, 1, goal);
			final Node bottom = new Node(this, 0, 1, 1, goal);
			final Node left = new Node(this, -1, 0, 1, goal);
			final Node right = new Node(this, 1, 0, 1, goal);
			final Node topLeft = new Node(this, -1, -1, 1.5, goal);
			final Node topRight = new Node(this, -1, 1, 1.5, goal);
			final Node bottomLeft = new Node(this, 1, -1, 1.5, goal);
			final Node bottomRight = new Node(this, 1, 1, 1.5, goal);
			if(top.isValid()) nodes.add(top);
			if(bottom.isValid()) nodes.add(bottom);
			if(left.isValid()) nodes.add(left);
			if(right.isValid()) nodes.add(right);
			if(topLeft.isValid()) nodes.add(topLeft);
			if(topRight.isValid()) nodes.add(topRight);
			if(bottomLeft.isValid()) nodes.add(bottomLeft);
			if(bottomRight.isValid()) nodes.add(bottomRight);
			return nodes.toArray(new Node[]{});
		}

		/**
		 * Convert to player coordinates
		 * @return
		 */
		public Point getPoint() {
			return new Point(cspace.getMPP() * (x - cspace.getWidth() / 2),
					cspace.getMPP() * ((cspace.getHeight() - y) - cspace.getHeight() / 2));
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			return obj instanceof Node &&
			((Node)obj).x == this.x &&
			((Node)obj).y == this.y;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.y * cspace.getWidth() + this.x;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(final Node o) {
			if(h + c < o.h + o.c)
				return -1;
			return 1;
		}

	}
}
