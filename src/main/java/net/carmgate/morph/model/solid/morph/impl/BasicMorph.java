package net.carmgate.morph.model.solid.morph.impl;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.impl.morph.DiffusingEnergy;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

import org.apache.log4j.Logger;

/**
 * Base class for all real physical morphs.
 * It hads some generic behaviors.
 */
@MorphInfo(possibleEvolutions = { EvolutionType.TO_PROPULSOR, EvolutionType.TO_STEM, EvolutionType.TO_GUN, EvolutionType.TO_MINER })
public class BasicMorph extends Morph {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(BasicMorph.class);

	public BasicMorph() {
		getAlwaysActiveBehaviorList().add(new DiffusingEnergy(this));
	}

	/**
	 * Does nothing
	 * @return true
	 */
	@Override
	public boolean activate() {
		return true;
	}

	/**
	 * Does nothing
	 * @return true by default
	 */
	@Override
	public boolean deactivate() {
		return true;
	}

}
