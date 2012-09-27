package net.carmgate.morph.model.solid.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

import org.apache.log4j.Logger;

/**
 * Base class for all real physical morphs.
 * It hads some generic behaviors.
 */
@MorphInfo(possibleEvolutions = { MorphType.PROPULSOR, MorphType.STEM_MORPH })
public class BasicMorph extends Morph {

	private static final Logger LOGGER = Logger.getLogger(BasicMorph.class);

	public BasicMorph() {
		getAlwaysActiveBehaviorList().add(new SpreadingEnergy(this));
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
