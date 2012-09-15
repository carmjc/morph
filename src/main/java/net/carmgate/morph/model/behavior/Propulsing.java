package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.virtual.physics.Force;

import org.apache.log4j.Logger;

public class Propulsing extends Behavior<Morph> {

	private static final Logger logger = Logger.getLogger(Propulsing.class);

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
	@Deprecated
	protected boolean activate() {
		return true;
	}

	@Override
	@Deprecated
	protected boolean deactivate() {
		return true;
	}

	@Override
	protected void execute() {

		// FIXME Should be done elsewhere. A behavior should not be responsible for deactivated its effects when its owner is disabled
		if (getOwner().disabled) {
			return;
		}

		// update force
		force.vector.copy(Vect3D.NORTH);
//		force.vector.rotate(getOwner().getRotInWorld());
		force.vector.normalize(propulsingForceModulusAtFullThrust * thrustPercentage);

		// energy loss
		getOwner().energy -= energyConsumptionAtFullThrust
				* getOwner().getMaxEnergy()
				* thrustPercentage
				* (World.getWorld().getCurrentTS() - getLastUpdateMsec()) / 1000;

		getOwner().getShip().ownForceList.add(force);
	}

	@Override
	@Deprecated
	protected int getActivationCoolDownTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@Deprecated
	protected int getDeactivationCoolDownTime() {
		// TODO Auto-generated method stub
		return 0;
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
