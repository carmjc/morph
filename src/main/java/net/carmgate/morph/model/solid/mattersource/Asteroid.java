package net.carmgate.morph.model.solid.mattersource;

public class Asteroid extends MatterSource {

	public static enum AsteroidType {
		TYPE_0,
		TYPE_1,
		TYPE_2,
		TYPE_3;
	}

	private AsteroidType type;

	public Asteroid(float x, float y, float z, AsteroidType type, int mass) {
		super(x, y, z, (float) (Math.random() * 20 - 10), mass);
		this.type = type;
	}

}
