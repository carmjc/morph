package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.morph.Morph;

public class SpreadingEnergy extends NoActivationBehavior<Morph> {

	private static final float ENERGY_TRANSFER_PER_SEC = .02f;

	public SpreadingEnergy(Morph owner) {
		super(owner);
	}

	@Override
	protected void execute() {
		for (Morph neighbour : getOwner().getNeighbours()) {
			if (neighbour != null && neighbour.energy / neighbour.getMaxEnergy() < getOwner().energy / getOwner().getMaxEnergy()) {
				neighbour.energy += ENERGY_TRANSFER_PER_SEC;
				getOwner().energy -= ENERGY_TRANSFER_PER_SEC;
			}
		}
	}

}
