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
	private final static float propulsingForceModulusAtFullThrust = 100f;

	/** The default activable behavior of this morph. */
	private final Propulsing propulsingBehavior;

	public PropulsorMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		maxMass = mass = 500;
		disableMass = 100;
		reenableMass = 200;

		//Behaviors
		propulsingBehavior = new Propulsing(this, energyConsumptionAtFullThrust, propulsingForceModulusAtFullThrust);
		activableBehaviorList.add(propulsingBehavior);
	}

	@Override
	public void afterActivate() {
		for (Behavior<?> b : activableBehaviorList) {
			b.tryToActivate();
		}
	}

	@Override
	public void afterDeactivate() {
		for (Behavior<?> b : activableBehaviorList) {
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
