package net.carmgate.morph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.ship.Ship;
import net.carmgate.morph.model.ship.TestShip;

public class World {
	public static final int GRID_SIZE = 64; // Main.SCALE_FACTOR);

	public static World worldInstance;

	public static World getWorld() {
		if (worldInstance == null) {
			worldInstance = new World();
		}
		return worldInstance;
	}

	/** World areas. */
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
		TestShip ship = new TestShip(200, 100, 0);
		shipList.add(ship);
		ship = new TestShip(500, 200, 0);
		ship.rot.z = 60;
		shipList.add(ship);
	}

	public void setSelectedShip(int index) {
		if (index >= 0 && index < shipList.size()) {
			this.selectedShip = shipList.get(index);
		} else {
			this.selectedShip = null;
		}
	}

	public void update() {
		for (Ship ship : shipList) {
			ship.update();
		}
	}
}
