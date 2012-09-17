package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Stemming;
import net.carmgate.morph.model.morph.Morph.MorphType;
import net.carmgate.morph.model.ship.Ship;

@MorphInfo(type = MorphType.STEM_MORPH, initialMass = 50)
public class StemMorph extends BasicMorph {

	public StemMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		getActivableBehaviorList().add(new Stemming(this));
	}

}
