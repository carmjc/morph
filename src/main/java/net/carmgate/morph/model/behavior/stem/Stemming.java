package net.carmgate.morph.model.behavior.stem;

import net.carmgate.morph.model.ModelConstants;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.solid.morph.BasicMorph;
import net.carmgate.morph.model.solid.morph.stem.StemMorph;
import net.carmgate.morph.model.solid.morph.stem.StemmingSelectionShadow;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

public class Stemming extends Behavior<StemMorph> {

	private static final Logger LOGGER = Logger.getLogger(Stemming.class);

	private StemmingSelectionShadow stemmingSelectedShadow;

	private long creationTS;

	public Stemming(StemMorph owner) {
		super(owner);
	}

	@Override
	protected boolean activate() {
		// This behavior cannot be activated if the stem morph is not at max mass.
		if (getOwner().getMass() < getOwner().getClass().getAnnotation(MorphInfo.class).maxMass()) {
			LOGGER.debug("Could not activate stemming. Insufficient mass.");
			return false;
		}

		return true;
	}

	@Override
	protected boolean deactivate(boolean forced) {
		stemmingSelectedShadow = null;
		return true;
	}

	@Override
	protected void execute() {
		if (stemmingSelectedShadow != null) {
			creationTS = World.getWorld().getCurrentTS() + ModelConstants.CREATION_TIME;
		}

		if (stemmingSelectedShadow != null && World.getWorld().getCurrentTS() >= creationTS) {
			// Create the new morph
			BasicMorph newMorph = new BasicMorph();

			// Transfer mass from stem morph to new morph
			float lossFactor = 5;
			float newMorphMass = Math.max(getOwner().getMass() * 0.95f / lossFactor, newMorph.getClass().getAnnotation(MorphInfo.class).maxMass() * 0.1f);
			newMorph.setMass(newMorphMass);
			getOwner().setMass(getOwner().getMass() - lossFactor * newMorphMass);

			// Add new morph to ship
			getOwner().getShip().addMorph(newMorph, stemmingSelectedShadow.getPosInShipGrid().x, stemmingSelectedShadow.getPosInShipGrid().y,
					stemmingSelectedShadow.getPosInShipGrid().z);

			// Deactivate the Stem morph that created it
			World.getWorld().getSelectionModel().removeMorphFromSelection(stemmingSelectedShadow);
			World.getWorld().getSelectionModel().removeMorphFromSelection(getOwner());
			getOwner().tryToDeactivate();
		}
	}

	/**
	 * Sets the currently selected emplacement for stemming
	 * @param stemmingSelectedShadow
	 */
	public void setSelectedShadow(StemmingSelectionShadow stemmingSelectedShadow) {
		this.stemmingSelectedShadow = stemmingSelectedShadow;
	}

}
