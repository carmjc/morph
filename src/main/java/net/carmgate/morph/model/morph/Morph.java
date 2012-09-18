package net.carmgate.morph.model.morph;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

/**
 * The generic class for morphs.
 */
@MorphInfo
public abstract class Morph {

	/**
	 * The list of morph types.
	 */
	public static enum MorphType {
		BASIC,
		EMITTER,
		PROPULSOR,
		SHIELD,
		SPREADER,
		STEM_MORPH,
		SHADOW;
	}

	private static final Logger LOGGER = Logger.getLogger(Morph.class);

	/** These behaviors are always active. */
	private List<Behavior<?>> alwaysActiveBehaviorList = new ArrayList<Behavior<?>>();

	/** These behaviors are active when the morph is active. */
	private final List<Behavior<?>> activableBehaviorList = new ArrayList<Behavior<?>>();

	/** The last ID assigned to a morph. */
	static private int lastId = 0;

	private State state;

	/** Each and every morph must have a single ID. */
	private int id;

	/** Morph mass. */
	private float mass = 0.5f;

	/** if true, the morph is disabled. */
	private boolean disabled = false;

	/** The morph position in the world referential. */
	private Vect3D posInShip = new Vect3D(0, 0, 0);
	/** The morph position in the world referential. */
	private Vect3D posInWorld = new Vect3D(0, 0, 0);

	/** The morph position in the ship hex grid. */
	private Vect3D shipGridPos;

	/** The morph orientation in the ship referential. */
	private float rotInShip;

	/** The morph orientation in the ship referential. */
	private float rotInWorld;

	/** The container ship. */
	// It is needed by the renderer.
	private Ship ship;

	/** the energy stored by the morph. */
	private float energy;

	/**
	 * Initializes morph position
	 * @param x
	 * @param y
	 * @param z
	 */
	public Morph(Ship ship, float x, float y, float z) {
		// update last id and affect it
		id = ++lastId;

		// set position in ship
		shipGridPos = new Vect3D(x, y, z);

		// Position and rotation in ship and world
		this.ship = ship;
		updatePosFromGridPos();
		setRotInShip(0);

		// energy
		energy = getClass().getAnnotation(MorphInfo.class).maxEnergy();

		// set initialMass
		setMass(getClass().getAnnotation(MorphInfo.class).initialMass());
		LOGGER.trace(getClass() + " initial mass: " + getMass());
	}

	/** The timestamp of last time the morph was updated. */
	// private long lastUpdateTS; // It seems it's not used

	// private boolean active = false; // It seems it's not used

	/**
	 * Returns true if the morph can be activated.
	 * @return
	 */
	protected boolean activable() {
		boolean activable = true;

		// can not activate if energy insufficient
		if (getEnergy() <= 0) {
			LOGGER.debug("no more energy");
			disable();
			activable = false;
		}

		return activable;
	}

	/**
	 * Called to activate the morph and its behaviors.
	 * @return TODO
	 */
	protected abstract boolean activate();

	/**
	 * Returns true if the morph can be deactivated.
	 * @return
	 */
	protected boolean deactivable() {
		return true;
	}

	/**
	 * Called to activate the morph and its behaviors.
	 * @return TODO
	 */
	protected abstract boolean deactivate();

	/**
	 * Disables a morph.
	 * This automatically forces the morph's deactivation.
	 */
	public final void disable() {
		disabled = true;
		tryToDeactivate(true);
	}

	/**
	 * Enables the morph
	 */
	public final void enable() {
		disabled = false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		Morph morph = (Morph) obj;
		return shipGridPos.x == morph.shipGridPos.x &&
				shipGridPos.y == morph.shipGridPos.y &&
				shipGridPos.z == morph.shipGridPos.z &&
				getShip() == morph.getShip() &&
				getClass().getName().equals(obj.getClass().getName());
	}

	public List<Behavior<?>> getActivableBehaviorList() {
		return activableBehaviorList;
	}

	public List<Behavior<?>> getAlwaysActiveBehaviorList() {
		return alwaysActiveBehaviorList;
	}

	public float getEnergy() {
		return energy;
	}

	public int getId() {
		return id;
	}

