package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

import org.apache.log4j.Logger;

public class TransformingBehavior extends Behavior<Morph> {

	private static final Logger LOGGER = Logger.getLogger(TransformingBehavior.class);

	private MorphType newType;

	public TransformingBehavior(Morph owner, MorphType newType) {
		super(owner, State.ACTIVE);
		this.newType = newType;
	}

	@Override
	protected void execute() {
		LOGGER.debug("Transforming ...");
		try {
			Morph newMorph = newType.getMorphClass().newInstance();
			getOwner().getShip().addMorph(newMorph, getOwner().getPosInShipGrid().x, getOwner().getPosInShipGrid().y, getOwner().getPosInShipGrid().z, true);
		} catch (InstantiationException e) {
			LOGGER.error("Error while creating new morph", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Error while creating new morph", e);
		}
		tryToDeactivate();
	}
}
