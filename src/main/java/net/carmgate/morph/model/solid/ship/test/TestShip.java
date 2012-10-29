package net.carmgate.morph.model.solid.ship.test;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.impl.BasicMorph;
import net.carmgate.morph.model.solid.morph.impl.GunMorph;
import net.carmgate.morph.model.solid.morph.impl.MinerMorph;
import net.carmgate.morph.model.solid.morph.impl.PropulsorMorph;
import net.carmgate.morph.model.solid.morph.impl.stem.StemMorph;
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

		addMorph(new GunMorph(), new Vect3D(-1, 1, 0));
		addMorph(new PropulsorMorph(), new Vect3D(-1, 0, 0));
		addMorph(new BasicMorph(), new Vect3D(0, 1, 0));
		addMorph(new StemMorph(), new Vect3D(0, 0, 0));
		addMorph(new PropulsorMorph(), new Vect3D(1, 0, 0));
		addMorph(new MinerMorph(), new Vect3D(1, 1, 0));

		addMorph(new GunMorph(), new Vect3D(10, 0, 0));
	}

}
