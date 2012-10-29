package net.carmgate.morph.model.behavior.impl.morph;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.impl.MinerMorph;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

public class Mining extends Behavior<MinerMorph> {

	private static final Logger LOGGER = Logger.getLogger(Mining.class);

	public Mining(MinerMorph owner) {
		super(owner);
	}

	public Mining(MinerMorph owner, State initialState) {
		super(owner, initialState);
	}

	@Override
	protected void execute() {
		MatterSource matterSource = getOwner().getTarget();

		// deactivate if matterSource empty
		if (matterSource.getMass() <= 0) {
			getOwner().tryToDeactivate(true);
			return;
		}

		// if we are not at mining distance, deactivate the morph
		if (matterSource.getPos().distance(getOwner().getPosInWorld()) > 500) {
			getOwner().tryToDeactivate(true);
		}

		// get the amount of transferable mass
		float transferedMass = Math.min(getOwner().getTarget().getMass(), (float) ModelConstants.MINING_SPEED * World.getWorld().getSinceLastUpdateTS()
				/ 1000);
		LOGGER.trace("Mining - transferredMass: " + transferedMass
				+ " - asteroidMass: " + getOwner().getTarget().getMass()
				+ " - calculated transferable mass: " + (float) ModelConstants.MINING_SPEED * World.getWorld().getSinceLastUpdateTS() / 1000);

		// give the matter to the miner morph
		getOwner().getShip().setStoredMass(getOwner().getShip().getStoredMass() + transferedMass);
		matterSource.setMass(matterSource.getMass() - transferedMass);
	}
}
