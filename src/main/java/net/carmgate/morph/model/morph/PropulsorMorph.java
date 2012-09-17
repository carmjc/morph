package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Propulsing;
import net.carmgate.morph.model.ship.Ship;

/**
 * A propulsor morph transforms its potential energy in cinetic energy by unknown means (we don't care)
 * A propulsor morph does not loose mass by being activated.
 */
public class PropulsorMorph extends BasicMorph {

	/** Energy Consumption per millis at full thrust. */
	private final static float energyConsumptionAtFullThrust = 0.000001f;
	public final static float PROPULSING_FORCE_MODULUS_AT_FULL_THRUST = 100f;

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		setMaxMass(500);
		setMass(500);
		setDisableMass(100);
		setReenableMass(200);

		// Behaviors
		propulsingBehavior = new Propulsing(this, energyConsumptionAtFullThrust, PROPULSING_FORCE_MODULUS_AT_FULL_THRUST);
		getActivableBehaviorList().add(propulsingBehavior);
	}

	@Override
	public void afterActivate() {
		for (Behavior<?> b : getActivableBehaviorList()) {
			b.tryToActivate();
		}
	}

	@Override
	public void afterDeactivate() {
		for (Behavior<?> b : getActivableBehaviorList()) {
			b.tryToDeactivate();
		}
	}

	public Propulsing getPropulsingBehavior() {
		return propulsingBehavior;
	}

	@Override
	public MorphType getType() {
		return MorphType.PROPULSOR;
	}

}
