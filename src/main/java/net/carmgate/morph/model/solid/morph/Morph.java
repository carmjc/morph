package net.carmgate.morph.model.solid.morph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Evolving;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.requirements.Requirement;
import net.carmgate.morph.model.solid.morph.prop.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.stem.StemMorph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

/**
 * The generic class for morphs.
 */
@MorphInfo
public abstract class Morph {

	/**
	 * The list of evolution types.
	 */
	public static enum EvolutionType {
		TO_STEM(MorphType.STEM, 100),
		TO_PROPULSOR(MorphType.PROPULSOR, 10),
		TO_GUN(MorphType.GUN, 30),
		TO_BASIC(MorphType.BASIC, 100);

		private final MorphType morphType;
		private final int durationInSeconds;

		private EvolutionType(MorphType type, int durationInSeconds) {
			morphType = type;
			this.durationInSeconds = durationInSeconds;
		}

		public int getDurationInSeconds() {
			return durationInSeconds;
		}

		public MorphType getMorphType() {
			return morphType;
		}
	}

	/**
	 * The list of morph types.
	 */
	public static enum MorphType {
		BASIC(BasicMorph.class),
		GUN(GunMorph.class),
		PROPULSOR(PropulsorMorph.class),
		SHIELD(null),
		SPREADER(null),
		STEM(StemMorph.class),
		SHADOW(null);

		private final Class<? extends Morph> morphClass;

		/**
		 * @param morphClass the morph class matching the morph type.
		 * This param is only used for real morph types.
		 */
		private MorphType(Class<? extends Morph> morphClass) {
			this.morphClass = morphClass;
		}

		/**
		 * @return the morph class matching the morph type.
		 * This param is only used for real morph types.
		 */
		public Class<? extends Morph> getMorphClass() {
			return morphClass;
		}
	}

	private static final int ENERGY_FLOW_ANALYSIS_PERIOD = 3000;

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

	/** The morph position in the world referential. */
	private Vect3D posInShip = new Vect3D(0, 0, 0);

	/** The morph position in the world referential. */
	private Vect3D posInWorld = new Vect3D(0, 0, 0);
	/** The morph position in the ship hex grid. */
	private Vect3D posInShipGrid;

	/** The morph orientation in the ship referential. */
	private float rotInShip;

	/** The morph orientation in the ship referential. */
	private float rotInWorld;

	/** The container ship. */
	// It is needed by the renderer.
	private Ship ship;

	/** the energy stored by the morph. */
	private float energy;
	private float oldEnergy;

	private float energyDiffused;

	private boolean selectable = true;

	private double energyFlow = 0;
	private double energyFlowLastReset = World.getWorld().getCurrentTS() / ENERGY_FLOW_ANALYSIS_PERIOD;
	private double energyFlowTab[] = new double[1000];
	private int energyFlowIndex = 0;

