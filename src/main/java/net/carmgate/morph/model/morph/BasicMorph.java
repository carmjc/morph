package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

@MorphInfo
public class BasicMorph extends Morph {

	private static final Logger LOGGER = Logger.getLogger(BasicMorph.class);

	public BasicMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		getAlwaysActiveBehaviorList().add(new SpreadingEnergy(this));
	}

	/**
	 * Empty implementation for easy use.
	 */
	@Override
	public void activate() {
	}

	/**
	 * Empty implementation for easy use.
	 */
	@Override
	public void deactivate() {
	}

}
