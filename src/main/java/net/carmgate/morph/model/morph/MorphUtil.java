package net.carmgate.morph.model.morph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import net.carmgate.morph.model.ship.Ship;

public class MorphUtil {
	
	private static final Logger LOGGER = Logger.getLogger(MorphUtil.class);

	/**
	 * Returns a set of basic morphs containing a morph for each position
	 * adjacent to the given morph.
	 * @param morph
	 * @return
	 */
	private static Set<Morph> createSurroundingMorphs(Morph morph) {
		Set<Morph> surroundingMorphs = new HashSet<Morph>();

		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x - 1, morph.shipGridPos.y, 0));
		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x + 1, morph.shipGridPos.y, 0));
		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x, morph.shipGridPos.y + 1, 0));
		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x, morph.shipGridPos.y - 1, 0));
		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x + 1, morph.shipGridPos.y - 1, 0));
		surroundingMorphs.add(new SurroundingMorph(morph.getShip(), morph.shipGridPos.x - 1, morph.shipGridPos.y + 1, 0));
		morph.updatePosFromGridPos();

		return surroundingMorphs;
	}

	/**
	 * Returns a set of morphs containing a surrounding morph for each position
	 * adjacent to a morph in the list.
	 * Surrounding morphs overlapping morphs in the same ship
	 * are removed from the result list.
	 * @param ship
	 * @return
	 */
	public static Set<Morph> createSurroundingMorphs(List<Morph> morphList) {
		Set<Morph> surroundingMorphs = new HashSet<Morph>();

		// add the surrounding morphs of all ship's morphs.
		// since we use a set, we won't have any duplicate
		for (Morph m : morphList) {
			surroundingMorphs.addAll(createSurroundingMorphs(m));
		}

		// Iterate over the surrounding created morphs to 
		// delete those that are at the same place as ships own morphs
		for (Iterator<Morph> i = surroundingMorphs.iterator(); i.hasNext(); ) {
			Morph m = i.next();
			
			if (m.getShip().getMorphList().contains(m)) {
				i.remove();
			}
		}

		return surroundingMorphs;
	}
	
}
