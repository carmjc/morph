package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.behavior.Stemming;
import net.carmgate.morph.model.ship.Ship;

public class StemMorph extends BasicMorph {

	public StemMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		activableBehaviorList.add(new Stemming(this));
	}

	@Override
	public float getMaxEnergy() {
		return 100;
	}

	@Override
	public MorphType getType() {
		return MorphType.STEM;
	}
}
