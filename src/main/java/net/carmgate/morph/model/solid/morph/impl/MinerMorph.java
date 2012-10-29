package net.carmgate.morph.model.solid.morph.impl;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.impl.morph.Mining;
import net.carmgate.morph.model.solid.TargettingSupport;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

@MorphInfo(possibleEvolutions = { EvolutionType.TO_BASIC }, maxEnergy = 300)
// type = MorphType.MINER,
public class MinerMorph extends BasicMorph implements TargettingSupport<MatterSource> {

	private MatterSource target;

	public MinerMorph() {
		getActivationLinkedBehaviorList().add(new Mining(this));
	}

	@Override
	public final MatterSource getTarget() {
		return target;
	}

	@Override
	public final void setTarget(MatterSource target) {
		this.target = target;

	}

}