	/**
	 * Initializes morph position
	 * @param x
	 * @param y
	 * @param z
	 */
	public Morph() {
		// update last id and affect it
		id = ++lastId;

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

		// if the morph is evolving, it cannot be activated
		for (Behavior<?> behavior : getAlternateBehaviorList()) {
			if (behavior instanceof Evolving) {
				return false;
			}
		}

		// if it does not meet requirements, it cannot be activated
		for (Requirement req : getActivationRequirements()) {
			if (!req.check()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Called to activate the morph and its behaviors.
	 * Warning: if this method returns false (meaning the deactivation failed), 
	 * it is the responsability of the method implementation to assure the consistency of the morph
	 * (a partial deactivation could pose problems).
	 * @return TODO
	 */
	protected abstract boolean deactivate();

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		Morph morph = (Morph) obj;
		return posInShipGrid.x == morph.posInShipGrid.x &&
				posInShipGrid.y == morph.posInShipGrid.y &&
				posInShipGrid.z == morph.posInShipGrid.z &&
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

	public float getEnergyDiffused() {
		return energyDiffused;
	}

	public double getEnergyFlow() {
		return energyFlow;
	}

	public float getExcessEnergy() {
		int maxEnergy = getClass().getAnnotation(MorphInfo.class).maxEnergy();
		if (energy > maxEnergy) {
			return energy - maxEnergy;
		}
		return 0;
	}

	public final int getId() {
		return id;
	}

	public final float getMass() {
		return mass;
	}

	/**
	 * Return max energy deducted from the MorphInfo annotation.
	 * @return
	 */
	public float getMaxEnergy() {
		return getClass().getAnnotation(MorphInfo.class).maxEnergy();
	}

	/**
	 * Return max mass deducted from the MorphInfo annotation.
	 * @return
	 */
	public final float getMaxMass() {
		return getClass().getAnnotation(MorphInfo.class).maxMass();
	}

	/**
	 * TODO This is suboptimal. We should not calculate the neighbors each time we need them.
	 * @return null if the morph takes part in no ship.
	 */
	public final List<Morph> getNeighbors() {
		if (ship == null) {
			return null;
		}
		return ship.getNeighbors(this);
	}

	public final Vect3D getPosInShip() {
		if (getShip() == null) {
			return null;
		}
		return posInShip;
	}

	public final Vect3D getPosInShipGrid() {
		return posInShipGrid;
	}

	/**
	 * @return morph pos in world
	 */
	public final Vect3D getPosInWorld() {
		if (ship != null) {
			// This should be computed only once per frame
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

	public final State getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return (int) (100 + (long) posInShipGrid.x * 500 + (long) posInShipGrid.y * 500 + (long) posInShipGrid.z * 500 + ship.hashCode()) * 100
				+ getClass().hashCode();
	}

	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * Warning: this method caps the energy to the maximum energy that can be put in the morph.
	 * When there is an excess of energy, it's added to the excess energy storage in order to be re-emitted
	 * if the morph can emit enough.
	 * @param energy
	 */
	public final void setEnergy(float energy) {
		// Update the energy
		this.energy = energy;
	}

	public void setEnergyDiffused(float energyDiffused) {
		this.energyDiffused = energyDiffused;
	}

	public final void setMass(float mass) {
		float oldMass = this.mass;
		this.mass = mass;
		// TODO we must cap the mass to 0 on the bottom side
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

	public void setPosInShipGrid(Vect3D posInShipGrid) {
		this.posInShipGrid = posInShipGrid;
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

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
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
		// TODO replace with usage of requirements.
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

		updateEnergyFlow();
	}

	private void updateEnergyFlow() {
		// compute the energy flow since last update
		double flow = (energy - oldEnergy) * 1000 / World.getWorld().getSinceLastUpdateTS();

		int newIndex = 0;
		// if we are still in the same analysis period, increment the energyFlowIndex
		if (World.getWorld().getCurrentTS() - energyFlowLastReset < 3000) {
			newIndex = energyFlowIndex + 1;
		} else {
			energyFlowLastReset = World.getWorld().getCurrentTS();
		}

		// Compute the mean energy flow
		energyFlow = energyFlow - energyFlowTab[energyFlowIndex] + flow;
		energyFlowTab[newIndex] = flow;
		energyFlowIndex = newIndex;

		LOGGER.trace(new DecimalFormat("0.0####").format(energyFlow) + " - " + oldEnergy + "->" + energy + " in "
				+ World.getWorld().getSinceLastUpdateTS() + "ms (index=" + newIndex + ") " + energyFlowLastReset + "->"
				+ World.getWorld().getCurrentTS());

		oldEnergy = energy;
	}

	public void updatePosFromGridPos() {
		setPosInShip(new Vect3D(
				posInShipGrid.x * World.GRID_SIZE + posInShipGrid.y * World.GRID_SIZE / 2,
				(float) (posInShipGrid.y * World.GRID_SIZE * Math.sqrt(3) / 2),
				0));
	}
}
