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

	private static final float MAX_ACCEL = 1;
	private static final float MAX_SPEED = 1;
	private static final float CAP_DISTANCE = 200;

	private final List<PropulsorMorph> propulsorMorphs = new ArrayList<PropulsorMorph>();
	private final List<PropulsorMorph> activePropulsorMorphs = new ArrayList<PropulsorMorph>();
	private final Ship ship;
	private final Vect3D targetPos;

	private boolean done;
	private static Vect3D dummyVect = new Vect3D();

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
		comToTarget.normalize(1);

		// Ship speed and ship accel towards the target
		Vect3D posAccel = new Vect3D(ship.posAccel);
		float shipAccel = Math.abs(posAccel.prodScal(comToTarget));
		Vect3D posSpeed = new Vect3D(ship.posSpeed);
		float shipSpeed = posSpeed.prodScal(comToTarget);
		float distanceToTarget = ship.pos.distance(targetPos);
		logger.debug(shipAccel + ", " + shipSpeed + ", " + distanceToTarget + ", " + ship.getCenterOfMassInShip().modulus() + ", " + (distanceToTarget <= shipSpeed));

		if (distanceToTarget <= shipSpeed * shipSpeed / (2 * shipAccel) && shipSpeed > 0) {
			comToTarget.rotate(180);

			if (Math.abs(shipSpeed) < 1) {
				done = true;
				return;
			}
		}

		Vect3D directionVector = new Vect3D(comToTarget);
		directionVector.normalize(ship.posSpeed.modulus() + ship.posAccel.modulus());
		directionVector.substract(ship.posSpeed);

		// Calculate left props and right props considering the direction vector
		// To evaluate which side is each propulsor morph, it suffices to evaluate the dot product
		//  between the direction vector rotated -90° and the position vector of the morph into the ship.
		Vect3D rotatedCopy = new Vect3D(directionVector);
		rotatedCopy.rotate(-90);

		List<PropulsorMorph> leftMorphs = new ArrayList<PropulsorMorph>();
		List<PropulsorMorph> rightMorphs = new ArrayList<PropulsorMorph>();
		Vect3D v = new Vect3D();
		for (PropulsorMorph m : activePropulsorMorphs) {
			v.copy(m.getPosInWorld());
			v.substract(comInWorld);
			float prodScal = rotatedCopy.prodScal(v);
			if (prodScal > 0) {
				leftMorphs.add(m);
			} else if (prodScal < 0) {
				rightMorphs.add(m);
			}
		}

		// Now that we have the left and right morphs, let's evaluate the total rotational moment of each collection.
		float leftMoment = 0;
		float rightMoment = 0;
		Vect3D moment = new Vect3D();
		for (PropulsorMorph m : leftMorphs) {
			moment.copy(m.getPosInWorld());
			moment.substract(comInWorld);
			leftMoment += moment.prodVectOnZ(directionVector);
		}
		for (PropulsorMorph m : rightMorphs) {
			moment.copy(m.getPosInWorld());
			moment.substract(comInWorld);
			rightMoment -= moment.prodVectOnZ(directionVector);
		}

		float leftThrust = 1;
		float rightThrust = 1;
//		if (leftMoment != 0 && rightMoment != 0) {
//			if (leftMoment < rightMoment) {
//				rightThrust = leftMoment / rightMoment;
//			} else {
//				leftThrust = rightMoment / leftMoment;
//			}
//		}
		leftThrust = rightMoment * rightMoment / (leftMoment * leftMoment + rightMoment * rightMoment);
		rightThrust = 1 - leftThrust;

		// We balance forces of both sides
		// and adjust thrust according to max_accel, max_speed and distanceToTarget
		for (PropulsorMorph m : activePropulsorMorphs) {
			// Adjust thrust for moments
			float thrust = 1;
			if (leftMorphs.contains(m)) {
				thrust = leftThrust;
			} else if (rightMorphs.contains(m)) {
				thrust = rightThrust;
			}

				m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(directionVector));
				m.getPropulsingBehavior().setThrustPercentage(thrust);

			m.activate();
		}

	}

	public boolean done() {

//		float distanceToTarget = ship.pos.distance(targetPos);
//		float shipSpeed = ship.posSpeed.modulus();
//		if (distanceToTarget < 50 && shipSpeed < 0.001) {
//			return true;
//		}
//		// Detect if we are close enough to the target point
//		if (ship.pos.distance(targetPos) < 20 && ship.posSpeed.modulus() < 0.1) {
//			// should only suppress its own forces.
//			ship.ownForceList.clear();
//			return true;
//		}
//
//		// Dectect if all the propulsors are out of energy
//		if (activePropulsorMorphs.size() == 0) {
//			return true;
//		}

		return done;
	}

	public Vect3D getTargetPos() {
		return targetPos;
	}

}
