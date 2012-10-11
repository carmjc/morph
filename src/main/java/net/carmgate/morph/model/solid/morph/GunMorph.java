package net.carmgate.morph.model.solid.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

@MorphInfo(type = MorphType.GUN, possibleEvolutions = { EvolutionType.TO_BASIC })
public class GunMorph extends BasicMorph {

	public GunMorph() {
	}

}
