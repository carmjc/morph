package net.carmgate.morph.model.physics;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.morph.Morph;

public class Force {
	private Morph target;
	private Vect3D vector;

	public Force(Morph target, Vect3D vector) {
		this.target = target;
		this.vector = vector;
	}

	public final Morph getTarget() {
		return target;
	}

	public final Vect3D getVector() {
		return vector;
	}

	public final void setTarget(Morph target) {
		this.target = target;
	}

	public final void setVector(Vect3D vector) {
		this.vector = vector;
	}

	@Override
	public final String toString() {
		return "Force(" + vector + ")";
	}
}