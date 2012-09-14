package net.carmgate.morph.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.model.ship.TestShip;

/**
 * The world has a list of world areas, a ships list, a forces list and a currently selected ship (for now).
 * @author Carm
 */
public class World {
	public static final int GRID_SIZE = 64; // Main.SCALE_FACTOR);

	/** World singleton instance. */
	public static World worldInstance;

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

	/** the list of all ships in game. */
	private final List<Ship> shipList = new ArrayList<Ship>();

	/** A list of forces to show. */
	private final List<Force> forceList = new ArrayList<Force>();

	//	private int selectedShipId = -1;
	private Ship selectedShip;

	private World() {
		// privatized constructor
	}

	/**
	 * @return number of millis since game start.
	 */
	public long getCurrentTS() {
		return msec;
	}

	public List<Force> getForceList() {
		return forceList;
	}

	public Ship getSelectedShip() {
		return selectedShip;
	}

	public List<Ship> getShipList() {
		return shipList;
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
		TestShip ship = new TestShip(0, 0, 0);
		shipList.add(ship);
	}

	public void setSelectedShip(int index) {
		if (index >= 0 && index < shipList.size()) {
			selectedShip = shipList.get(index);
		} else {
			selectedShip = null;
		}
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
		for (Ship ship : shipList) {
			ship.update();
		}
	}
}
