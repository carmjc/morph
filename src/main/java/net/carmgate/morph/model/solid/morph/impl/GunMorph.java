package net.carmgate.morph.model.solid.morph.impl;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.impl.morph.LaserFiring;
import net.carmgate.morph.model.solid.TargettingSupport;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

@MorphInfo(possibleEvolutions = { EvolutionType.TO_BASIC }, maxEnergy = 300)
// type = MorphType.GUN,
public class GunMorph extends BasicMorph implements TargettingSupport<Morph> {

	private Morph target;

	public GunMorph() {
		getActivationLinkedBehaviorList().add(new LaserFiring(this));
	}

	@Override
	public boolean canBeActivated() {
		return super.canBeActivated() && target.getPosInWorld().distance(getPosInWorld()) < ModelConstants.MAX_FIRING_DISTANCE;
	}

	@Override
	public final Morph getTarget() {
		return target;
	}

	@Override
	public final void setTarget(Morph target) {
		this.target = target;

	}

}
