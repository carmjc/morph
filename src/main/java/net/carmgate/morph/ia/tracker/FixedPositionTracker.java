package net.carmgate.morph.ia.tracker;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.prop.PropulsorMorph;
import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

/**
 * All Vect3D instances are understood in the ship's referential.
 * Warning: if all the propulsors of a ship are deactivated at any moment in time,
 * the tracker will stop trying to direct the ship to the target.
 */
public class FixedPositionTracker implements IA {

	private static final Logger LOGGER = Logger.getLogger(FixedPositionTracker.class);

	private final List<PropulsorMorph> propulsorMorphs = new ArrayList<PropulsorMorph>();
	private final List<PropulsorMorph> activePropulsorMorphs = new ArrayList<PropulsorMorph>();
	private final Ship ship;
	private Vect3D targetPos;

	private boolean done;

	public FixedPositionTracker(Ship ship, Vect3D targetPos) {
		this.ship = ship;
		this.targetPos = targetPos;
		done = false;

		// extract every morph that can make the ship move.
		for (Morph m : ship.getMorphsByIds().values()) {
			if (m instanceof PropulsorMorph) {
				propulsorMorphs.add((PropulsorMorph) m);
			}
		}

		// initializes the list of active propulsors
		// this is mandatory so that done() does not considers the ship can't move
		// after initialization
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : propulsorMorphs) {
			if (m.canBeActivated()) {
				activePropulsorMorphs.add(m);
			}
		}
	}

	public void compute() {
		// update the list of active propulsors
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : propulsorMorphs) {
			if (m.canBeActivated()) {
				activePropulsorMorphs.add(m);
			}
		}

		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMass());
		ship.transformShipToWorldCoords(comInWorld);

		// the vector from the ship's center of mass to the target
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);

		float distanceToTarget = comToTarget.modulus();
		float nbSecondsToBreak = ship.getPosSpeed().modulus()
				/ (ModelConstants.PROPULSING_FORCE_MODULUS_AT_FULL_THRUST * activePropulsorMorphs.size());
		float distanceToBreak = ship.getPosSpeed().modulus() * nbSecondsToBreak;
		LOGGER.trace("Distance to break/distance to target : " + distanceToBreak + "/" + distanceToTarget);
		float rampedSpeed = ModelConstants.MAX_SPEED * (distanceToTarget - distanceToBreak) / distanceToBreak;// ship.slowingDistance;
		float clippedSpeed = Math.min(rampedSpeed, ModelConstants.MAX_SPEED);
		Vect3D desiredVelocity = new Vect3D(comToTarget);
		desiredVelocity.normalize(clippedSpeed);
		LOGGER.trace("DesiredVelocity: " + desiredVelocity.modulus());

		Vect3D steeringForce = new Vect3D(desiredVelocity);
		steeringForce.substract(ship.getPosSpeed());

		// We balance forces of both sides
		// and adjust thrust according to max_accel, max_speed and distanceToTarget
		for (PropulsorMorph m : activePropulsorMorphs) {
			// Adjust thrust for moments
			float thrust = steeringForce.modulus() / ModelConstants.MAX_SPEED;
			m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(steeringForce));
			m.getPropulsingBehavior().setThrustPercentage(thrust);

			m.tryToActivate();
		}

	}

	public boolean done() {

		// The direction vector points in the direction the ship should try to go
		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMass());
		ship.transformShipToWorldCoords(comInWorld);
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);
		float distanceToTarget = comToTarget.modulus();

		if (distanceToTarget < 20 && ship.getPosSpeed().modulus() < 30) {
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
			for (PropulsorMorph morph : propulsorMorphs) {
				morph.tryToDeactivate();
			}
		}

		return done;
	}

	public Vect3D getTargetPos() {
		return targetPos;
	}

	public void setTargetPos(Vect3D targetPos) {
		this.targetPos = targetPos;
	}

}
