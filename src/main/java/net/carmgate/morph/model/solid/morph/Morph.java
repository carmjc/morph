package net.carmgate.morph.model.solid.morph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Activable;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.impl.morph.Evolving;
import net.carmgate.morph.model.requirements.Requirement;
import net.carmgate.morph.model.solid.WorldSolid;
import net.carmgate.morph.model.solid.morph.impl.BasicMorph;
import net.carmgate.morph.model.solid.morph.impl.GunMorph;
import net.carmgate.morph.model.solid.morph.impl.MinerMorph;
import net.carmgate.morph.model.solid.morph.impl.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.impl.stem.StemMorph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

/**
 * The generic class for morphs.
 */
@MorphInfo
public abstract class Morph extends WorldSolid implements Activable {

	/**
	 * The list of evolution types.
	 */
	public static enum EvolutionType {
		TO_BASIC(BasicMorph.class, 100),
		TO_GUN(GunMorph.class, 30),
		TO_MINER(MinerMorph.class, 30),
		TO_PROPULSOR(PropulsorMorph.class, 10),
		TO_STEM(StemMorph.class, 100);

		private final Class<? extends Morph> morphType;
		private final int durationInSeconds;

		private EvolutionType(Class<? extends Morph> type, int durationInSeconds) {
			morphType = type;
			this.durationInSeconds = durationInSeconds;
		}

		public int getDurationInSeconds() {
			return durationInSeconds;
		}

		public Class<? extends Morph> getMorphType() {
			return morphType;
		}
	}

	private static final int ENERGY_FLOW_ANALYSIS_PERIOD = 3000;

	private static final Logger LOGGER = Logger.getLogger(Morph.class);

	/** These behaviors are always active. */
	private final List<Behavior<?>> alwaysActiveBehaviorList = new ArrayList<Behavior<?>>();
	private final List<Requirement> activationRequirements = new ArrayList<Requirement>();

	/** These behaviors are active when the morph is active. */
	private final List<Behavior<?>> activationLinkedBehaviorList = new ArrayList<Behavior<?>>();

	/** This behaviors won't be (de)activated with Morph.tryTo(De)Active(). */
	private final List<Behavior<?>> activationIsolatedBehaviorList = new ArrayList<Behavior<?>>();

