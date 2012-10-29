package net.carmgate.morph.model.behavior.impl.morph;

import java.util.GregorianCalendar;

import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.NoActivationBehavior;
import net.carmgate.morph.model.behavior.Progress;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.ui.model.UIModel;

import org.apache.log4j.Logger;

public class Evolving extends NoActivationBehavior<Morph> implements Progress {

	private static final Logger LOGGER = Logger.getLogger(Evolving.class);

	private EvolutionType evolutionType;

	private long beginDate;

	public Evolving(Morph owner, EvolutionType evolutionType) {
		super(owner, State.ACTIVE);
		this.evolutionType = evolutionType;
	}

	@Override
	protected boolean activate() {
		// Store the date of transformation start
		beginDate = new GregorianCalendar().getTimeInMillis();

		// make it not selectable and deselect it
		getOwner().setSelectable(false);
		UIModel.getUiModel().getSelectionModel().removeMorphFromSelection(getOwner());

		return super.activate();
	}

	@Override
	protected void execute() {

		// Transform the morph only when time is up
		if (getProgress() >= 1) {
			try {
				Class<? extends Morph> newMorphClass = evolutionType.getMorphType();
				Morph newMorph = newMorphClass.newInstance();
				newMorph.setEnergy(Math.min(getOwner().getEnergy(), newMorphClass.getAnnotation(MorphInfo.class).maxEnergy()));
				newMorph.setMass(Math.min(getOwner().getMass(), newMorphClass.getAnnotation(MorphInfo.class).maxMass()));
				getOwner().getShip().addMorph(newMorph, getOwner().getPosInShipGrid(), true);
			} catch (InstantiationException e) {
				LOGGER.error("Error while creating new morph", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Error while creating new morph", e);
			}
			tryToDeactivate();
			getOwner().setSelectable(true);
		}
	}

	@Override
	public final float getProgress() {
		double currentTimeInMillis = new GregorianCalendar().getTimeInMillis();
		return (float) ((currentTimeInMillis - beginDate) / (evolutionType.getDurationInSeconds() * 1000));
	}
}
