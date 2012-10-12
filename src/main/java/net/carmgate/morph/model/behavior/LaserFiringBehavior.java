package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.solid.morph.GunMorph;
import net.carmgate.morph.model.solid.morph.Morph;
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
		float transferedEnergy = 100;
		float transferableEnergy = transferedEnergy * World.getWorld().getSinceLastUpdateTS() / 1000;
		transferableEnergy = Math.min(transferableEnergy, getOwner().getEnergy());

		// Remove energy from the gun morph
		getOwner().setEnergy(getOwner().getEnergy() - transferableEnergy);
		LOGGER.debug(getOwner().getEnergy());

		// Focus energy on the ennemy ship's morphs
		int nbMorphs = getOwner().getTarget().getMorphsByIds().values().size();
		for (Morph m : getOwner().getTarget().getMorphsByIds().values()) {
			m.setEnergy(m.getEnergy() + transferableEnergy / 2 / nbMorphs);
		}

		if (getOwner().getEnergy() < 0.1
				|| getOwner().getTarget().getMorphsByIds().size() == 0) {
			getOwner().tryToDeactivate(true);
		}
	}
}
