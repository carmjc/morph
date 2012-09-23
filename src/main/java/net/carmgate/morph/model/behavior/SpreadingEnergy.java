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
		float totalEnergyTransfered = 0;

		for (Morph neighbour : getOwner().getNeighbours()) {
			LOGGER.trace("Transferring energy");

			if (neighbour != null
					&& neighbour.getEnergy() / neighbour.getMaxEnergy() < getOwner().getEnergy()
							/ getOwner().getMaxEnergy()
					&& neighbour.getEnergy() < neighbour.getMaxEnergy()) {
				float transferRatio = neighbour.getMass() / neighbour.getMaxMass();
				float energyTransferedToNeighbour = ModelConstants.ENERGY_TRANSFER_PER_SEC * transferRatio * World.getWorld().getSinceLastUpdateTS() / 1000;
				neighbour.setEnergy(neighbour.getEnergy() + energyTransferedToNeighbour);
				getOwner().setEnergy(getOwner().getEnergy() - energyTransferedToNeighbour);

				totalEnergyTransfered += energyTransferedToNeighbour;
			}
		}

		if (getOwner().getExcessEnergy() > 0) {
			getOwner().setEnergyDiffused(ModelConstants.MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND - totalEnergyTransfered);
			getOwner().setEnergy(getOwner().getEnergy() - ModelConstants.MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND - totalEnergyTransfered);
		}
	}
}
