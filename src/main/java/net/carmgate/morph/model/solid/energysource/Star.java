package net.carmgate.morph.model.solid.energysource;

import net.carmgate.morph.model.user.User;

/**
 * A start is a fixed source of energy.
 * It outputs a constant stream of energy in an isotropic fashion.
 */
public class Star extends EnergySource {

	private float radiatedEnergy;
	private float effectRadius;
	private User user;

	public Star(float x, float y, float z, float radiatedEnergy, float effectRadius, User user) {
		super(x, y, z);
		this.radiatedEnergy = radiatedEnergy;
		this.effectRadius = effectRadius;
		this.user = user;
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
