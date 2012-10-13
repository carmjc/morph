package net.carmgate.morph.model.solid.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.LaserFiringBehavior;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.model.solid.ship.Ship;

@MorphInfo(type = MorphType.GUN, possibleEvolutions = { EvolutionType.TO_BASIC }, maxEnergy = 300)
public class GunMorph extends BasicMorph {

	private Ship target;

	public GunMorph() {
		getActivableBehaviorList().add(new LaserFiringBehavior(this));
	}

	public Ship getTarget() {
		return target;
	}

	public void setTarget(Ship target) {
		this.target = target;

	}

}
