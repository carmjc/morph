package net.carmgate.morph.model.morph.old;

import net.carmgate.morph.model.behavior.Emitting;
import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.Morph.MorphType;
import net.carmgate.morph.model.ship.Ship;

@Deprecated
public class EmitterMorph extends BasicMorph {

	public EmitterMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		activableBehaviorList.add(new Emitting(this, 1, 0, null));
	}

	@Override
	public float getMaxEnergy() {
		return 100;
	}

	@Override
	public MorphType getType() {
		return MorphType.EMITTER;
	}
}
