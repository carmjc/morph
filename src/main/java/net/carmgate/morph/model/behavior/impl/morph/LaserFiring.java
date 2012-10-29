package net.carmgate.morph.model.behavior.impl.morph;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.impl.GunMorph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

@BehaviorInfo(activationCoolDownTime = 0)
public class LaserFiring extends Behavior<GunMorph> {

	private static final Logger LOGGER = Logger.getLogger(LaserFiring.class);
	private Morph effectiveTarget = null;

	public LaserFiring(GunMorph owner) {
		super(owner);
	}

	public LaserFiring(GunMorph owner, State initialState) {
		super(owner, initialState);
	}

	@Override
	protected boolean deactivate(boolean forced) {
		getOwner().setTarget(null);
		return super.deactivate(forced);
	}

	@Override
	protected void execute() {
		GunMorph gun = getOwner();
		Morph targetMorph = gun.getTarget();
		Ship targetShip = gun.getTarget().getShip();
		Morph newTargetMorph = null;

		// If the target is too far away, deactivate itself
		if (targetMorph.getPosInWorld().distance(getOwner().getPosInWorld()) > ModelConstants.MAX_FIRING_DISTANCE) {
			getOwner().tryToDeactivate(true);
			return;
		}

		// Detect ship in the way
		double normalLength = Math.hypot(targetMorph.getPosInWorld().x - gun.getPosInWorld().x, targetMorph.getPosInWorld().y - gun.getPosInWorld().y);
		for (Ship s : World.getWorld().getShips().values()) {

			// No need to compute anything for the gun and the initial target
			// We do not accept friendly fire ...
			if (gun.getShip() == s || gun.getShip().getOwner() == s.getOwner()) {
				continue;
			}

			// Computing distance between the center of a ship and the line going from gun to target.
			double dist = Math.abs((s.getPos().x - gun.getPosInWorld().x) * (targetMorph.getPosInWorld().y - gun.getPosInWorld().y)
					- (s.getPos().y - gun.getPosInWorld().y) * (targetMorph.getPosInWorld().x - gun.getPosInWorld().x))
					/ normalLength;

			// If this distance is smaller than the ship's radius, then we might have a hit.
			// We just have to check that the given hit is really between the gun and the target.
			if (dist < s.getRadius()) {
				Vect3D v1 = new Vect3D(targetMorph.getPosInWorld());
				v1.substract(gun.getPosInWorld());
				Vect3D v2 = new Vect3D(s.getPos());
				v2.substract(gun.getPosInWorld());
				float scal = v1.prodScal(v2);
				float modulus = v1.modulus();
				if (scal < modulus * modulus) {
					targetShip = s;
				}
			}

		}

		// we need to find the morph targeted by the laser
		double closestMorphDist = normalLength;
		for (Morph m : targetShip.getMorphsByIds().values()) {

			// dont process the intended target
			if (m == getOwner().getTarget()) {
				continue;
			}

			// Computing distance between the center of a ship and the line going from gun to target.
			double dist = Math.abs((m.getPosInWorld().x - gun.getPosInWorld().x) * (targetMorph.getPosInWorld().y - gun.getPosInWorld().y)
					- (m.getPosInWorld().y - gun.getPosInWorld().y) * (targetMorph.getPosInWorld().x - gun.getPosInWorld().x))
					/ normalLength;

			// if the distance is smaller than the morph radius we might have a hit
			// we need to check that it is the closest one
			// FIXME we need to ensure that the given morph is between the gun and the target and
			// not outside the gun-target segment
			if (dist < 16 && gun.getPosInWorld().distance(m.getPosInWorld()) < closestMorphDist) {
				newTargetMorph = m;
				closestMorphDist = gun.getPosInWorld().distance(m.getPosInWorld());
			}
		}

		// Compute transferable energy
		float transferableEnergy = (float) 100 * World.getWorld().getSinceLastUpdateTS() / 1000;
		transferableEnergy = Math.min(transferableEnergy, gun.getEnergy());

		// Remove energy from the gun morph
		gun.setEnergy(gun.getEnergy() - transferableEnergy);
		LOGGER.trace(gun.getEnergy());

		if (newTargetMorph != null) {
			// Focus energy on the "in the way" morph
			effectiveTarget = newTargetMorph;
		} else {
			// Focus energy on the enemy targeted morphs
			effectiveTarget = targetMorph;
		}
		effectiveTarget.setEnergy(effectiveTarget.getEnergy() + transferableEnergy / 2);

		if (gun.getEnergy() < 0.1 || targetMorph.getShip().getMorphsByIds().get(targetMorph.getId()) == null) {
			gun.tryToDeactivate(true);
		}
	}

	/**
	 * The effective target of the behavior.
	 * It might be the intended target or it might be a morph in the way.
	 * @return the effective target of the gun.
	 */
	public final Morph getEffectiveTarget() {
		return effectiveTarget;
	}
}
