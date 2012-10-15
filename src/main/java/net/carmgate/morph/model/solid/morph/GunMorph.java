package net.carmgate.morph.model.solid.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.LaserFiringBehavior;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

@MorphInfo(type = MorphType.GUN, possibleEvolutions = { EvolutionType.TO_BASIC }, maxEnergy = 300)
public class GunMorph extends BasicMorph {

	private Morph target;

	public GunMorph() {
		getActivableBehaviorList().add(new LaserFiringBehavior(this));
	}

	public Morph getTarget() {
		return target;
	}

	public void setTarget(Morph target) {
		this.target = target;

	}

}
