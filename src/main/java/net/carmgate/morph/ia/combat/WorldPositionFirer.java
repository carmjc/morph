package net.carmgate.morph.ia.combat;

import net.carmgate.morph.ia.IA;
import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.old.EmitterMorph;
import net.carmgate.morph.model.ship.Ship;

@Deprecated
public class WorldPositionFirer implements IA {

	private final Ship firerShip;
	private final Vect3D target;

	public WorldPositionFirer(Ship firerShip, Vect3D target) {
		this.firerShip = firerShip;
		this.target = target;
		for (Morph morph : firerShip.getMorphs().values()) {
			if (morph instanceof EmitterMorph) {
				for (Behavior<?> behavior : morph.getActivableBehaviorList()) {
					if (behavior instanceof Emitting) {
						Emitting emitting = (Emitting) behavior;
						emitting.setTarget(new Vect3D(target));
						emitting.tryToActivate();
					}
				}
			}
		}
	}

	public void compute() {
	}

	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
