package net.carmgate.morph.model.solid.ship.test;

import net.carmgate.morph.model.solid.morph.GunMorph;
import net.carmgate.morph.model.solid.morph.prop.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.stem.StemMorph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.user.User;

public class TestShip extends Ship {

	/**
	 * Creating a new ship with the given coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @param user 
	 */
	public TestShip(float x, float y, float z, User user) {
		super(x, y, z, user);

		addMorph(new GunMorph(), -1, 1, 0);
		addMorph(new PropulsorMorph(), -1, 0, 0);
		addMorph(new StemMorph(), 0, 1, 0);
		addMorph(new StemMorph(), 0, 0, 0);
		addMorph(new PropulsorMorph(), 1, 0, 0);

		// addMorph(new GunMorph(), 10, 0, 0);
	}

}
