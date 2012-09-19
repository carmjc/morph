package net.carmgate.morph.model.morph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.carmgate.morph.model.ship.Ship;

import org.apache.log4j.Logger;

public class MorphUtil {

	private static final Logger LOGGER = Logger.getLogger(MorphUtil.class);

	/**
	 * Returns a set of morphs containing a surrounding morph for each position
	 * adjacent to a morph in the list.
	 * Surrounding morphs overlapping morphs in the same ship
	 * are removed from the result list.
	 * @param morphList
	 * @return
	 */
	public static <T extends Morph> Set<T> createSurroundingMorphs(List<Morph> morphList, Class<T> template) {
		Set<T> surroundingMorphs = new HashSet<T>();

		// add the surrounding morphs of all ship's morphs.
		// since we use a set, we won't have any duplicate
		for (Morph m : morphList) {
			surroundingMorphs.addAll(internalCreateSurroundingMorphs(m, template));
		}

		removeAlreadyExistingMorphs(surroundingMorphs);
		return surroundingMorphs;
	}

	/**
	 * Returns a set of morphs containing a surrounding morph for each position
	 * adjacent to the provided morph.
	 * Surrounding morphs overlapping morphs in the same ship
	 * are removed from the result list.
	 * @param morph
	 * @return
	 */
	public static <T extends Morph> Set<T> createSurroundingMorphs(Morph morph, Class<T> template) {
		// get the surrounding morphs
		Set<T> surroundingMorphs = new HashSet<T>(internalCreateSurroundingMorphs(morph, template));

		removeAlreadyExistingMorphs(surroundingMorphs);
		return surroundingMorphs;
	}

	/**
	 * Returns a set of basic morphs containing a morph for each position
	 * adjacent to the given morph.
	 * @param morph
	 * @return
	 */
	private static <T extends Morph> Set<T> internalCreateSurroundingMorphs(Morph morph, Class<T> template) {
		Set<T> surroundingMorphs = new HashSet<T>();

		Constructor<T> templateConstructor = null;
		try {
			templateConstructor = template.getConstructor(Ship.class, float.class, float.class, float.class);
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x - 1, morph.getShipGridPos().y, 0));
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x + 1, morph.getShipGridPos().y, 0));
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x, morph.getShipGridPos().y + 1, 0));
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x, morph.getShipGridPos().y - 1, 0));
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x + 1, morph.getShipGridPos().y - 1, 0));
			surroundingMorphs.add(templateConstructor.newInstance(morph.getShip(), morph.getShipGridPos().x - 1, morph.getShipGridPos().y + 1, 0));
		} catch (SecurityException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		} catch (NoSuchMethodException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		} catch (InstantiationException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		} catch (InvocationTargetException e) {
			LOGGER.error("Error while creating surrounding morphs", e);
		}

		return surroundingMorphs;
	}

	/**
	 * @param surroundingMorphs
	 */
	private static <T extends Morph> void removeAlreadyExistingMorphs(Set<T> surroundingMorphs) {
		// Iterate over the surrounding created morphs to
		// delete those that are at the same place as ships own morphs
		for (Iterator<T> i = surroundingMorphs.iterator(); i.hasNext();) {
			T m = i.next();

			if (m.getShip().getMorphs().values().contains(m)) {
				i.remove();
			}
		}
	}

}
