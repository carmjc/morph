package net.carmgate.morph.model.solid.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.energysource.Star;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.ship.TestShip;

import org.apache.log4j.Logger;

/**
 * The world has a list of world areas, a ships list, a forces list and a currently selected ship (for now).
 * @author Carm
 */
public class World {

	private static final Logger LOGGER = Logger.getLogger(World.class);

	/** the ratio between world x coordinates and ship grid x coordinates. **/
	public static final int GRID_SIZE = 64;

	/** World singleton instance. */
	private static World worldInstance;

	/** number of millis since game start. */
	private long currentTS = 0;
	/** number of millis since last update. */
	private long sinceLastUpdateTS = 0;

	/** timestamp of game start. */
	private long gameStartMsec = new Date().getTime();

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

	private World() {
		// privatized constructor
	}

	/**
	 * @return number of millis since game start.
	 */
	public long getCurrentTS() {
		return currentTS;
	}

	public Map<Integer, EnergySource> getEnergySources() {
		return energySources;
	}

	public List<Force> getForceList() {
		return forceList;
	}

	public Map<Integer, Ship> getShips() {
		return ships;
	}

	/**
	 * @return the number of millis since last update.
	 */
	public long getSinceLastUpdateTS() {
		return sinceLastUpdateTS;
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
		EnergySource star = new Star(900, -400, 0, 10f, 1000);
		getEnergySources().put(star.getId(), star);

		// Create a ship
		TestShip ship = new TestShip(0, 0, 0);
		getShips().put(ship.getId(), ship);
	}

	/**
	 * Energy sources will replenish the morphs of a ship if they are close enough.
	 * @param ship
	 */
	private void processGettingEnergyFromEnergySource(Ship ship) {
		for (EnergySource energySource : getEnergySources().values()) {
			float energyGainedPerMorph = 0;
			float distance = ship.getPos().distance(energySource.getPos());

			// A distance of 1000 pixels to the star causes the star to give no energy to the ship
			// The greater the ship, the more energy it gets
			// The greater the star, the more energy it emits
			double minimumActingDistanceSquared = energySource.getEffectRadius() * energySource.getEffectRadius();
			double distanceSquared = distance * distance;

			if (distanceSquared < minimumActingDistanceSquared) {
				energyGainedPerMorph += (minimumActingDistanceSquared - distanceSquared) / minimumActingDistanceSquared
						* energySource.getRadiatedEnergy()
						* sinceLastUpdateTS / 1000;

				for (Morph m : ship.getMorphsByIds().values()) {
					// if the morph is not virtual (selection shadows for instance)
					// then we update it's energy
					if (!m.getClass().getAnnotation(MorphInfo.class).virtual()) {
						// augment morph energy
						m.setEnergy(m.getEnergy() + energyGainedPerMorph * m.getMass() / m.getClass().getAnnotation(MorphInfo.class).maxMass());

						// if there is too much excess of energy, the morph will loose a portion of its mass
						// proportional to the amount of energy above sustainable amount of energy in excess
						float sustainableExcessEnergy = m.getClass().getAnnotation(MorphInfo.class).maxEnergy()
								* ModelConstants.MAX_EXCEED_ENERGY_AS_RATIO_OF_MAX_MORPH_ENERGY;
						if (m.getExcessEnergy() > sustainableExcessEnergy) {
							float energyAboveSustainable = m.getExcessEnergy() - sustainableExcessEnergy;
							m.setMass(m.getMass() - energyAboveSustainable * ModelConstants.MASS_LOSS_TO_EXCESS_ENERGY_RATIO);
							m.setEnergy(m.getEnergy() - energyAboveSustainable);
						}
					}
				}
			}
		}
	}

	/**
	 * Update the world.
	 */
	public void update() {
		// update the number of millis since game start
		long oldTS = currentTS;
		currentTS = new Date().getTime() - gameStartMsec;
		sinceLastUpdateTS = currentTS - oldTS;

		if (World.freeze) {
			gameStartMsec += sinceLastUpdateTS;
			currentTS -= sinceLastUpdateTS;
			return;
		}

		// update ships
		for (Ship ship : getShips().values()) {
			processGettingEnergyFromEnergySource(ship);
			ship.update();
		}

	}
}
