package net.carmgate.morph.model.solid.energysource.impl;

import net.carmgate.morph.model.solid.energysource.EnergySource;

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
	public final float getEffectRadius() {
		return effectRadius;
	}

	@Override
	public final float getRadiatedEnergy() {
		return radiatedEnergy;
	}

}
