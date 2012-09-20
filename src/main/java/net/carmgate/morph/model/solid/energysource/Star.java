package net.carmgate.morph.model.solid.energysource;

import net.carmgate.morph.model.Vect3D;

/**
 * A start is a fixed source of energy.
 * It outputs a constant stream of energy in an isotropic fashion.
 */
public class Star extends EnergySource {

	private final Vect3D pos = new Vect3D();

	public Star(float x, float y, float z) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
	}

	public Vect3D getPos() {
		return pos;
	}

}
