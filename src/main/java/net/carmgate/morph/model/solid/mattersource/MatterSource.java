package net.carmgate.morph.model.solid.mattersource;

import net.carmgate.morph.model.Vect3D;

public abstract class MatterSource {

	private static int lastId = 0;
	private int id = ++lastId;

	protected final Vect3D pos = new Vect3D();
	private float rotationSpeed;
	private int mass;
	private final int initialMass;

	public MatterSource(float x, float y, float z, float rotationSpeed, int mass) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
		this.rotationSpeed = rotationSpeed;
		this.mass = mass;
		initialMass = mass;
	}

	/**
	 * @return the id of the source. This is a unique Id for all sources.
	 */
	public int getId() {
		return id;
	}

	public int getInitialMass() {
		return initialMass;
	}

	public int getMass() {
		return mass;
	}

	public Vect3D getPos() {
		return pos;
	}

	public float getRotationSpeed() {
		return rotationSpeed;
	}

	public void setMass(int mass) {
		this.mass = mass;
	}

}
