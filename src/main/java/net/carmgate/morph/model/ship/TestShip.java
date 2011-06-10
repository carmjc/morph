package net.carmgate.morph.model.ship;

import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.morph.EmitterMorph;
import net.carmgate.morph.model.morph.PropulsorMorph;
import net.carmgate.morph.model.morph.ShieldMorph;

public class TestShip extends Ship {

	public TestShip(float x, float y, float z) {
		super(x, y, z);

		addMorph(new BasicMorph(-3, 0, 0));
		addMorph(new BasicMorph(-2, 0, 0));
		addMorph(new PropulsorMorph(-1, 1, 0));
		addMorph(new BasicMorph(-1, 0, 0));
		addMorph(new EmitterMorph(0, 1, 0));
		addMorph(new ShieldMorph(0, 0, 0));
		addMorph(new BasicMorph(0, -1, 0));
		addMorph(new BasicMorph(0, -2, 0));
		addMorph(new EmitterMorph(1, 0, 0));
		addMorph(new EmitterMorph(1, -1, 0));
		addMorph(new PropulsorMorph(2, 0, 0));

		addMorph(new PropulsorMorph(-4, 0, 0));

	}

}
