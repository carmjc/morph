package net.carmgate.morph.ia;

import java.util.List;

import net.carmgate.morph.ia.tracker.FixedPositionTracker;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.MinerMorph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

public class MiningAI implements AI {

	private static final Logger LOGGER = Logger.getLogger(MiningAI.class);

	private boolean done = false;
	private MatterSource target;
	private Ship ship;

	private FixedPositionTracker tracker;

	public MiningAI(Ship ship, MatterSource target) {
		this.ship = ship;
		this.target = target;
	}

	@Override
	public void compute() {
		// Check that the ship is at optimal mining distance of the asteroid.
		float distanceToTarget = target.getPos().distance(ship.getPos());
		if (distanceToTarget > 200 && tracker == null) {

			Vect3D trackerPos = new Vect3D(target.getPos()).substract(ship.getPos());
			trackerPos.normalize(distanceToTarget - 200).add(ship.getPos());

			tracker = null;
			for (AI ai : ship.getAIList()) {
				if (ai instanceof FixedPositionTracker) {
					tracker = (FixedPositionTracker) ai;
					tracker.setTargetPos(trackerPos);
					LOGGER.trace("tracker target set");
				}
			}
			if (tracker == null) {
				tracker = new FixedPositionTracker(ship, trackerPos);
				ship.getAIList().add(tracker);
				LOGGER.trace("tracker target set");
			}
		}

		// If it is at mining distance, mine !
		if (distanceToTarget < 300) {
			// Find mining morphs in the ship and give them a target
			List<MinerMorph> miningMorphs = ship.getMorphsByType(MinerMorph.class);
			if (miningMorphs != null) {
				for (MinerMorph m : miningMorphs) {
					m.setTarget(target);
					m.tryToActivate();
				}
			}

			done = true;
		}
	}

	@Override
	public boolean done() {
		if (done) {
			if (tracker != null) {
				tracker = null;
			}
		}
		return done;
	}

	/**
	 * @return the ship this AI operates on.
	 */
	public Ship getShip() {
		return ship;
	}

	/**
	 * @return the matter source this AI tries to mine from.
	 */
	public MatterSource getTarget() {
		return target;
	}

	public void setTarget(MatterSource target) {
		this.target = target;
		done = false;
		if (tracker != null) {
			tracker = null;
		}
	}

}
