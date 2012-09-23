package net.carmgate.morph.model.solid.energysource;

import net.carmgate.morph.model.Vect3D;

/**
 * An energy source outputs energy.
 * That energy can be used by morph to replenish their energy.
 */
public abstract class EnergySource {

	private static int lastId = 0;
	private int id = ++lastId;
	protected final Vect3D pos = new Vect3D();

	public EnergySource(float x, float y, float z) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
	}

	public abstract float getEffectRadius();

	/**
	 * @return the id of the source. This is a unique Id for all sources.
	 */
	public int getId() {
		return id;
	}

	public Vect3D getPos() {
		return pos;
	}

	public abstract float getRadiatedEnergy();
}
