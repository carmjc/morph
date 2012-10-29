package net.carmgate.morph.model.solid.morph.impl;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.impl.morph.prop.Propulsing;
import net.carmgate.morph.model.behavior.impl.morph.prop.PropulsorsLost;
import net.carmgate.morph.model.requirements.impl.EnoughEnergy;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

import org.apache.log4j.Logger;

/**
 * A propulsor morph transforms its potential energy in cinetic energy by unknown means (we don't care)
 * A propulsor morph does not loose mass by being activated.
 */
@MorphInfo(possibleEvolutions = { EvolutionType.TO_BASIC })
public class PropulsorMorph extends BasicMorph {

	private static final Logger LOGGER = Logger.getLogger(PropulsorMorph.class);

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph() {
		// Behaviors
		propulsingBehavior = new Propulsing(this, ModelConstants.ENERGY_CONSUMPTION_AT_FULL_THRUST, ModelConstants.PROPULSING_FORCE_MODULUS_AT_FULL_THRUST);
		getActivationLinkedBehaviorList().add(propulsingBehavior);
		getActivationRequirements().add(new EnoughEnergy(this, 1));
	}

	@Override
	public boolean deactivate() {
		LOGGER.trace("PropulsorMorph deactivated.");

		// If the propulsor has been stopped because of a lack of energy,
		// create a new behavior to slow down the ship
		if (getEnergy() < 1) {
			PropulsorsLost propsLostBehavior = new PropulsorsLost(this);
			// Add it to the ship
			getActivationIsolatedBehaviorList().add(propsLostBehavior);
		}

		return true;
	}

	public Propulsing getPropulsingBehavior() {
		return propulsingBehavior;
	}

}
