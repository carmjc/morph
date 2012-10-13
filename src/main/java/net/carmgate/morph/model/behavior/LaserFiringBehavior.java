package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.solid.morph.GunMorph;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

@BehaviorInfo(activationCoolDownTime = 0)
public class LaserFiringBehavior extends Behavior<GunMorph> {

	private static final Logger LOGGER = Logger.getLogger(LaserFiringBehavior.class);

	public LaserFiringBehavior(GunMorph owner) {
		super(owner);
	}

	public LaserFiringBehavior(GunMorph owner, State initialState) {
		super(owner, initialState);
	}

	@Override
	protected void execute() {
		GunMorph gun = getOwner();
		Ship targetShip = gun.getTarget();

		// Detect ship in the way
		double normalLength = Math.hypot(targetShip.getPos().x - gun.getPosInWorld().x, targetShip.getPos().y - gun.getPosInWorld().y);
		for (Ship s : World.getWorld().getShips().values()) {
			if (gun.getShip() == s || targetShip == s) {
				continue;
			}

			double dist = Math.abs((s.getPos().x - gun.getPosInWorld().x) * (targetShip.getPos().y - gun.getPosInWorld().y)
					- (s.getPos().y - gun.getPosInWorld().y) * (targetShip.getPos().x - gun.getPosInWorld().x))
					/ normalLength;
			if (dist < s.getRadius()) {
				Vect3D v1 = new Vect3D(targetShip.getPos());
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

		// Compute transferable energy
		float transferableEnergy = (float) 100 * World.getWorld().getSinceLastUpdateTS() / 1000;
		transferableEnergy = Math.min(transferableEnergy, gun.getEnergy());

		// Remove energy from the gun morph
		gun.setEnergy(gun.getEnergy() - transferableEnergy);
		LOGGER.trace(gun.getEnergy());

		// Focus energy on the ennemy ship's morphs
		int nbMorphs = targetShip.getMorphsByIds().values().size();
		for (Morph m : targetShip.getMorphsByIds().values()) {
			m.setEnergy(m.getEnergy() + transferableEnergy / 2 / nbMorphs);
		}

		if (gun.getEnergy() < 0.1
				|| targetShip.getMorphsByIds().size() == 0) {
			gun.tryToDeactivate(true);
		}
	}
}
