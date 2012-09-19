package net.carmgate.morph.model.morph.stem;

import java.util.Set;

import net.carmgate.morph.model.World;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.behavior.stem.Stemming;
import net.carmgate.morph.model.morph.BasicMorph;
import net.carmgate.morph.model.morph.Morph.MorphType;
import net.carmgate.morph.model.morph.MorphUtil;
import net.carmgate.morph.model.requirements.EnoughMass;
import net.carmgate.morph.model.selection.SelectionAdapter;
import net.carmgate.morph.model.selection.SelectionEvent;
import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

@MorphInfo(type = MorphType.STEM_MORPH, initialMass = 100)
public class StemMorph extends BasicMorph {

	private static final Logger LOGGER = Logger.getLogger(StemMorph.class);

	/** These are the virtual morphs used to select in what place the future new morph should be put. */
	private Set<StemmingSelectionShadow> stemmingSelectionShadows;

	private Stemming stemmingBehavior;

	public StemMorph(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);
		stemmingBehavior = new Stemming(this);
		getActivableBehaviorList().add(stemmingBehavior);
		getActivationRequirements().add(new EnoughMass(this, 1));

		// Adding listeners
		World.getWorld().getSelectionModel().addSelectionListener(new SelectionAdapter() {
			@Override
			public void morphDeselected(SelectionEvent selectionEvent) {
				if (StemMorph.this.tryToDeactivate() == State.INACTIVE) {
					if (stemmingSelectionShadows != null) {
						getShip().removeMorphs(stemmingSelectionShadows);
						stemmingSelectionShadows = null;
					}
				}
			}

			@Override
			public void morphSelected(SelectionEvent selectionEvent) {
				if (StemMorph.this.tryToActivate() == State.ACTIVE) {

					// add the selection shadows to the ship if not already done.
					if (stemmingSelectionShadows == null) {
						stemmingSelectionShadows = MorphUtil.createSurroundingMorphs(StemMorph.this, StemmingSelectionShadow.class);
						for (StemmingSelectionShadow m : stemmingSelectionShadows) {
							m.setStemming(stemmingBehavior);
						}
						LOGGER.trace(stemmingSelectionShadows);
						getShip().addMorphs(stemmingSelectionShadows);

						// TODO Add code here to limit interaction with the interface
					}
				}
			}
		});
	}

}
