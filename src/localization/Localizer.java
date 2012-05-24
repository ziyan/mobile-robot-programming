/**
 * 
 */
package localization;

import java.util.Arrays;

import common.GridMap;
import common.Position;
import common.Utils;

/**
 * Localizer thread
 * @author ziyan
 *
 */
public class Localizer extends Thread {

	private final GridMap map;
	private final GridMap floorplan;
	private final Position[] candidates;
	private boolean done;

	/**
	 * Localizer thread
	 * @param floorplan floorplan (workspace)
	 * @param map local map generated from mapper
	 */
	public Localizer(final GridMap floorplan, final GridMap map) {
		this.done = false;
		this.floorplan = floorplan;
		this.map = map;
		this.candidates = new Position[] {
				new Position("p0", -15.5, 12.0, 0.0),
				new Position("p1", -16.5, 12.0, Utils.dtor(-180.0)),
				new Position("p2", -5.0, -10.5, 0.0),
				new Position("p3", 7.5, 1.0, Utils.dtor(90.0)),
				new Position("p4", -48.0, 12.0, Utils.dtor(90.0)),
				new Position("p5", -48.0, -10.5, Utils.dtor(-90.0)),
				new Position("p6", 7.5, -5.0, Utils.dtor(90.0)),
				new Position("p7", 0.0, -7.0, Utils.dtor(-90.0))
		};
		this.setPriority(1);
		this.start();
	}

	/**
	 * get a list of candidates in order
	 * from most likely to most unlikely
	 * @return
	 */
	public Position[] getCandidates() {
		return candidates;
	}

	/**
	 * get most likely start location
	 * @return
	 */
	public Position getCandidate() {
		return candidates[0];
	}

	/**
	 * Shutdown gracefully
	 */
	public void shutdown() {
		this.done = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while(!done) {
				/*if(!map.isSufficient()) {
					Thread.sleep(100);
					continue;
				}*/
				for(int i = 0; i < candidates.length; i++)
					candidates[i].setWeight(GridMap.diff(floorplan, map, candidates[i]));
				Arrays.sort(candidates);
				/*
				System.out.println();
				System.out.println("Sorted position candidates:");
				System.out.println("===========================");
				for(int i = 0; i < candidates.length; i++)
					System.out.println(candidates[i]);
				System.out.println();
				 */
				System.out.println("Localizer: most likely = " + candidates[0]);
				if(candidates.length <= 1) break; // probably never going to happen

				Thread.sleep(100);
			}
		} catch(final InterruptedException e) { }

		//  output localization result
		if(candidates.length > 0) {
			System.out.println();
			System.out.println("Robot start location:");
			System.out.println("=====================");
			System.out.println(candidates[0]);
		}
	}

}
