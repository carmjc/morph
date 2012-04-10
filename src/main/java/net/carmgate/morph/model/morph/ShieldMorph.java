package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.ship.Ship;

public class ShieldMorph extends BasicMorph {

	public ShieldMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
	}

	@Override
	public float getMaxEnergy() {
		return 1000;
	}

	@Override
	public MorphType getType() {
		return MorphType.SHIELD;
	}
}
