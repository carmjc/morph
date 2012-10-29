package net.carmgate.morph.ia.impl.morph.fight;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.ia.impl.morph.tracker.GoToPositionAI;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.impl.GunMorph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

public class GoToShipAndEngage implements AI<Ship> {

	private static final Logger LOGGER = Logger.getLogger(GoToShipAndEngage.class);

	private Morph target;
	private Ship ship;

	private GoToPositionAI tracker;

	public GoToShipAndEngage(Ship ship, Morph target) {
		this.ship = ship;
		this.target = target;
	}

	@Override
	public void compute() {
		// Check that the ship is at optimal mining distance of the asteroid.
		float distanceToTarget = target.getPosInWorld().distance(ship.getPos());
		if (distanceToTarget > 300) {
			LOGGER.debug("Moving");

			Vect3D trackerPos = new Vect3D(target.getPosInWorld()).substract(ship.getPos());
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

		// Engage
		for (GunMorph m : ship.getMorphsByType(GunMorph.class)) {
			LOGGER.debug("Gun state: " + m.getState());
			m.setTarget(target);
			if (m.getState() == State.INACTIVE) {
				m.tryToActivate();
			}
		}
	}

	@Override
	public boolean done() {
		return false;
	}

	public Morph getTarget() {
		return target;
	}

	public void setTarget(Morph target) {
		this.target = target;
	}

}
