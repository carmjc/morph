package net.carmgate.morph.model.solid.energysource;

/**
 * A start is a fixed source of energy.
 * It outputs a constant stream of energy in an isotropic fashion.
 */
public class Star extends EnergySource {

	private float radiatedEnergy;
	private float effectRadius;

	public Star(float x, float y, float z, float radiatedEnergy, float effectRadius) {
		super(x, y, z);
		this.radiatedEnergy = radiatedEnergy;
		this.effectRadius = effectRadius;
	}

	@Override
	public float getEffectRadius() {
		return effectRadius;
	}

	@Override
	public float getRadiatedEnergy() {
		return radiatedEnergy;
	}

}
