package net.carmgate.morph.model.physic.morph.old;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.old.Emitting;
import net.carmgate.morph.model.physic.morph.BasicMorph;
import net.carmgate.morph.model.physic.morph.Morph.MorphType;
import net.carmgate.morph.model.physic.ship.Ship;

@Deprecated
@MorphInfo(type = MorphType.EMITTER)
public class EmitterMorph extends BasicMorph {

	public EmitterMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		getActivableBehaviorList().add(new Emitting(this, 1, 0, null));
	}

}
