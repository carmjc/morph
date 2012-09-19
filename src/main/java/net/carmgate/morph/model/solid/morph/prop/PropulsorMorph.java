package net.carmgate.morph.model.solid.morph.prop;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.prop.Propulsing;
import net.carmgate.morph.model.behavior.prop.PropulsorsLost;
import net.carmgate.morph.model.requirements.EnoughEnergy;
import net.carmgate.morph.model.solid.morph.BasicMorph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

/**
 * A propulsor morph transforms its potential energy in cinetic energy by unknown means (we don't care)
 * A propulsor morph does not loose mass by being activated.
 */
@MorphInfo(type = MorphType.PROPULSOR)
public class PropulsorMorph extends BasicMorph {

	/** Energy Consumption per millis at full thrust. */
	private final static float ENERGY_CONSUMPTION_AT_FULL_THRUST = 1f;
	public final static float PROPULSING_FORCE_MODULUS_AT_FULL_THRUST = 100f;

	private static final Logger LOGGER = Logger.getLogger(PropulsorMorph.class);

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);

		// Behaviors
		propulsingBehavior = new Propulsing(this, ENERGY_CONSUMPTION_AT_FULL_THRUST, PROPULSING_FORCE_MODULUS_AT_FULL_THRUST);
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
