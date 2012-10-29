package net.carmgate.morph.model.solid.morph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.carmgate.morph.model.solid.ship.Ship;

import org.apache.log4j.Logger;

public class MorphUtil {

	private static final Logger LOGGER = Logger.getLogger(MorphUtil.class);

	/**
	 * Add a new surrounding morph to the surroundingMorphs set.
	 * This method does not add a newly created morph if it has the same position as an existing morph
	 * in the ship (See {@link Ship#addMorph(Morph, float, float, float, boolean)}).
	 * @param ship the ship to which the surrounding morph should be added
	 * @param x the x coordinate in ship grid
	 * @param y the y coordinate in ship grid
	 * @param z the z coordinate in ship grid
	 * @param newMorph the morph to add
	 * @param surroundingMorphs the set of surrounding morph to which the newly created morph should be added.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static final <T extends Morph> void addOneSurroundingMorph(Ship ship, float x, float y, float z, T newMorph,
			Set<T> surroundingMorphs) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (ship.addMorph(newMorph, x, y, z, false)) {
			surroundingMorphs.add(newMorph);
		}
	}

	/**
	 * Adds the surrounding morphs of the provided morphs to their ship.
	 * This method does not add a newly created morph if it has the same position as an existing morph
	 * in the ship (See {@link Ship#addMorph(Morph, float, float, float, boolean)}).
	 * @param morphList 
	 * @return the added surrounding morphs or an empty set if no surrounding morphs has been added.
	 */
	public static final <T extends Morph> Set<T> addSurroundingMorphs(List<Morph> morphList, Class<T> template) {
		for (Morph m : morphList) {
			return internalAddSurroundingMorphs(m, template);
		}

		return new HashSet<T>();
	}

	/**
	 * Adds the surrounding morphs of the provided morph to its ship.
	 * This method does not add a newly created morph if it has the same position as an existing morph
	 * in the ship (See {@link Ship#addMorph(Morph, float, float, float, boolean)}).
	 * @param morph
	 * @return the added surrounding morphs or an empty set if no surrounding morphs has been added.
	 */
	public static final <T extends Morph> Set<T> addSurroundingMorphs(Morph morph, Class<T> template) {
		// get the surrounding morphs
		return internalAddSurroundingMorphs(morph, template);
	}

	/**
	 * Adds the surrounding morphs of the provided morph to its ship.
	 * This method does not add a newly created morph if it has the same position as an existing morph
	 * in the ship (See {@link Ship#addMorph(Morph, float, float, float, boolean)}).
	 * @param morph
	 * @return the added surrounding morphs or an empty set if no surrounding morphs has been added.
	 */
	private static final <T extends Morph> Set<T> internalAddSurroundingMorphs(Morph morph, Class<T> template) {
		Set<T> surroundingMorphs = new HashSet<T>();

		try {
			Constructor<T> templateConstructor = template.getConstructor();
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x - 1, morph.getPosInShipGrid().y, 0,
					templateConstructor.newInstance(), surroundingMorphs);
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x + 1, morph.getPosInShipGrid().y, 0,
					templateConstructor.newInstance(), surroundingMorphs);
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x, morph.getPosInShipGrid().y + 1, 0,
					templateConstructor.newInstance(), surroundingMorphs);
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x, morph.getPosInShipGrid().y - 1, 0,
					templateConstructor.newInstance(), surroundingMorphs);
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x + 1, morph.getPosInShipGrid().y - 1, 0,
					templateConstructor.newInstance(), surroundingMorphs);
			addOneSurroundingMorph(morph.getShip(), morph.getPosInShipGrid().x - 1, morph.getPosInShipGrid().y + 1, 0,
					templateConstructor.newInstance(), surroundingMorphs);
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
}