	public float getMass() {
		return mass;
	}

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
			rotInWorld = ship.getRot() + rotInShip;
		}
		return rotInWorld;
	}

	public Ship getShip() {
		return ship;
	}

	public Vect3D getShipGridPos() {
		return shipGridPos;
	}

	public State getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return (int) (100 + (long) shipGridPos.x * 500 + (long) shipGridPos.y * 500 + (long) shipGridPos.z * 500 + ship.hashCode()) * 100
				+ getClass().hashCode();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	// TODO Unit test
	public void setPosInShip(Vect3D posInShip) {
		this.posInShip.copy(posInShip);
		if (getShip() != null) {
			posInWorld.copy(posInShip);
			posInWorld.rotate(getShip().getRot());
			posInWorld.add(getShip().getPos());
		}
	}

	// TODO Unit test
	public void setPosInWorld(Vect3D posInWorld) {
		this.posInWorld.copy(posInWorld);
		if (getShip() != null) {
			posInShip.copy(posInWorld);
			posInShip.substract(getShip().getPos());
			posInShip.rotate(-getShip().getRot());

		}
	}

	public void setRotInShip(float rotInShip) {
		this.rotInShip = rotInShip;
		if (ship != null) {
			rotInWorld = ship.getRot() + rotInShip;
		}
	}

	public void setRotInWorld(float rotInWorld) {
		this.rotInWorld = rotInWorld;
		if (ship != null) {
			rotInShip = rotInWorld - ship.getRot();
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
		rotInWorld = ship.getRot();
	}

	@Override
	public String toString() {
		return "morph(id=" + id + ", posInShip:" + getPosInShip().x + "," + getPosInShip().y + ")";
	}

	/**
	 * Activate a morph.
	 * Also activate activable behaviors of the morph.
	 * Make careful use of this method.
	 * What it really does is not specified by this contract.
	 * It is the specific morph responsability to document what is done upon activation.
	 * @return TODO
	 */
	public final State tryToActivate() {
		LOGGER.trace("Trying to activate " + getClass());

		if (state == State.ACTIVE) {
			// already inactive
			return state;
		}

		// Initialize the expected state
		// This will be overridden by the following code if necessary
		state = State.ACTIVE;

		// can not activate if energy insufficient
		if (getEnergy() <= 0) {
			LOGGER.debug("no more energy");
			disable();
		} else {
			// activate activable behaviors
			for (Behavior<?> b : getActivableBehaviorList()) {
				// TODO what happen if this fails
				if (b.tryToActivate() == State.INACTIVE) {
					state = State.INACTIVE;
					LOGGER.trace("Failed to activate: " + b.getClass());
				}
			}

			if (!activate()) {
				state = State.INACTIVE;
				LOGGER.trace("Failed to activate: " + getClass());
			}
		}

		// Add to the active morph list of the owning ship
		if (state == State.ACTIVE) {
			getShip().getActiveMorphList().add(this);
		}
		LOGGER.trace(getClass() + " successfully activated");
		return state;
	}

	/**
	 * De-activate a morph.
	 * Make careful use of this method.
	 * What it really does is not specified by this contract.
	 * It is the specific morph responsability to document what is done upon activation.
	 * @return TODO
	 */
	public final State tryToDeactivate() {
		return tryToDeactivate(false);
	}

	/**
	 * De-activate a morph.
	 * Make careful use of this method.
	 * What it really does is not specified by this contract.
	 * It is the specific morph responsability to document what is done upon activation.
	 * Deactivation might be forced for instance if the morph is disabled.
	 * @param forced set to true to force deactivation
	 * @return TODO
	 */
	private final State tryToDeactivate(boolean forced) {
		if (state == State.INACTIVE) {
			// already inactive
			return state;
		}

		// Initialize the expected state
		// This will be overridden by the following code if necessary
		state = State.INACTIVE;

		if (!forced || deactivable()) {
			for (Behavior<?> b : getActivableBehaviorList()) {
				// TODO what happen if this fails
				if (b.tryToDeactivate(forced) == State.ACTIVE) {
					// Behavior deactivation failed
					if (!forced) {
						state = State.ACTIVE;
					}
				}
			}

			if (!deactivate() && !forced) {
				state = State.ACTIVE;
			}
		}

		// Add to the active morph list of the owning ship
		if (state == State.INACTIVE) {
			getShip().getActiveMorphList().remove(this);
		}
		LOGGER.trace(getClass() + " state: " + state);
		return state;
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

		// // then execute the behaviors that have been temporarily added to the morph
		// for (Behavior<?> behavior : temporaryBehaviorList) {
		// behavior.tryToExecute();
		// }

		// Update last update msec
		// lastUpdateTS = World.getWorld().getCurrentTS();
	}

	public void updatePosFromGridPos() {
		setPosInShip(new Vect3D(
				shipGridPos.x * World.GRID_SIZE + shipGridPos.y * World.GRID_SIZE / 2,
				(float) (shipGridPos.y * World.GRID_SIZE * Math.sqrt(3) / 2),
				0));
	}
}
