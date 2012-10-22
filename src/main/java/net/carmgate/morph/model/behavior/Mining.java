package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.MinerMorph;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

public class Mining extends Behavior<MinerMorph> {

	private static final Logger LOGGER = Logger.getLogger(Mining.class);
	private static int MINING_SPEED = 200;

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

		// else let's transfer matter
		int transferedMass = (int) Math.min(MINING_SPEED * World.getWorld().getSinceLastUpdateTS() / 1000, matterSource.getMass());
		getOwner().setMass(getOwner().getMass() + transferedMass);
		matterSource.setMass(matterSource.getMass() - transferedMass);
	}
}
