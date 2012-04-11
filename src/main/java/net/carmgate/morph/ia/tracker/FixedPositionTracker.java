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

		logger.debug(activePropulsorMorphs);

		for (PropulsorMorph m : activePropulsorMorphs) {
			Vect3D direction = new Vect3D(targetPos);
			dummyVect.copy(ship.getCenterOfMassInShip());
			ship.transformShipToWorldCoords(dummyVect);
			direction.substract(dummyVect);
			m.setRotInWorld(new Vect3D(0, -1, 0).angleWith(direction));
			m.activate();
		}

	}

	public boolean done() {
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
