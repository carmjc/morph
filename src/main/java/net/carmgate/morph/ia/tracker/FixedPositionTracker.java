package net.carmgate.morph.ia.tracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.PropulsorMorph;
import net.carmgate.morph.model.ship.Ship;

public class FixedPositionTracker implements IA, Cloneable {

	private final Vect3D targetPos;
	private final List<PropulsorMorph> propulsorMorphs = new ArrayList<PropulsorMorph>();
	private final List<PropulsorMorph> activePropulsorMorphs = new ArrayList<PropulsorMorph>();
	private final Ship ship;

	public FixedPositionTracker(Ship ship, Vect3D targetPos) {

		this.ship = ship;
		this.targetPos = targetPos;

		// extract every morph that can make the ship move.
		for (Morph m : ship.getMorphList()) {
			if (m instanceof PropulsorMorph) {
				propulsorMorphs.add((PropulsorMorph) m);
			}
		}

		for (PropulsorMorph m : propulsorMorphs) {
			if (!m.disabled) {
				activePropulsorMorphs.add(m);
			}
		}
	}

	private void accelerate(Vect3D forceSum, Vect3D targetSum) {
		// Reorient all engines towards the target (minus the position of the engine in the ship)
		for (Morph m : activePropulsorMorphs) {
			Vect3D tmpVect = new Vect3D(targetPos);
			tmpVect.substract(ship.pos);
			m.rot.z = - m.ship.rot.z + new Vect3D(1, 0, 0).angleWith(tmpVect);
		}



		// Normalize the vector to 1/nth of the it's modulus (n being the number of vectors combined in the sum.
		//		targetSum.normalize(targetSum.modulus() / propulsorMorphs.size());
		//		targetSum.add(ship.pos);

		// Calculating a pseudo propulsor equivalent to the propulsing forces applied to the ship.
		//		PropulsorMorph target = new PropulsorMorph(0, 0, 0);
		//		target.pos.x = targetSum.x;
		//		target.pos.y = targetSum.y;
		//		target.pos.z = targetSum.z;
		//		target.ship = ship;
		//		if (ship.debugForce == null) {
		//			ship.debugForce = new Force(target, forceSum);
		//			World.getWorld().getForceList().add(ship.debugForce);
		//		} else {
		//			ship.debugForce.target = target;
		//			ship.debugForce.vector = forceSum;
		//		}
		//		targetSum.substract(virtualShip.pos);
	}

	//	private void brake(Vect3D forceSum, Vect3D targetSum) {
	//		// Reorient all engines towards the target (minus the position of the engine in the ship)
	//		for (Morph m : propulsorMorphs) {
	//			Vect3D tmpVect = new Vect3D(targetPos);
	//			tmpVect.substract(ship.pos);
	//			m.rot.z = 180 - m.ship.rot.z + new Vect3D(1, 0, 0).angleWith(tmpVect);
	//		}
	//	}

	public void compute() {
		// update the list of active propulsors
		activePropulsorMorphs.clear();
		for (PropulsorMorph m : propulsorMorphs) {
			if (!m.disabled) {
				activePropulsorMorphs.add(m);
			}
		}

		// should only suppress its own forces.
		ship.ownForceList.clear();

		Vect3D forceSum = new Vect3D(0, 0, 0);
		Vect3D targetSum = new Vect3D(0, 0, 0);
		for (Iterator<PropulsorMorph> i = activePropulsorMorphs.iterator(); i.hasNext(); ) {
			PropulsorMorph m = i.next();

			if (i.hasNext()) {
				// where does the vector sum points to
				Vect3D vector = new Vect3D(m.getGeneratedForce().vector);
				vector.rotateZ(m.rot.z);
				forceSum.add(vector);

				// What is the application point of the sum of the forces
				targetSum.add(m.getGeneratedForce().target.pos);
				targetSum.substract(ship.pos);
			}
		}

		// is it needed to accelerate or brake ?
		// it s needed to break if the distance necessary to stop with reverse thrust is the current distance to target
		// it should be independent from frame rate ... however, for now it won't be ...
		//		float thrustFactor = 1;
		//		float distanceLeft = targetPos.distance(ship.pos);
		//		float shipSpeed = ship.posSpeed.modulus();
		//		float shipAccel = ship.posAccel.modulus();
		//		float timeLeft = (float) ((-shipSpeed + Math.sqrt(Math.pow(shipSpeed, 2) + 2 * shipAccel * distanceLeft)) / shipAccel);
		//		System.out.println("forceSum: " + forceSum.modulus() + ", " + shipSpeed);
		//		double distanceToBreak = shipSpeed * timeLeft - 1f / 2 * forceSum.modulus() * Math.pow(timeLeft, 2);
		//		System.out.println("distanceLeft: " + distanceLeft + ", timeLeft: " + timeLeft + ", distanceToBreak: " + distanceToBreak);
		//		if (distanceToBreak > distanceLeft && ship.pos.distance(targetPos) > 150) {
		//			accelerate(forceSum, targetSum);
		//		} else if (ship.pos.distance(targetPos) > 100) {
		//			brake(forceSum, targetSum);
		//		} else {
		//			accelerate(forceSum, targetSum);
		//			thrustFactor = ship.pos.distance(targetPos) / 500;
		//		}

		// adjust thrust factor
		// if we are near the target, we should adjust the thrust factor
		float thrustFactor = 1;
		if (ship.pos.distance(targetPos) > 1000) {
			thrustFactor = 1;
		} else {
			thrustFactor = ship.pos.distance(targetPos) / 1000;
		}
		accelerate(forceSum, targetSum);

		for (Iterator<PropulsorMorph> i = activePropulsorMorphs.iterator(); i.hasNext(); ) {
			PropulsorMorph m = i.next();

			if (i.hasNext() || activePropulsorMorphs.size() <= 2) {
				if (!m.disabled) {
					m.setThrustFactor(thrustFactor);
					m.activate();
				}
			} else {
				if (!m.disabled) {
					Vect3D counterVector = new Vect3D(-forceSum.x, -forceSum.y, -forceSum.z);
					m.rot.z = new Vect3D(1, 0, 0).angleWith(counterVector);
					m.setThrustFactor(thrustFactor);
					m.activate();
				}
			}
		}

	}

	public boolean done() {
		// Detect if we are close enough to the target point
		if (ship.pos.distance(targetPos) < 20 && ship.posSpeed.modulus() < 0.1) {
			// should only suppress its own forces.
			ship.ownForceList.clear();
			return true;
		}

		// Dectect if all the propulsors are out of energy
		if (activePropulsorMorphs.size() == 0) {
			return true;
		}

		return false;
	}

}
