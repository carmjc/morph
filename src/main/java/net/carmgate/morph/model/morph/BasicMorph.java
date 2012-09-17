package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.behavior.SpreadingEnergy;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

public class BasicMorph extends Morph {

	private static final Logger LOGGER = Logger.getLogger(BasicMorph.class);

	public BasicMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		getAlwaysActiveBehaviorList().add(new SpreadingEnergy(this));
	}

	@Override
	public boolean activable() {
		// can not activate if energy insufficient
		if (getEnergy() <= 0) {
			LOGGER.debug("no more energy");
			setDisabled(true);
			return false;
		}

		return true;
	}

	@Override
	public void afterActivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterDeactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeActivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDeactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean deactivable() {
		return true;
	}

	@Override
	public float getMaxEnergy() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public MorphType getType() {
		return MorphType.BASIC;
	}

}
