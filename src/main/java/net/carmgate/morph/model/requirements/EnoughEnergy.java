package net.carmgate.morph.model.requirements;

import net.carmgate.morph.model.solid.morph.Morph;

import org.apache.log4j.Logger;

/**
 * Checks that a morph has more than a given energy.
 */
public class EnoughEnergy implements Requirement {

	private static final Logger LOGGER = Logger.getLogger(EnoughEnergy.class);

	private int minEnergy;
	private Morph morph;

	public EnoughEnergy(Morph morph, int minEnergy) {
		this.morph = morph;
		this.minEnergy = minEnergy;
	}

	public boolean check() {
		return morph.getEnergy() >= minEnergy;
	}

}
