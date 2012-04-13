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
	private static Vect3D dummyVect = new Vect3D();

	public FixedPositionTracker(Ship ship, Vect3D targetPos) {
		this.ship = ship;
		this.targetPos = targetPos;

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

		// Calculate left props and right props considering the direction vector
		// The direction vector points from the center of mass in the direction of the targetPos
		// To evaluate which side is each propulsor morph, it suffices to evaluate the dot product
		//  between the direction vector rotated -90° and the position vector of the morph into the ship.
		Vect3D directionVector = new Vect3D(targetPos);
		directionVector.substract(comInWorld);
		directionVector.rotate(-90);

		List<PropulsorMorph> leftMorphs = new ArrayList<PropulsorMorph>();
		List<PropulsorMorph> rightMorphs = new ArrayList<PropulsorMorph>();
		Vect3D v = new Vect3D();
		for (PropulsorMorph m : activePropulsorMorphs) {
			v.copy(m.getPosInWorld());
			v.substract(comInWorld);
			float prodScal = directionVector.prodScal(v);
			if (prodScal > 0) {
				leftMorphs.add(m);
			} else if (prodScal < 0) {
				rightMorphs.add(m);
			}
		}

		// Recalculate direction Vector
		directionVector.copy(targetPos);
		directionVector.substract(comInWorld);

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

		float shipAccel = ship.posAccel.modulus();
		float shipSpeed = ship.posSpeed.modulus();
		float distanceToTarget = ship.pos.distance(targetPos);
		logger.debug(shipAccel + ", " + shipSpeed + ", " + distanceToTarget + ", " + ship.getCenterOfMassInShip().modulus());

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

			if (distanceToTarget <= shipSpeed) {
				m.setRotInWorld(180 + new Vect3D(0, -1, 0).angleWith(directionVector));
				if (shipSpeed / MAX_SPEED > distanceToTarget / CAP_DISTANCE) {
					m.getPropulsingBehavior().setThrustPercentage(thrust);
				}
				m.getPropulsingBehavior().setThrustPercentage(0);
			} else {
				m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(directionVector));
				m.getPropulsingBehavior().setThrustPercentage(thrust);
			}

			m.activate();
		}

	}

	public boolean done() {

		float distanceToTarget = ship.pos.distance(targetPos);
		float shipSpeed = ship.posSpeed.modulus();
		if (distanceToTarget < 50 && shipSpeed < 0.001) {
			return true;
		}
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

		return false;
	}

	public Vect3D getTargetPos() {
		return targetPos;
	}

}
