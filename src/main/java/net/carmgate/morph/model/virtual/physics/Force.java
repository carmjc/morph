package net.carmgate.morph.model.virtual.physics;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.morph.Morph;

public class Force {
	private Morph target;
	private Vect3D vector;

	public Force(Morph target, Vect3D vector) {
		this.target = target;
		this.vector = vector;
	}

	public Morph getTarget() {
		return target;
	}

	public Vect3D getVector() {
		return vector;
	}

	public void setTarget(Morph target) {
		this.target = target;
	}

	public void setVector(Vect3D vector) {
		this.vector = vector;
	}
}