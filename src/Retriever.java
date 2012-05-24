/**
 * 
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javaclient2.PlayerClient;
import javaclient2.Position2DInterface;
import javaclient2.SonarInterface;
import javaclient2.structures.PlayerConstants;
import localization.Localizer;
import localization.Mapper;
import navigation.PotentialField;
import planning.Planner;
import ui.MapViewer;

import common.GridMap;
import common.Point;
import common.Position;
import common.Utils;


/**
 * Retriver thread
 * @author ziyan
 *
 */
public class Retriever implements Runnable {

	private final PlayerClient robot;
	private final SonarInterface sonar;
	private final Position2DInterface p2d;
	private final PotentialField apf;
	private final Planner planner;
	private final Mapper mapper;
	private final GridMap map, floorplan, cspace;
	private final Localizer localizer;
	private final MapViewer mapviewer, planviewer;
	private boolean running = true;

	/**
	 * Retriever Thread
	 * @param hostname hostname of the player server
	 * @param port port of the player server
	 * @param waypoints waypoints that need to be reached
	 * @param floorplan floorplan file
	 * @throws IOException
	 */
	public Retriever(final String hostname, final int port, final Point[] waypoints, final String floorplan) throws IOException {
		this.robot = new PlayerClient(hostname, port);
		this.p2d = robot.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		this.sonar = robot.requestInterfaceSonar(0, PlayerConstants.PLAYER_OPEN_MODE);
		this.apf = new PotentialField(this.p2d);

		this.robot.readAll();
		this.sonar.setSonarPower(1);
		this.p2d.setMotorPower(1);

		this.floorplan = GridMap.loadFromRaw(1600, 500, 0.082, floorplan);
		this.cspace = this.floorplan.getCSpace();
		this.map = new GridMap(5248/2, 5248/2, 0.05);
		this.mapper = new Mapper(this.map);
		this.localizer = new Localizer(this.floorplan, this.map);

		this.planner = new Planner(waypoints, cspace);

		this.mapviewer = new MapViewer(this.map, true);
		this.mapviewer.setSize(500,500);
		this.mapviewer.setVisible(true);

		this.planviewer = new MapViewer(this.cspace, true);
		this.planviewer.setSize(500,500);
		this.planviewer.setVisible(true);

		System.out.println("Retriever: initialized");
	}

	/**
	 * Gracefully shutdown
	 */
	public void shutdown() {
		running = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		double x, y, yaw, angle, length, rx, ry, ryaw;
		float[] ranges;
		Position candidate;
		while(running && !planner.isDone()) {
			robot.readAll();
			if (!sonar.isDataReady()) continue;
			ranges = sonar.getData().getRanges();
			if (sonar.getData().getRanges().length <= 0) continue;

			x = p2d.getData().getPos().getPx();
			y = p2d.getData().getPos().getPy();
			yaw = p2d.getData().getPos().getPa();

			// convert coordinates
			candidate = localizer.getCandidate();
			angle = Math.atan2(y, x) + candidate.getYaw();
			length = Utils.elength(x, y);
			rx = Math.cos(angle) * length + candidate.getX();
			ry = Math.sin(angle) * length + candidate.getY();
			ryaw = Utils.normalize(yaw +  candidate.getYaw());
			cspace.setX(rx); cspace.setY(ry); cspace.setYaw(ryaw);
			//System.out.println("Retriever: real coordinates ("+ rx + ", "+ry+", "+ryaw+")");

			planner.update(rx, ry);
			apf.update(ranges, rx, ry, ryaw, planner.getNext());
			mapper.update(ranges, x, y, yaw);
		}
		running = false;

		mapviewer.shutdown();
		planviewer.shutdown();
		apf.shutdown();
		localizer.shutdown();
		mapper.shutdown();
		planner.shutdown();
	}

	public static Point[] load(final String filename) throws IOException {
		final Scanner scanner = new Scanner(new File(filename));
		final List<Point> points = new ArrayList<Point>();
		while(scanner.hasNext()) {
			final String line = scanner.nextLine();
			if(line.length() <= 0) continue;
			final String[] parts = line.split("[\\s]+");
			if(parts.length != 2) continue; 
			final double x = Double.parseDouble(parts[0]);
			final double y = Double.parseDouble(parts[1]);
			points.add(new Point(x, y));
		}
		System.out.println("Retriever: " + points.size() + " points loaded.");
		return points.toArray(new Point[]{});
	}

	/**
	 * Main entry of the program
	 * @param args command line arguments
	 */
	public static void main(final String[] args) throws IOException {
		// check arguments
		if(args.length < 3) {
			System.err.println("Usage: java Retriever <hostname> <port> <point-file> [floorplan]");
			return;
		}

		final Retriever retriever = new Retriever(args[0], Integer.parseInt(args[1]), load(args[2]), args.length > 3 ? args[3] : "3large.raw");
		// start a thread
		new Thread(retriever).start();

		// stop when you hit enter on the commandline
		System.in.read();
		retriever.shutdown();
	}

}
