package net.carmgate.morph.model.requirements;

import net.carmgate.morph.model.physic.morph.Morph;

import org.apache.log4j.Logger;

/**
 * Checks that a morph has more than a given mass.
 */
public class EnoughMass implements Requirement {

	private static final Logger LOGGER = Logger.getLogger(EnoughMass.class);

	private int minMass;
	private Morph morph;

	public EnoughMass(Morph morph, int minMass) {
		this.morph = morph;
		this.minMass = minMass;
	}

	public boolean check() {
		return morph.getMass() >= minMass;
	}

}
