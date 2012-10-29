package net.carmgate.morph.model.solid.energysource;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.WorldPositionSupport;
import net.carmgate.morph.model.solid.WorldSolid;

/**
 * An energy source outputs energy.
 * That energy can be used by morph to replenish their energy.
 */
public abstract class EnergySource extends WorldSolid implements WorldPositionSupport {

	protected final Vect3D pos = new Vect3D();

	/**
	 * Creates a new energy source.
	 * @param x the x position in world coordinates.
	 * @param y the y position in world coordinates.
	 * @param z the z position in world coordinates.
	 */
	public EnergySource(float x, float y, float z) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
	}

	/**
	 * @return the max radius of effect of the energy source.
	 * Outside of that radius, this is as if it did not exist.
	 */
	public abstract float getEffectRadius();

	/**
	 * @return the position of the energy source.
	 */
	@Override
	public final Vect3D getPos() {
		return pos;
	}

	/**
	 * This produces the amount of energy radiated by the energy source.
	 * <p><b>TODO</b> This could be modified to use a position in order to handle anisotropic
	 * energy source</p>
	 * @return the amount of energy radiated by the energy source.
	 */
	public abstract float getRadiatedEnergy();

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
