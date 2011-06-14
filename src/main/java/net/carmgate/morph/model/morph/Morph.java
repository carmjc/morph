package net.carmgate.morph.model.morph;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.ship.Ship;

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

	/** These behaviors are always active. */
	public List<Behavior<?>> alwaysActiveSpecificBehaviorList = new ArrayList<Behavior<?>>();
	/** These behaviors are activated when the morph is active. */
	public List<Behavior<?>> activableSpecificBehaviorList = new ArrayList<Behavior<?>>();

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
	public Vect3D pos;

	/** The morph position in the ship hex grid. */
	public Vect3D shipGridPos;

	/** The morph orientation in the ship referential. */
	public Vect3D rot;

	/** The container ship. */
	// It is needed by the renderer.
	public Ship ship;

	/** the energy stored by the morph. */
	public float energy;

	/**
	 * Initializes morph position
	 * @param x
	 * @param y
	 * @param z
	 */
	public Morph(float x, float y, float z) {
		shipGridPos = new Vect3D(x, y, z);
		pos = new Vect3D(
				shipGridPos.x * World.GRID_SIZE + shipGridPos.y * World.GRID_SIZE / 2,
				(float) (shipGridPos.y * World.GRID_SIZE * Math.sqrt(3)/2),
				0);
		rot = new Vect3D(0, 0, 0);
		energy = getMaxEnergy();
	}

	public abstract void activate();

	public abstract void deactivate();

	/** the maximum energy that this kind of morph can store. */
	public abstract float getMaxEnergy();

	/**
	 * TODO This is suboptimal. We should not calculate the neighbours each time we need them.
	 * @return
	 */
	public List<Morph> getNeighbours() {
		return ship.getNeighbours(this);
	}

	/**
	 * @return this morph's type.
	 */
	public abstract MorphType getType();

	public void update() {
		// first execute behavior that are specific to the morph and always active
		for (Behavior<?> behavior : alwaysActiveSpecificBehaviorList) {
			behavior.tryToExecute();
		}

		// then execute the behavior specific to the morph that might be inactive
		for (Behavior<?> behavior : activableSpecificBehaviorList) {
			behavior.tryToExecute();
		}

		// then execute the behaviors that have been temporarily added to the morph
		for (Behavior<?> behavior : activableSpecificBehaviorList) {
			behavior.tryToExecute();
		}
	}

}
