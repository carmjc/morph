package net.carmgate.morph.model.physic.morph.stem;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.stem.Stemming;
import net.carmgate.morph.model.physic.morph.Morph;
import net.carmgate.morph.model.physic.morph.Morph.MorphType;
import net.carmgate.morph.model.physic.ship.Ship;
import net.carmgate.morph.model.physic.world.World;
import net.carmgate.morph.ui.selection.SelectionAdapter;
import net.carmgate.morph.ui.selection.SelectionEvent;

import org.apache.log4j.Logger;

@MorphInfo(initialMass = 0, maxMass = 0, disableMass = -1, reEnableMass = -1, type = MorphType.SHADOW)
public class StemmingSelectionShadow extends Morph {

	private static final Logger LOGGER = Logger.getLogger(StemmingSelectionShadow.class);

	private Stemming stemming;

	public StemmingSelectionShadow(Ship ship, float x, float y, float z) {
		super(ship, x, y, z);

		World.getWorld().getSelectionModel().addSelectionListener(new SelectionAdapter() {
			@Override
			public void morphSelected(SelectionEvent selectionEvent) {
				if (selectionEvent.getSource() == StemmingSelectionShadow.this) {
					stemming.setSelectedShadow(StemmingSelectionShadow.this);
					LOGGER.trace("Stem shadow selected");
				}
			}
		});
	}

	@Override
	protected boolean activate() {
		// FIXME Handle the selection code
		return true;
	}

	@Override
	protected boolean deactivate() {
		// nothing to do
		return true;
	}

	/**
	 * Returns the stemming behavior responsible for this shadow's creation.
	 * @return
	 */
	public Stemming getStemming() {
		return stemming;
	}

	/**
	 * Sets the stemming behavior responsible for this shadow's creation.
	 * @param stemming
	 */
	public void setStemming(Stemming stemming) {
		this.stemming = stemming;
	}

}