package net.carmgate.morph.ia.impl.mining;

import java.util.List;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.ia.impl.tracker.GoToPositionAI;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.WorldPositionSupport;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.impl.MinerMorph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

public class GoInMiningRangeAndMineAI implements AI<Ship> {

	private static final Logger LOGGER = Logger.getLogger(GoInMiningRangeAndMineAI.class);

	private boolean done = false;
	private MatterSource target;
	private Ship ship;

	private GoToPositionAI tracker;

	public GoInMiningRangeAndMineAI(Ship ship, MatterSource target) {
		this.ship = ship;
		this.target = target;
	}

	@Override
	public final void compute() {

		// Check whether there are activable MinerMorphs
		done = true;
		for (MinerMorph m : ship.getMorphsByType(MinerMorph.class)) {
			// There is no MinerMorph, this AI cannot fulfill its purpose,
			// therefore, it should deactivate itself
			if (m.canBeActivated()) {
				done = false;
			}
		}
		if (done) {
			return;
		}

		// Check that the ship is at optimal mining distance of the asteroid.
		float distanceToTarget = target.getPos().distance(ship.getPos());
		if (distanceToTarget > 200 && tracker == null) {

			Vect3D trackerPos = new Vect3D(target.getPos()).substract(ship.getPos());
			trackerPos.normalize(distanceToTarget - 200).add(ship.getPos());

			tracker = null;
			for (AI<Ship> ai : ship.getAIList()) {
				if (ai instanceof GoToPositionAI) {
					tracker = (GoToPositionAI) ai;
					tracker.setTargetPos(trackerPos);
					LOGGER.trace("tracker target set");
				}
			}
			if (tracker == null) {
				tracker = new GoToPositionAI(ship, trackerPos);
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
	public final boolean done() {
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
	public final WorldPositionSupport getShip() {
		return ship;
	}

	/**
	 * @return the matter source this AI tries to mine from.
	 */
	public final MatterSource getTarget() {
		return target;
	}

	public final void setTarget(MatterSource target) {
		this.target = target;
		done = false;
		if (tracker != null) {
			tracker = null;
		}
	}

}
