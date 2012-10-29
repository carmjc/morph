package net.carmgate.morph.model.requirements.impl;

import net.carmgate.morph.model.requirements.Requirement;
import net.carmgate.morph.model.solid.morph.Morph;

import org.apache.log4j.Logger;

/**
 * Checks that a morph has more than a given mass.
 */
public class EnoughMass implements Requirement {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(EnoughMass.class);

	private int minMass;
	private Morph morph;

	public EnoughMass(Morph morph, int minMass) {
		this.morph = morph;
		this.minMass = minMass;
	}

	@Override
	public boolean check() {
		return morph.getMass() >= minMass;
	}

}
