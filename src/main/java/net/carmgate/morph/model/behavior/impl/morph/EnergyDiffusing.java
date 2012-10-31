package net.carmgate.morph.model.behavior.impl.morph;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.behavior.NoActivationBehavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

/**
 * This behavior diffuses energy from the current morph to the adjacent ones (neighbors).
 * <ul><li>If the energy currently stored in the morph is below max energy,
 * the amount diffused per second is {@link ModelConstants#ENERGY_TRANSFER_PER_SEC}}</li>
 * <li>If the energy currently stored in the morph is above max energy,
 * the amount diffused per second is {@link ModelConstants#MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND}</li>
 * </ul>
 */
@BehaviorInfo(alwaysActive = true)
public class EnergyDiffusing extends NoActivationBehavior<Morph> {

	private static final Logger LOGGER = Logger.getLogger(EnergyDiffusing.class);

	public EnergyDiffusing(Morph owner) {
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
