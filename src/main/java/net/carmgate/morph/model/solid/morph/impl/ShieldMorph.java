package net.carmgate.morph.model.solid.morph.impl;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

import org.apache.log4j.Logger;

/**
 * Shield morph.
 * It looses energy to avoid the other morphs getting too much.
 * As long as it has energy, the other morphs are protected from energy overflow.
 */
@MorphInfo(possibleEvolutions = { EvolutionType.TO_BASIC })
public class ShieldMorph extends BasicMorph {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ShieldMorph.class);

}
