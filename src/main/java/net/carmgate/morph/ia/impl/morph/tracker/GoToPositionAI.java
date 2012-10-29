package net.carmgate.morph.ia.impl.morph.tracker;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.impl.PropulsorMorph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

/**
 * All Vect3D instances are understood in the ship's referential.
 * Warning: if all the propulsors of a ship are deactivated at any moment in time,
 * the tracker will stop trying to direct the ship to the target.
 */
public class GoToPositionAI implements AI<Ship> {

	private static final Logger LOGGER = Logger.getLogger(GoToPositionAI.class);

	private final List<PropulsorMorph> activePropulsorMorphs = new ArrayList<PropulsorMorph>();
	private final Ship ship;
	private Vect3D targetPos;

	private boolean done;

	private float activePropsToMorphsRatio;

	private float clippedSpeed;

	/**
	 * Create a new fixed position tracker.
	 * @param ship the ship this tracker is bound to.
	 * @param targetPos the target of the tracker.
	 */
	public GoToPositionAI(Ship ship, Vect3D targetPos) {
		this.ship = ship;
		this.targetPos = targetPos;
		done = false;

		// initializes the list of active propulsors
		// this is mandatory so that done() does not considers the ship can't move
		// after initialization
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : ship.getMorphsByType(PropulsorMorph.class)) {
			if (m.canBeActivated()) {
				activePropulsorMorphs.add(m);
			}
		}
	}

	@Override
	public final void compute() {
		// update the list of active propulsors
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : ship.getMorphsByType(PropulsorMorph.class)) {
			if (m.canBeActivated()) {
				activePropulsorMorphs.add(m);
			}
		}

		// Compute ratio of active props over total morphs
		activePropsToMorphsRatio = (float) activePropulsorMorphs.size() / ship.getMorphsByIds().size();

		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getComInShip());
		ship.transformShipToWorldCoords(comInWorld);

		// the vector from the ship's center of mass to the target
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);

		float distanceToTarget = comToTarget.modulus();
		LOGGER.trace("DistanceToTarget: " + distanceToTarget + ", shipSpeed: " + ship.getPosSpeed().modulus());

		// This is speed
		final float maxForce = ModelConstants.PROPULSING_FORCE_MODULUS_AT_FULL_THRUST * activePropulsorMorphs.size();
		float nbSecondsToBreak = ship.getPosSpeed().modulus()
				* ship.getTotalMass()
				/ maxForce;
		float distanceToBreak = -1 / 2 * maxForce / ship.getTotalMass() * nbSecondsToBreak * nbSecondsToBreak + ship.getPosSpeed().modulus()
				* nbSecondsToBreak;
		LOGGER.trace("nbSecondsToBreak: " + nbSecondsToBreak + ", distanceToBreak: " + distanceToBreak);

		final float shipMaxSpeed = ModelConstants.MAX_SPEED_PER_PROP_MORPH * activePropsToMorphsRatio;
		float rampedSpeed;
		if (comToTarget.prodScal(ship.getPosSpeed()) > 0) {
			rampedSpeed = shipMaxSpeed * (distanceToTarget - distanceToBreak) / distanceToBreak;
		} else {
			rampedSpeed = shipMaxSpeed;
		}
		clippedSpeed = Math.min(rampedSpeed, shipMaxSpeed);
		Vect3D desiredVelocity = new Vect3D(comToTarget);
		desiredVelocity.normalize(clippedSpeed);

		Vect3D steeringForce = new Vect3D(desiredVelocity);
		steeringForce.substract(ship.getPosSpeed());

		// We adjust thrust according to max_accel, max_speed and distanceToTarget
		for (PropulsorMorph m : activePropulsorMorphs) {
			// Adjust thrust for moments
			LOGGER.trace("steeringForce: " + steeringForce.modulus());
			float thrustPercentage = 1;
			if (Math.abs(Math.sin(Math.toRadians(steeringForce.angleWith(ship.getPosSpeed())))
					* distanceToTarget) < 10
					|| ship.getPosSpeed().modulus() > shipMaxSpeed * 0.5) {
				thrustPercentage = Math.min(1, steeringForce.modulus() / shipMaxSpeed);
			}
			LOGGER.trace("thrustPercentage: " + thrustPercentage + ", sin: " + Math.abs(Math.sin(Math.toRadians(steeringForce.angleWith(ship.getPosSpeed())))
					* distanceToTarget));

			m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(steeringForce));
			m.getPropulsingBehavior().setThrustPercentage(thrustPercentage);

			m.tryToActivate();
		}

	}

	@Override
	public final boolean done() {

		// The direction vector points in the direction the ship should try to go
		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getComInShip());
		ship.transformShipToWorldCoords(comInWorld);
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);
		float distanceToTarget = comToTarget.modulus();

		LOGGER.trace("distanceToTarget: " + distanceToTarget + ", shipSpeed: " + ship.getPosSpeed().modulus());
		if (distanceToTarget < ModelConstants.ACCEPTABLE_DISTANCE_TO_TARGET && ship.getPosSpeed().modulus() < ModelConstants.ACCEPTABLE_SHIP_SPEED) {
			done = true;
		}

		// This is done before checking if there is no more active morph
		// If we don't, the speed of a ship with no more active morphs will be
		// set to 0 although we did not reach the target
		if (done) {
			ship.getOwnForceList().clear();
			ship.getPosAccel().copy(Vect3D.NULL);
			ship.getPosSpeed().copy(Vect3D.NULL);
		}

		// Dectect if there is no more active prop morphs (for instance, if they are out of energy)
		LOGGER.trace("Number of active propulsor morphs: " + activePropulsorMorphs.size());
		if (activePropulsorMorphs.size() == 0) {
			done = true;
		}

		// Whatever might be the reason, if the IA is done, we shut down the prop morphs
		if (done) {
			for (PropulsorMorph morph : ship.getMorphsByType(PropulsorMorph.class)) {
				morph.tryToDeactivate();
			}
			LOGGER.trace("tracker done");
		}

		return done;
	}

	public final Vect3D getTargetPos() {
		return targetPos;
	}

	public final void setTargetPos(Vect3D targetPos) {
		this.targetPos = targetPos;
	}

}
