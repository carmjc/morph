package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.World;
import net.carmgate.morph.model.physics.Force;

public class PropulsorMorph extends BasicMorph {

	private final Force force = new Force(this, new Vect3D(0.02f, 0, 0));

	public float thrustFactor = 1;
	public final float massLossAtFullThrust = 0.1f;

	public PropulsorMorph(float x, float y, float z) {
		super(x, y, z);
		maxMass = mass = 500;
		disableMass = 100;
		reenableMass = 200;
	}

	@Override
	public void activate() {
		if (energy <= 0) {
			disabled = true;
			return;
		}

		Force generatedForce = getGeneratedForce();
		ship.ownForceList.add(generatedForce);
		World.getWorld().getForceList().add(generatedForce);

		// energy loss
		energy -= .1;

		// mass transfer to void
		float massLoss = massLossAtFullThrust * thrustFactor;
		mass -= massLoss;
		World.getWorld().getWorldArea(pos).mass += massLoss;
	}

	@Override
	public void deactivate() {
		Force generatedForce = getGeneratedForce();
		ship.ownForceList.remove(generatedForce);
		World.getWorld().getForceList().remove(generatedForce);
	}

	public Force getGeneratedForce() {
		Vect3D vector = new Vect3D(force.vector);
		vector.normalize(vector.modulus() * thrustFactor);
		return new Force(force.target, vector);
	}

	@Override
	public MorphType getType() {
		return MorphType.PROPULSOR;
	}

	public void setThrustFactor(float thrustFactor) {
		this.thrustFactor = thrustFactor;
	}
}
