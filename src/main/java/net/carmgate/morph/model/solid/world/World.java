package net.carmgate.morph.model.solid.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.energysource.Star;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.ship.TestShip;
import net.carmgate.morph.ui.selection.SelectionModel;

/**
 * The world has a list of world areas, a ships list, a forces list and a currently selected ship (for now).
 * @author Carm
 */
public class World {
	public static final int GRID_SIZE = 64; // Main.SCALE_FACTOR);

	/** World singleton instance. */
	private static World worldInstance;

	/** number of millis since game start. */
	private long msec = 0;
	/** timestamp of game start. */
	private final long gameStartMsec = new Date().getTime();

	public static boolean combat = false;
	public static boolean freeze = false;

	public static World getWorld() {
		if (worldInstance == null) {
			worldInstance = new World();
		}
		return worldInstance;
	}

	/**
	 * World areas.
	 * The coordinates of the world areas in this Map are not the world coordinates, but the coordinates of the world areas along the 3 axis.
	 * @See {@link WorldArea#toWorldAreaPos(Vect3D)}
	 */
	private final Map<Vect3D, WorldArea> worldAreas = new HashMap<Vect3D, WorldArea>();

	/** the list of all energy sources in the game. */
	private final Map<Integer, EnergySource> energySources = new HashMap<Integer, EnergySource>();

	/** the list of all ships in game. */
	private final Map<Integer, Ship> ships = new HashMap<Integer, Ship>();

	/** A list of forces to show. */
	private final List<Force> forceList = new ArrayList<Force>();

	/** The selection model. */
	private final SelectionModel selectionModel = new SelectionModel();

	private World() {
		// privatized constructor
	}

	/**
	 * @return number of millis since game start.
	 */
	public long getCurrentTS() {
		return msec;
	}

	public Map<Integer, EnergySource> getEnergySources() {
		return energySources;
	}

	public List<Force> getForceList() {
		return forceList;
	}

	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

	public Map<Integer, Ship> getShips() {
		return ships;
	}

	/**
	 * Get the world area for a given world position.
	 * @param pos in world coordinates
	 * @return the corresponding world area
	 */
	public WorldArea getWorldArea(Vect3D pos) {
		if (worldAreas.get(WorldArea.toWorldAreaPos(pos)) == null) {
			worldAreas.put(WorldArea.toWorldAreaPos(pos), new WorldArea(new Vect3D(pos)));
		}

		return worldAreas.get(WorldArea.toWorldAreaPos(pos));
	}

	public Map<Vect3D, WorldArea> getWorldAreas() {
		return worldAreas;
	}

	public void init() {
		// Create a star
		EnergySource star = new Star(900, -400, 0);
		getEnergySources().put(star.getId(), star);

		// Create a ship
		TestShip ship = new TestShip(0, 0, 0);
		getShips().put(ship.getId(), ship);
	}

	/**
	 * Update the world.
	 */
	public void update() {
		if (World.freeze) {
			return;
		}

		// update the number of millis since game start
		msec = new Date().getTime() - gameStartMsec;

		// update ships
		for (Ship ship : getShips().values()) {
			ship.update();
		}
	}
}
