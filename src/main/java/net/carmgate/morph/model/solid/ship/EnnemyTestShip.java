package net.carmgate.morph.model.solid.ship;

import net.carmgate.morph.model.solid.morph.BasicMorph;
import net.carmgate.morph.model.user.User;

public class EnnemyTestShip extends Ship {

	/**
	 * Creating a new ship with the given coordinates
	 * @param x
	 * @param y
	 * @param z
	 * @param user 
	 */
	public EnnemyTestShip(float x, float y, float z, User user) {
		super(x, y, z, user);

		addMorph(new BasicMorph(), 0, 0, 0);
		// addMorph(new BasicMorph(), 1, 0, 0);
		// addMorph(new BasicMorph(), -1, 0, 0);

	}

}
