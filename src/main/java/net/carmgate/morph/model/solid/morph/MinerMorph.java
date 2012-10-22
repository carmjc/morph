package net.carmgate.morph.model.solid.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Mining;
import net.carmgate.morph.model.solid.mattersource.MatterSource;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

@MorphInfo(type = MorphType.MINER, possibleEvolutions = { EvolutionType.TO_BASIC }, maxEnergy = 300)
public class MinerMorph extends BasicMorph implements MassProvider {

	private MatterSource target;

	public MinerMorph() {
		getActivableBehaviorList().add(new Mining(this));
	}

	@Override
	public float getAvailableMass() {
		return Math.max(0, getMass() - getClass().getAnnotation(MorphInfo.class).maxMass());
	}

	public MatterSource getTarget() {
		return target;
	}

	/**
	 * Retrieves the requested mass from the morph.
	 * If there is less mass available than requested, retrieves as much as possible.
	 * If there is no mass available, returns 0 
	 * @param mass the requested mass
	 * @return 0 if nothing is available
	 */
	@Override
	public float retrieveMass(float mass) {
		mass = Math.max(0, Math.min(mass, getMass() - getClass().getAnnotation(MorphInfo.class).maxMass()));
		setMass(getMass() - mass);
		return mass;
	}

	public void setTarget(MatterSource target) {
		this.target = target;

	}

}
