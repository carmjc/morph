package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.ship.Ship;

// FIXME Do not know what a spreader morph is
public class SpreaderMorph extends BasicMorph {

	public SpreaderMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
	}

	@Override
	public float getMaxEnergy() {
		return 0;
	}

	@Override
	public MorphType getType() {
		return MorphType.SPREADER;
	}
}
