package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

@BehaviorInfo(alwaysActive = true)
public class SpreadingEnergy extends NoActivationBehavior<Morph> {

	private static final Logger LOGGER = Logger.getLogger(SpreadingEnergy.class);

	public SpreadingEnergy(Morph owner) {
		super(owner);
		LOGGER.trace("new SpreadingBehavior for " + getOwner().getClass());
	}

	@Override
	protected void execute() {
		for (Morph neighbor : getOwner().getNeighbors()) {
			LOGGER.trace("Transferring energy");

			if (neighbor != null
					&& neighbor.getEnergy() / neighbor.getMaxEnergy() < getOwner().getEnergy() * 0.9
							/ getOwner().getMaxEnergy()
					&& neighbor.getEnergy() < neighbor.getMaxEnergy() * 0.9) {
				float transferRatio = neighbor.getMass() / neighbor.getMaxMass();
				float energyTransferedToNeighbor = ModelConstants.ENERGY_TRANSFER_PER_SEC * transferRatio * World.getWorld().getSinceLastUpdateTS() / 1000;
				neighbor.setEnergy(neighbor.getEnergy() + energyTransferedToNeighbor);
				getOwner().setEnergy(getOwner().getEnergy() - energyTransferedToNeighbor);
			}
		}

		if (getOwner().getExcessEnergy() > 0) {
			getOwner().setEnergyDiffused(ModelConstants.MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND * World.getWorld().getSinceLastUpdateTS() / 1000);
			getOwner().setEnergy(getOwner().getEnergy()
					- ModelConstants.MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND
					* World.getWorld().getSinceLastUpdateTS()
					/ 1000);
		}
	}
}
