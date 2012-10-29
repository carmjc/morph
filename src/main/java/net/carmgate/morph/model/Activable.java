package net.carmgate.morph.model;

import java.util.List;

import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.requirements.Requirement;

/**
 * Classes implementing this interface are required to manage an activation state.
 * <p>This state denotes if the element is active or not and has consequences on its behavior.
 * For instance an ACTIVE morph sees its "activation linked behaviors" executed each cycle 
 * whereas "activation linked behaviors" in an INACTIVE morph are not executed.</p>
 * <p>These classes are also required to <b>check activation requirements</b> before authorizing
 * activation.</p>
 * See {@link State}.
 */
public interface Activable {

	/**
	 * <p><b>Caution</b>: Overriding methods should always call the inherited class method if it exists to check as well.</p>
	 * @return true if the element can be activated
	 */
	public abstract boolean canBeActivated();

	/**
	 * The requirements that need to be validated before activating the element.
	 * @return a list of {@link Requirement}.
	 */
	List<Requirement> getActivationRequirements();

	/**
	 * @return the {@link State}.
	 */
	State getState();

	/**
	 * Tries to activate the element.
	 * <p>This method do not execute the element, thus, there will be no direct tangible
	 * effect of the activation besides the fact that, if activated, it is possible to execute it.</p>
	 * @return the element {@link State} after the call
	 */
	public abstract State tryToActivate();

	/**
	 * Tries to deactivate the element.
	 * @return the element {@link State} after the call
	 */
	public abstract State tryToDeactivate();

	/**
	 * <p>Tries to deactivate the element.</p>
	 * <p>If the deactivation has been forced, the result of the {@link Behavior#deactivate()} method
	 * will be disregarded and deactivation will be completed whatever its value.</p>
	 * @param forced true to force deactivation regardless of cool down.
	 * @return the behavior {@link State} after the call
	 */
	public abstract State tryToDeactivate(boolean forced);
}
