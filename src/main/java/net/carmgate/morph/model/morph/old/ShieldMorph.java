package net.carmgate.morph.model.morph.old;

import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.morph.Morph;
import net.carmgate.morph.model.morph.Morph.MorphType;
import net.carmgate.morph.model.ship.Ship;

@Deprecated
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
