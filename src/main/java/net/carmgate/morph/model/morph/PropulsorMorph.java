package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Propulsing;
import net.carmgate.morph.model.morph.Morph.MorphType;
import net.carmgate.morph.model.ship.Ship;

/**
 * A propulsor morph transforms its potential energy in cinetic energy by unknown means (we don't care)
 * A propulsor morph does not loose mass by being activated.
 */
@MorphInfo(type = MorphType.PROPULSOR)
public class PropulsorMorph extends BasicMorph {

	/** Energy Consumption per millis at full thrust. */
	private final static float ENERGY_CONSUMPTION_AT_FULL_THRUST = 0.1f;
	public final static float PROPULSING_FORCE_MODULUS_AT_FULL_THRUST = 100f;

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);

		// Behaviors
		propulsingBehavior = new Propulsing(this, ENERGY_CONSUMPTION_AT_FULL_THRUST, PROPULSING_FORCE_MODULUS_AT_FULL_THRUST);
		getActivableBehaviorList().add(propulsingBehavior);
	}

	@Override
	public void activate() {
		for (Behavior<?> b : getActivableBehaviorList()) {
			b.tryToActivate();
		}
	}

	@Override
	public void deactivate() {
		for (Behavior<?> b : getActivableBehaviorList()) {
			b.tryToDeactivate();
		}
	}

	public Propulsing getPropulsingBehavior() {
		return propulsingBehavior;
	}

}
