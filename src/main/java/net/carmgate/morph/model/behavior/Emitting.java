package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.morph.EmitterMorph;

public class Emitting extends Behavior<EmitterMorph> {

	private final float minimalEnergy;
	private float coolDownTimer;
	public Vect3D target;
	private final float coolDown;

	/**
	 * @param owner
	 * @param minimalEnergy minimal energy for the behavior to be available
	 * @param coolDown the time between each execution
	 */
	public Emitting(EmitterMorph owner, float minimalEnergy, float coolDown, Vect3D target) {
		super(owner);
		this.minimalEnergy = minimalEnergy;
		this.coolDown = coolDown;
		this.target = target;
	}

	@Override
	protected boolean activate() {
		if (getOwner().energy > minimalEnergy) {
			System.out.println("activated");
			return true;
		}
		return false;
	}

	@Override
	protected boolean deactivate() {
		return true;
	}

	@Override
	protected void execute() {
		if (coolDownTimer <= 0) {
			// TODO hits a random morph in the target ship
			// The rendering is handled separately
			coolDownTimer = coolDown;
		} else {
			coolDownTimer--;
		}
	}

	@Override
	protected int getActivationCoolDownTime() {
		return 0;
	}

	@Override
	protected int getDeactivationCoolDownTime() {
		return 0;
	}

	public Vect3D getTarget() {
		return target;
	}

}
