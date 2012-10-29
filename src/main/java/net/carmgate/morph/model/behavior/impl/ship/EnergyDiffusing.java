package net.carmgate.morph.model.behavior.impl.ship;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

/**
 * This ship behavior handles energy diffusion for ship's morphs.
 * It also handles mass loss in case of energy overflow.
 */
@BehaviorInfo(alwaysActive = true)
public class EnergyDiffusing extends Behavior<Ship> {

	private static final Logger LOGGER = Logger.getLogger(EnergyDiffusing.class);

	public EnergyDiffusing(Ship owner) {
		super(owner);
	}

	public EnergyDiffusing(Ship owner, State initialState) {
		super(owner, initialState);
	}

	@Override
	protected void execute() {
		for (Morph m : getOwner().getMorphsByIds().values()) {
			// if there is too much excess of energy, the morph will loose a portion of its mass
			// proportional to the amount of energy above sustainable amount of energy in excess
			float sustainableExcessEnergy = m.getClass().getAnnotation(MorphInfo.class).maxEnergy()
					* ModelConstants.MAX_EXCEED_ENERGY_AS_RATIO_OF_MAX_MORPH_ENERGY;
			if (m.getExcessEnergy() > sustainableExcessEnergy) {
				float energyAboveSustainable = m.getExcessEnergy() - sustainableExcessEnergy;
				float lostMass = energyAboveSustainable * ModelConstants.MASS_LOSS_TO_EXCESS_ENERGY_RATIO;
				m.setMass(m.getMass() - lostMass);
				LOGGER.trace("Lost mass: " + lostMass + " - lost mass / s: " + lostMass * 1000 / World.getWorld().getSinceLastUpdateTS());
				m.setEnergy(m.getEnergy() - energyAboveSustainable);
			}

		}
	}

}