	private State state;

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
	/** the energy stored by the morph at previous cycle. */
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
	 * The default implementation of this method checks the morph's activation requirements.
	 */
	@Override
	public boolean canBeActivated() {

		// if the morph is evolving, it cannot be activated
		for (Behavior<?> behavior : getActivationIsolatedBehaviorList()) {
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

		if (getEnergy() <= 0) {
			return false;
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

	/**
	 * Two morphs are considered equal if :
	 * <ul><li>they are at the same position in the ship</li>
	 * <li>they are in the same ship</li>
	 * <li>they are of the same type (in terms of result of {@link Object#getClass()}</li></ul>
	 */
	@Override
	public boolean equals(Object obj) {

		// Extra caution must be taken when modifying this method.
		// A lot of processes count on it
		// If this method is changed, hashCode() should be changed as well.

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

	public final List<Behavior<?>> getActivationIsolatedBehaviorList() {
		return activationIsolatedBehaviorList;
	}

	public final List<Behavior<?>> getActivationLinkedBehaviorList() {
		return activationLinkedBehaviorList;
	}

	@Override
	public final List<Requirement> getActivationRequirements() {
		return activationRequirements;
	}

	public final List<Behavior<?>> getAlwaysActiveBehaviorList() {
		return alwaysActiveBehaviorList;
	}

	/**
	 * Iterates over the three behavior lists
	 * (activationLinked, activationIsolated and alwaysActive in that order) and returns
	 * the first behavior whose class is the same or a subclass of provided class.
	 * <p><b>Caution</b>: if there is several match, it only returns the first one.</p>
	 * @param clazz requested behavior type.
	 * @return the first matching behavior or null if there is no match.
	 */
	public final <T extends Behavior<?>> T getBehavior(Class<T> clazz) {
		for (Behavior<?> b : getActivationLinkedBehaviorList()) {
			if (clazz.isAssignableFrom(b.getClass())) {
				return (T) b;
			}
		}

		for (Behavior<?> b : getActivationIsolatedBehaviorList()) {
			if (clazz.isAssignableFrom(b.getClass())) {
				return (T) b;
			}
		}

		for (Behavior<?> b : getAlwaysActiveBehaviorList()) {
			if (clazz.isAssignableFrom(b.getClass())) {
				return (T) b;
			}
		}

		return null;
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
		return ship.findNeighbors(this);
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

	@Override
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

	public final void setEnergy(float energy) {
		this.energy = energy;
	}

	public void setEnergyDiffused(float energyDiffused) {
		this.energyDiffused = energyDiffused;
	}

	public final void setMass(float mass) {
		this.mass = mass;
		// TODO we should cap the mass to 0 on the bottom side
	}

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
		return "morph(id:" + getId() + ", posInShip:" + getPosInShip().x + "," + getPosInShip().y + ", mass:" + mass + ")";
	}

	/**
	 * Activate a morph.
	 * Also activate activation linked behaviors of the morph.
	 * <p><b>Caution:</b> What it does besides changing the {@link State} is not specified by this contract.
	 * It is the specific morph's responsibility to document what is done upon activation.</p>
	 * <p><b>Caution:</b> If the behaviors activation fails, there is nothing to 
	 * notify the user besides proper error logging.</p>
	 * @return TODO
	 */
	@Override
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

		if (activate()) {
			state = State.ACTIVE;

			// activate activable behaviors
			for (Behavior<?> b : getActivationLinkedBehaviorList()) {
				// TODO what happen if this fails
				if (b.tryToActivate() == State.INACTIVE) {
					state = State.INACTIVE;
					LOGGER.error("Failed to activate: " + b.getClass() + " for " + getClass());
				}
			}

		} else {
			// Activation failed, the morph stays inactive
			state = State.INACTIVE;
			LOGGER.trace("Failed to activate: " + getClass());
		}

		LOGGER.trace(getClass() + " successfully activated");
		return state;
	}

	/**
	 * De-activate a morph.
	 * Also attempts to deactivate activation linked behaviors.
	 * <p><b>Caution</b>: What it does besides changing the {@link State} is not specified by this contract.
	 * It is the specific morph's responsibility to document what is done upon deactivation.</p>
	 * <p><b>Caution:</b> If the behaviors deactivation fails, there is nothing to 
	 * notify the user besides proper error logging.</p>
	 * @return TODO
	 */
	@Override
	public final State tryToDeactivate() {
		return tryToDeactivate(false);
	}

	/**
	 * De-activate a morph.
	 * Also force deactivation of activation linked behaviors.
	 * <p><b>Caution</b>: What it does besides changing the {@link State} is not specified by this contract.
	 * It is the specific morph responsability to document what is done upon activation.
	 * Deactivation might be forced for instance if the morph is disabled.</p>
	 * <p><b>Caution:</b> If forced is set to true and the morph deactivation fails, 
	 * or if the behaviors deactivation fails, there is nothing to 
	 * notify the user besides proper error logging.</p>
	 * @param forced set to true to force deactivation
	 * @return TODO
	 */
	@Override
	public final State tryToDeactivate(boolean forced) {
		if (state == State.INACTIVE) {
			// already inactive
			return state;
		}

		// Initialize the expected state
		// This will be overridden by the following code if necessary
		state = State.INACTIVE;

		// if deactivation fails and wasn't forced, we declare the morph as being still active
		if (!deactivate() && !forced) {
			state = State.ACTIVE;
			LOGGER.error("Deactivation failed for " + getClass());
		} else {
			// deactivation succeded or if forced was set to true
			state = State.INACTIVE;

			for (Behavior<?> b : getActivationLinkedBehaviorList()) {
				// TODO what happen if this fails
				if (b.tryToDeactivate(forced) == State.ACTIVE) {
					// Behavior deactivation failed
					if (!forced) {
						state = State.ACTIVE;
					}
					LOGGER.error("Deactivation failed for " + b.getClass() + " in " + getClass());
				}
			}
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

		for (Behavior<?> behavior : getAlwaysActiveBehaviorList()) {
			behavior.tryToExecute();
		}

		// then execute the behavior specific to the morph that might be active/inactive
		for (Behavior<?> behavior : getActivationLinkedBehaviorList()) {
			behavior.tryToExecute();
		}

		// finally try to execute the alternate behaviors
		// these behaviors do not depend on the activation state of their
		// owning morph
		for (Behavior<?> behavior : getActivationIsolatedBehaviorList()) {
			behavior.tryToExecute(true);
		}

		updateMeanEnergyFlow();
	}

	/**
	 * This method is responsible for updating the mean energy flow.
	 * <p>It tries to calculate a mean value of the energy flow over 1000 cycles.
	 * It is used to show a numerical value of the energy flow.</p>
	 * <p><b>FIXME</b> Since we are only showing it for one morph, it should be reworked
	 * This logic should be put outside of the morph in a helper class calculating
	 * floating mean values. Besides, it does not seem to work very well.</p>
	 */
	private void updateMeanEnergyFlow() {
		// compute the energy flow since last update
		// when debugging this method, check that this value does not vary rapidly as it shouldn't
		// We could also crop a part of the decimals to reduce instant precision
		// but improve mean stability.
		double flow = ((double) energy - (double) oldEnergy) * 1000 / World.getWorld().getSinceLastUpdateTS();

		int newIndex = 0;
		// if we are still in the same analysis period, increment the energyFlowIndex
		if (World.getWorld().getCurrentTS() - energyFlowLastReset < 1000) {
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
