package net.carmgate.morph.model.solid.mattersource;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.WorldPositionSupport;
import net.carmgate.morph.model.solid.WorldSolid;

public abstract class MatterSource extends WorldSolid implements WorldPositionSupport {

	protected final Vect3D pos = new Vect3D();
	private float rotationSpeed;
	private float mass;
	private final int initialMass;

	public MatterSource(float x, float y, float z, float rotationSpeed, int mass) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
		this.rotationSpeed = rotationSpeed;
		this.mass = mass;
		initialMass = mass;
	}

	public int getInitialMass() {
		return initialMass;
	}

	public float getMass() {
		return mass;
	}

	@Override
	public Vect3D getPos() {
		return pos;
	}

	public float getRotationSpeed() {
		return rotationSpeed;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public float getRotSpeed() {
		return 0;
	}

	public float getRotAccel() {
		return 0;
	}

	public float getRot() {
		return 0;
	}

}
