package net.carmgate.morph.ia.tracker;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.PropulsorMorph;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

/**
 * All Vect3D instances are understood in the ship's referential.
 */
public class FixedPositionTracker implements IA {

	private static final Logger logger = Logger.getLogger(FixedPositionTracker.class);

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
		for (Morph m : ship.getMorphList()) {
			if (m instanceof PropulsorMorph) {
				propulsorMorphs.add((PropulsorMorph) m);
			}
		}

		// initializes the list of active propulsors
		// this is mandatory so that done() does not considers the ship can't move
		//  after initialization
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : propulsorMorphs) {
			if (!m.disabled) {
				activePropulsorMorphs.add(m);
			}
		}
	}

	public void compute() {
		// update the list of active propulsors
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : propulsorMorphs) {
			if (!m.disabled) {
				activePropulsorMorphs.add(m);
			}
		}

		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMassInShip());
		ship.transformShipToWorldCoords(comInWorld);

		// The direction vector points in the direction the ship should try to go
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);
		float distanceToTarget = comToTarget.modulus();
		float nbSecondsToBreak = ship.posSpeed.modulus() / (PropulsorMorph.propulsingForceModulusAtFullThrust * activePropulsorMorphs.size());
		float distanceToBreak = ship.posSpeed.modulus() * nbSecondsToBreak;
		logger.debug("Distance to break/distance to target : " + distanceToBreak + "/" + distanceToTarget);
		float rampedSpeed = ship.maxSpeed * distanceToTarget / distanceToBreak;//ship.slowingDistance;
		float clippedSpeed = Math.min(rampedSpeed, ship.maxSpeed);
		Vect3D desiredVelocity = new Vect3D(comToTarget);
		desiredVelocity.normalize(clippedSpeed);

		Vect3D steeringForce = new Vect3D(desiredVelocity);
		steeringForce.substract(ship.posSpeed);

		// We balance forces of both sides
		// and adjust thrust according to max_accel, max_speed and distanceToTarget
		for (PropulsorMorph m : activePropulsorMorphs) {
			// Adjust thrust for moments
			float thrust = steeringForce.modulus() / ship.maxSpeed;
			m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(steeringForce));
			m.getPropulsingBehavior().setThrustPercentage(thrust);

			m.activate();
		}

	}

	public boolean done() {

		// The direction vector points in the direction the ship should try to go
		// Calculate center of mass in world
		Vect3D comInWorld = new Vect3D(ship.getCenterOfMassInShip());
		ship.transformShipToWorldCoords(comInWorld);
		Vect3D comToTarget = new Vect3D(targetPos);
		comToTarget.substract(comInWorld);
		float distanceToTarget = comToTarget.modulus();

		if (distanceToTarget < 15 && ship.posSpeed.modulus() < 2) {
			done = true;
		}

		// Dectect if all the propulsors are out of energy
		if (activePropulsorMorphs.size() == 0) {
			done = true;
		}

		if (done) {
			ship.ownForceList.clear();
			ship.posAccel.copy(Vect3D.NULL);

			for (PropulsorMorph morph : propulsorMorphs) {
				morph.deactivate();
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
