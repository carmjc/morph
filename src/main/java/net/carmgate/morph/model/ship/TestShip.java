package net.carmgate.morph.model.ship;

import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.morph.EmitterMorph;
import net.carmgate.morph.model.morph.PropulsorMorph;
import net.carmgate.morph.model.morph.ShieldMorph;

public class TestShip extends Ship {

	/**
	 * Creating a new ship with the given coordinates
	 * @param x
	 * @param y
	 * @param z
	 */
	public TestShip(float x, float y, float z) {
		super(x, y, z);

		addMorph(new BasicMorph(this, -3, 0, 0));
		addMorph(new BasicMorph(this, -2, 0, 0));
		addMorph(new PropulsorMorph(this, -1, 1, 0));
		addMorph(new BasicMorph(this, -1, 0, 0));
		addMorph(new EmitterMorph(this, 0, 1, 0));
		addMorph(new ShieldMorph(this, 0, 0, 0));
		addMorph(new BasicMorph(this, 0, -1, 0));
		addMorph(new BasicMorph(this, 0, -2, 0));
		addMorph(new EmitterMorph(this, 1, 0, 0));
		addMorph(new EmitterMorph(this, 1, -1, 0));
		addMorph(new PropulsorMorph(this, 2, 0, 0));

		addMorph(new PropulsorMorph(this, -4, 0, 0));

	}

}
