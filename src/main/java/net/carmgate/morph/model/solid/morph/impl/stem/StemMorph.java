package net.carmgate.morph.model.solid.morph.impl.stem;

import java.util.Set;

import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.impl.morph.stem.Stemming;
import net.carmgate.morph.model.requirements.impl.EnoughMass;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.model.solid.morph.MorphUtil;
import net.carmgate.morph.model.solid.morph.impl.BasicMorph;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.selection.SelectionAdapter;
import net.carmgate.morph.ui.selection.SelectionEvent;

import org.apache.log4j.Logger;

@MorphInfo(possibleEvolutions = { EvolutionType.TO_BASIC })
// type = MorphType.STEM,
public class StemMorph extends BasicMorph {

	private static final Logger LOGGER = Logger.getLogger(StemMorph.class);

	/** These are the virtual morphs used to select in what place the future new morph should be put. */
	private Set<StemmingSelectionShadow> stemmingSelectionShadows;

	private Stemming stemmingBehavior;

	public StemMorph() {
		stemmingBehavior = new Stemming(this);
		getActivationLinkedBehaviorList().add(stemmingBehavior);
		getActivationRequirements().add(new EnoughMass(this, 1));

		// Adding selection listeners
		// This is used to handle the fact that the stem morph is (de)selected
		// If it is (de)selected, we must create(remove) the shadow morphs
		// used to select the stemming position
		UIModel.getUiModel().getSelectionModel().addSelectionListener(new SelectionAdapter() {
			@Override
			public final void morphDeselected(SelectionEvent selectionEvent) {
				if (selectionEvent.getSource() == StemMorph.this) {
					if (stemmingSelectionShadows != null) {
						getShip().removeMorphs(stemmingSelectionShadows);
						stemmingSelectionShadows = null;
					}
				}
			}

			@Override
			public final void morphSelected(SelectionEvent selectionEvent) {
				if (selectionEvent.getSource() == StemMorph.this
						&& StemMorph.this.tryToActivate() == State.ACTIVE) {

					// add the selection shadows to the ship if not already done.
					if (stemmingSelectionShadows == null) {
						stemmingSelectionShadows = MorphUtil.addSurroundingMorphs(StemMorph.this, StemmingSelectionShadow.class);
						for (StemmingSelectionShadow m : stemmingSelectionShadows) {
							m.setStemming(stemmingBehavior);
						}
						LOGGER.trace(stemmingSelectionShadows);

						// TODO Add code here to limit interaction with the interface (to make it "modal")
					}
				}
			}
		});
	}

}
