package net.carmgate.morph.model.solid.morph.prop;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.prop.Propulsing;
import net.carmgate.morph.model.behavior.prop.PropulsorsLost;
import net.carmgate.morph.model.requirements.EnoughEnergy;
import net.carmgate.morph.model.solid.morph.BasicMorph;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

import org.apache.log4j.Logger;

/**
 * A propulsor morph transforms its potential energy in cinetic energy by unknown means (we don't care)
 * A propulsor morph does not loose mass by being activated.
 */
@MorphInfo(type = MorphType.PROPULSOR, possibleEvolutions = { EvolutionType.TO_BASIC })
public class PropulsorMorph extends BasicMorph {

	private static final Logger LOGGER = Logger.getLogger(PropulsorMorph.class);

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph() {
		// Behaviors
		propulsingBehavior = new Propulsing(this, ModelConstants.ENERGY_CONSUMPTION_AT_FULL_THRUST, ModelConstants.PROPULSING_FORCE_MODULUS_AT_FULL_THRUST);
		getActivableBehaviorList().add(propulsingBehavior);
		getActivationRequirements().add(new EnoughEnergy(this, 1));
	}

	@Override
	public boolean deactivate() {
		LOGGER.debug("PropulsorMorph deactivated.");

		// Create a new behavior to slow down the ship
		PropulsorsLost propsLostBehavior = new PropulsorsLost(this);
		// Add it to the ship
		getAlternateBehaviorList().add(propsLostBehavior);

		return true;
	}

	public Propulsing getPropulsingBehavior() {
		return propulsingBehavior;
	}

}
