package net.carmgate.morph.model.behavior.impl.ship;

import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.solid.energysource.EnergySource;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

/**
 * This ship behavior processes energy sources and updates the ship's morph accordingly.
 */
@BehaviorInfo(alwaysActive = true)
public class GettingEnergyFromSources extends Behavior<Ship> {

	public GettingEnergyFromSources(Ship owner) {
		super(owner);
	}

	public GettingEnergyFromSources(Ship owner, State initialState) {
		super(owner, initialState);
	}

	@Override
	protected void execute() {
		for (EnergySource energySource : World.getWorld().getEnergySources().values()) {
			double energyGainedPerMorph = 0;
			float distance = getOwner().getPos().distance(energySource.getPos());

			// A distance of 1000 pixels to the star causes the star to give no energy to the ship
			// The greater the ship, the more energy it gets
			// The greater the star, the more energy it emits
			double minimumActingDistanceSquared = energySource.getEffectRadius() * energySource.getEffectRadius();
			double distanceSquared = distance * distance;

			if (distanceSquared < minimumActingDistanceSquared) {
				energyGainedPerMorph = (minimumActingDistanceSquared - distanceSquared) / minimumActingDistanceSquared
						* energySource.getRadiatedEnergy()
						* World.getWorld().getSinceLastUpdateTS() / 1000;

				for (Morph m : getOwner().getMorphsByIds().values()) {
					// if the morph is not virtual (selection shadows for instance)
					// then we update it's energy
					if (!m.getClass().getAnnotation(MorphInfo.class).virtual()) {
						// augment morph energy
						m.setEnergy((float) (m.getEnergy() + energyGainedPerMorph));

					}
				}
			}
		}
	}

}
