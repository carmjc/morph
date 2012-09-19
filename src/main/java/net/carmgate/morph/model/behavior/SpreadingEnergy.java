package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.morph.Morph;

import org.apache.log4j.Logger;

@BehaviorInfo(alwaysActive = true)
public class SpreadingEnergy extends NoActivationBehavior<Morph> {

	private static final float ENERGY_TRANSFER_PER_SEC = .01f;
	private static final Logger LOGGER = Logger.getLogger(SpreadingEnergy.class);

	public SpreadingEnergy(Morph owner) {
		super(owner);
		LOGGER.trace("new SpreadingBehavior for " + getOwner().getClass());
	}

	@Override
	protected void execute() {
		for (Morph neighbour : getOwner().getNeighbours()) {
			LOGGER.trace("Transferring energy");

			if (neighbour != null
					&& neighbour.getEnergy() / neighbour.getClass().getAnnotation(MorphInfo.class).maxEnergy() < getOwner().getEnergy()
							/ getOwner().getClass().getAnnotation(MorphInfo.class).maxEnergy()) {
				neighbour.setEnergy(neighbour.getEnergy() + ENERGY_TRANSFER_PER_SEC);
				getOwner().setEnergy(getOwner().getEnergy() - ENERGY_TRANSFER_PER_SEC);
			}
		}
	}

}
