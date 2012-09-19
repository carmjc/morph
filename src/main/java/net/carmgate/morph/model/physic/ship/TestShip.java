package net.carmgate.morph.model.physic.ship;

import net.carmgate.morph.model.physic.morph.BasicMorph;
import net.carmgate.morph.model.physic.morph.prop.PropulsorMorph;
import net.carmgate.morph.model.physic.morph.stem.StemMorph;

public class TestShip extends Ship {

	/**
	 * Creating a new ship with the given coordinates
	 * @param x
	 * @param y
	 * @param z
	 */
	public TestShip(float x, float y, float z) {
		super(x, y, z);

		addMorph(new PropulsorMorph(this, -1, 1, 0));
		addMorph(new PropulsorMorph(this, -1, 0, 0));
		addMorph(new BasicMorph(this, 0, 1, 0));
		addMorph(new StemMorph(this, 0, 0, 0));

	}

}