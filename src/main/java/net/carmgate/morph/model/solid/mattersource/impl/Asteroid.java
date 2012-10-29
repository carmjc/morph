package net.carmgate.morph.model.solid.mattersource.impl;

import net.carmgate.morph.model.solid.mattersource.MatterSource;

public class Asteroid extends MatterSource {

	public static enum AsteroidType {
		TYPE_0,
		TYPE_1,
		TYPE_2,
		TYPE_3;
	}

	public Asteroid(float x, float y, float z, int mass) {
		super(x, y, z, (float) (Math.random() * 20 - 10), mass);
	}

}
