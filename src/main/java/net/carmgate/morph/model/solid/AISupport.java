package net.carmgate.morph.model.solid;

import net.carmgate.morph.ia.AI;
import net.carmgate.morph.util.collections.ModifiableIterable;

/**
 * Defines the necessary methods to allow an model element to be assigned AIs
 * @param <T> the type of the element that will be allowed to be assigned AIs.
 */
public interface AISupport<T> {

	/**
	 * @return the list of IAs of the ship.
	 */
	public abstract ModifiableIterable<AI<T>> getAIList();

}
