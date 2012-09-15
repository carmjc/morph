package net.carmgate.morph.model.virtual.physics;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.morph.Morph;

public class Force {
	public Morph target;
	public Vect3D vector;

	public Force(Morph target, Vect3D vector) {
		this.target = target;
		this.vector = vector;
	}
}