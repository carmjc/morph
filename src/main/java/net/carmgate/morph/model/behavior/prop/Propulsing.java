package net.carmgate.morph.model.behavior.prop;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.physics.Force;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

public class Propulsing extends Behavior<Morph> {

	private static final Logger LOGGER = Logger.getLogger(Propulsing.class);

	/** The force generated by this behavior. */
	private Force force = new Force(getOwner(), new Vect3D());

	/** Thrust percentage. 0 <= thrustPercentage <= 1. */
	private float thrustPercentage = 1f;

	private final float energyConsumptionAtFullThrust;
	private final float propulsingForceModulusAtFullThrust;

	public Propulsing(Morph morph, float energyConsumptionAtFullThrust, float propulsingForceModulusAtFullThrust) {
		super(morph);
		this.energyConsumptionAtFullThrust = energyConsumptionAtFullThrust;
		this.propulsingForceModulusAtFullThrust = propulsingForceModulusAtFullThrust;

	}

	@Override
	protected void execute() {
		// update force
		// The propulsing force is always oriented to the north,
		// because the morph is rotated by the trackers
		force.getVector().copy(Vect3D.NORTH);
		// force.vector.rotate(getOwner().getRotInWorld());
		force.getVector().normalize(propulsingForceModulusAtFullThrust * thrustPercentage);

		// energy loss if we can calculate a value
		if (getLastExecutionTS() != 0 && getActivationTS() != 0) {
			float energyCost = getOwner().getEnergy() - energyConsumptionAtFullThrust
					* getOwner().getClass().getAnnotation(MorphInfo.class).maxEnergy()
					* thrustPercentage
					* (World.getWorld().getCurrentTS() - getLastExecutionTS()) / 1000;
			LOGGER.trace(energyCost);
			getOwner().setEnergy(energyCost);
		}

		getOwner().getShip().getOwnForceList().add(force);
	}

	public Force getForce() {
		return force;
	}

	public float getThrustPercentage() {
		return thrustPercentage;
	}

	public void setThrustPercentage(float thrustPercentage) {
		this.thrustPercentage = thrustPercentage;
	}

}
