package net.carmgate.morph.model.physic.morph;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.physic.ship.Ship;
import net.carmgate.morph.model.physic.world.World;
import net.carmgate.morph.model.requirements.Requirement;

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
	private final List<Behavior<?>> alwaysActiveBehaviorList = new ArrayList<Behavior<?>>();
	private final List<Requirement> activationRequirements = new ArrayList<Requirement>();

	/** These behaviors are active when the morph is active. */
	private final List<Behavior<?>> activableBehaviorList = new ArrayList<Behavior<?>>();

	/** This behaviors won't be (de)activated with Morph.tryTo(De)Active(). */
	private final List<Behavior<?>> alternateBehaviorList = new ArrayList<Behavior<?>>();

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

	/**
	 * Called to activate the morph and its behaviors.
	 * Warning: if this method returns false (meaning the activation failed), 
	 * it is the responsability of the method implementation to assure the consistency of the morph
	 * (a partial activation could pose problems).
	 * @return TODO
	 */
	protected abstract boolean activate();

	/**
	 * The default implementation of this method checks the morphs activation requirements.
	 * Override with caution.
	 * Overriding method should always call the inherited class method to check as well.
	 * @return true if the morph can be activated
	 */
	public boolean canBeActivated() {
		boolean canBeActivated = true;
		for (Requirement req : getActivationRequirements()) {
			if (!req.check()) {
				canBeActivated = false;
			}
		}
		return canBeActivated;
	}

	/**
	 * Called to activate the morph and its behaviors.
	 * Warning: if this method returns false (meaning the deactivation failed), 
	 * it is the responsability of the method implementation to assure the consistency of the morph
	 * (a partial deactivation could pose problems).
	 * @return TODO
	 */
	protected abstract boolean deactivate();

	// /**
	// * Disables a morph.
	// * This automatically forces the morph's deactivation.
	// */
	// public final void disable() {
	// disabled = true;
	// tryToDeactivate(true);
	// }
	//
	// /**
	// * Enables the morph
	// */
	// public final void enable() {
	// disabled = false;
	// }
	//
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

	public final List<Behavior<?>> getActivableBehaviorList() {
		return activableBehaviorList;
	}

	public final List<Requirement> getActivationRequirements() {
		return activationRequirements;
	}

	public List<Behavior<?>> getAlternateBehaviorList() {
		return alternateBehaviorList;
	}

	public final List<Behavior<?>> getAlwaysActiveBehaviorList() {
		return alwaysActiveBehaviorList;
	}

	public final float getEnergy() {
		return energy;
	}

	public final int getId() {
		return id;
	}

	public final float getMass() {
		return mass;
	}

	/**
	 * TODO This is suboptimal. We should not calculate the neighbours each time we need them.
	 * @return null if the morph takes part in no ship.
	 */
	public final List<Morph> getNeighbours() {
		if (ship == null) {
			return null;
		}
		return ship.getNeighbours(this);
	}

	public final Vect3D getPosInShip() {
		if (getShip() == null) {
			return null;
		}
		return posInShip;
	}

	/**
	 * @return morph pos in world
	 */
	public final Vect3D getPosInWorld() {
		if (ship != null) {
			posInWorld.copy(posInShip);
			ship.transformShipToWorldCoords(posInWorld);
		}
		return posInWorld;
	}

	public final float getRotInShip() {
		return rotInShip;
	}

	public final float getRotInWorld() {
		if (ship != null) {
			rotInWorld = ship.getRot() + rotInShip;
		}
		return rotInWorld;
	}

	public final Ship getShip() {
		return ship;
	}

	public final Vect3D getShipGridPos() {
		return shipGridPos;
	}

	public final State getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return (int) (100 + (long) shipGridPos.x * 500 + (long) shipGridPos.y * 500 + (long) shipGridPos.z * 500 + ship.hashCode()) * 100
				+ getClass().hashCode();
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

		// Iterate over requirements to check that the morph can be activated
		// At this moment, state is necessarily INACTIVE
		// If at least one of the requirements is not satisfied, it return INACTIVE
		if (!canBeActivated()) {
			return state;
		}

		// Initialize the expected state
		// This will be overridden by the following code if necessary
		state = State.ACTIVE;

		// can not activate if energy insufficient
		if (getEnergy() <= 0) {
			LOGGER.debug("no more energy");
			// disable();
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
	public final State tryToDeactivate(boolean forced) {
		if (state == State.INACTIVE) {
			// already inactive
			return state;
		}

		// Initialize the expected state
		// This will be overridden by the following code if necessary
		state = State.INACTIVE;

		for (Behavior<?> b : getActivableBehaviorList()) {
			// TODO what happen if this fails
			if (b.tryToDeactivate(forced) == State.ACTIVE) {
				// Behavior deactivation failed
				if (!forced) {
					state = State.ACTIVE;
				}
			}
		}

		// if deactivation fails and wasn't forced, we declare the morph as being still active
		if (!deactivate() && !forced) {
			state = State.ACTIVE;
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

		// See if this morph needs to be deactivated
		if (getMass() < getClass().getAnnotation(MorphInfo.class).disableMass()) {
			LOGGER.trace("Disabling morph");
			tryToDeactivate(true);
		}

		for (Behavior<?> behavior : alwaysActiveBehaviorList) {
			behavior.tryToExecute();
		}

		// then execute the behavior specific to the morph that might be active/inactive
		for (Behavior<?> behavior : activableBehaviorList) {
			behavior.tryToExecute();
		}

		// finally try to execute the alternate behaviors
		// these behaviors do not depend on the activation state of their
		// owning morph
		for (Behavior<?> behavior : alternateBehaviorList) {
			behavior.tryToExecute(true);
		}

	}

	public void updatePosFromGridPos() {
		setPosInShip(new Vect3D(
				shipGridPos.x * World.GRID_SIZE + shipGridPos.y * World.GRID_SIZE / 2,
				(float) (shipGridPos.y * World.GRID_SIZE * Math.sqrt(3) / 2),
				0));
	}
}