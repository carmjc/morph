package net.carmgate.morph.model.morph;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

/**
 * The generic class for morphs.
 */
public abstract class Morph {

	/**
	 * The list of morph types.
	 */
	public static enum MorphType {
		BASIC,
		EMITTER,
		PROPULSOR,
		SHIELD,
		SPREADER;
	}

	private static final Logger logger = Logger.getLogger(Morph.class);

	/** These behaviors are always active. */
	public List<Behavior<?>> alwaysActiveBehaviorList = new ArrayList<Behavior<?>>();
	/** These behaviors are active when the morph is active. */
	public List<Behavior<?>> activableBehaviorList = new ArrayList<Behavior<?>>();

	/** Morph mass. */
	public float mass = 1;

	/** Morph max mass. */
	public float maxMass = 1;

	/** Morph minimum mass before being enable to fulfill its mission. */
	public float disableMass = 0;

	public float reenableMass = 1;
	/** if true, the morph is disabled. */
	public boolean disabled = false;

	/** The morph position in the world referential. */
	private Vect3D posInShip = new Vect3D(0, 0, 0);

	/** The morph position in the world referential. */
	private Vect3D posInWorld = new Vect3D(0, 0, 0);

	/** The morph position in the ship hex grid. */
	public Vect3D shipGridPos;

	/** The morph orientation in the ship referential. */
	private float rotInShip;

	/** The morph orientation in the ship referential. */
	private float rotInWorld;

	/** The container ship. */
	// It is needed by the renderer.
	private Ship ship;

	/** the energy stored by the morph. */
	public float energy;

	/** The timestamp of last time the morph was updated. */
	public long lastUpdateTS;

	// Activity
	public long activeMsec;
	public boolean active = false;
	/**
	 * Initializes morph position
	 * @param x
	 * @param y
	 * @param z
	 */
	public Morph(Ship ship, float x, float y, float z) {
		shipGridPos = new Vect3D(x, y, z);

		// Position and rotation in ship and world
		this.ship = ship;
		setPosInShip(new Vect3D(
				shipGridPos.x * World.GRID_SIZE + shipGridPos.y * World.GRID_SIZE / 2,
				(float) (shipGridPos.y * World.GRID_SIZE * Math.sqrt(3)/2),
				0));
		setRotInShip(0);

		// energy
		energy = getMaxEnergy();
	}
	public abstract boolean activable();

	public final void activate() {

		beforeActivate();
		if (activable()) {
			// Activate
			active = true;
			activeMsec = 0;

			afterActivate();
		}

	}

	public abstract void afterActivate();

	public abstract void afterDeactivate();

	public abstract void beforeActivate();

	public abstract void beforeDeactivate();

	public abstract boolean deactivable();

	public final void deactivate() {

		beforeDeactivate();
		if (deactivable()) {
			// Activate
			active = false;

			afterDeactivate();
		}

	}
	/** the maximum energy that this kind of morph can store. */
	public abstract float getMaxEnergy();

	/**
	 * TODO This is suboptimal. We should not calculate the neighbours each time we need them.
	 * @return null if the morph takes part in no ship.
	 */
	public List<Morph> getNeighbours() {
		if (ship == null) {
			return null;
		}
		return ship.getNeighbours(this);
	}

	public Vect3D getPosInShip() {
		if (getShip() == null) {
			return null;
		}
		return posInShip;
	}

	/**
	 * @return morph pos in world
	 */
	public Vect3D getPosInWorld() {
		if (ship != null) {
			posInWorld.copy(posInShip);
			ship.transformShipToWorldCoords(posInWorld);
		}
		return posInWorld;
	}

	public float getRotInShip() {
		return rotInShip;
	}

	public float getRotInWorld() {
		if (ship != null) {
			rotInWorld = ship.rot + rotInShip;
		}
		return rotInWorld;
	}

	public Ship getShip() {
		return ship;
	}

	/**
	 * @return this morph's type.
	 */
	public abstract MorphType getType();

	// TODO Unit test
	public void setPosInShip(Vect3D posInShip) {
		this.posInShip.copy(posInShip);
		if (getShip() != null) {
			posInWorld.copy(posInShip);
			posInWorld.rotate(getShip().rot);
			posInWorld.add(getShip().pos);
		}
	}

	// TODO Unit test
	public void setPosInWorld(Vect3D posInWorld) {
		this.posInWorld.copy(posInWorld);
		if (getShip() != null) {
			posInShip.copy(posInWorld);
			posInShip.substract(getShip().pos);
			posInShip.rotate(-getShip().rot);

		}
	}

	public void setRotInShip(float rotInShip) {
		this.rotInShip = rotInShip;
		if (ship != null) {
			rotInWorld = ship.rot + rotInShip;
		}
	}

	public void setRotInWorld(float rotInWorld) {
		this.rotInWorld = rotInWorld;
		if (ship != null) {
			rotInShip = rotInWorld - ship.rot;
		}
	}

	/**
	 * Assign morph to a ship and resets orientation in ship
	 * @param ship
	 */
	public void setShip(Ship ship) {
		this.ship = ship;

		// Reset orientation
		rotInShip = 0;
		rotInWorld = ship.rot;
	}

	/**
	 * Executes behaviors of the morph
	 */
	public final void update() {

		for (Behavior<?> behavior : alwaysActiveBehaviorList) {
			behavior.tryToExecute();
		}

		// then execute the behavior specific to the morph that might be active/inactive
		for (Behavior<?> behavior : activableBehaviorList) {
			behavior.tryToExecute();
		}

		//		// then execute the behaviors that have been temporarily added to the morph
		//		for (Behavior<?> behavior : temporaryBehaviorList) {
		//			behavior.tryToExecute();
		//		}

		// Update last update msec
		lastUpdateTS = World.getWorld().getCurrentTS();
	}

}
