package net.carmgate.morph.model.morph.old;

import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.ship.Ship;

@Deprecated
public class IAMorph extends BasicMorph {

	public IAMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
	}

	@Override
	public MorphType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
