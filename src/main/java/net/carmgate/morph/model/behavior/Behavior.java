package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.World;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.morph.Morph;

/**
 * Allows to define any behavior on any element of the model.
 * Keep in mind, even if a behavior is strongly linked to a morph for instance,
 * We should always try to make it compatible with the larget set of elements.
 * For instance, &lt;T> should not be a particular type of Morph but simply a Morph
 * unless there are very good reasons to bare some Morph to get the behavior.
 * @param <T> the type of owner of the behavior.
 */
@BehaviorInfo
public abstract class Behavior<T extends Morph> {
	private final T owner;
	/** Number of millis before next activation, counting from last update. */
	private long msecBeforeNextActivation = 0;
	/** Number of millis before next deactivation, counting from last update. */
	private long msecBeforeNextDeactivation = 0;
	private State state;
	/** number of milliseconds from game start of the last behavior execution. */
	private long lastExecutionTS;
	/** activation TS. */
	private long activationTS;

	public Behavior(T owner) {
		this(owner, State.INACTIVE);
	}

	/**
	 * Warning : if the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * the initialState parameter of the constructor will be disregarded.
	 * @param owner the morph owning this behavior
	 * @param initialState {@link State#ACTIVE} or {@link State#INACTIVE}
	 */
	public Behavior(T owner, State initialState) {
		this.owner = owner;
		state = initialState;

		// Override the state value if the behavior is of the always active type
		if (getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			state = State.ACTIVE;
		}
	}

	/**
	 * Implement this method to activate the behavior.
	 * This method won't be called if cool down timers prevent the behavior from being activated.
	 * This implementation does nothing and returns true.
	 * @return true if the activation was successful.
	 */
	protected boolean activate() {
		return true;
	}

	/**
	 * Implement this method to deactivate the behavior.
	 * This method won't be called if cool down timers prevent the behavior from being deactivated.
	 * This implementation does nothing and returns true.
	 * @param forced true if the deactivation was forced
	 * @return true if the deactivation was successful.
	 */
	protected boolean deactivate(boolean forced) {
		return true;
	}

	/**
	 * Implement this method to "execute" the behavior.
	 * This method will be called regularly to allow the behavior to have an effect on it's owner,
	 * its owner's ship or the rest of the world. 
	 * @return true if the deactivation was successful.
	 */
	protected abstract void execute();

	public long getActivationTS() {
		return activationTS;
	}

	public long getLastExecutionTS() {
		return lastExecutionTS;
	}

	public T getOwner() {
		return owner;
	}

	/**
	 * Returns the state.
	 * If the behavior has annotation parameter {@link BehaviorInfo#alwaysActive()} set to true, 
	 * this will always return {@link State#ACTIVE}.
	 * @return
	 */
	public State getState() {
		if (getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			return State.ACTIVE;
		}
		return state;
	}

	/**
	 * Tries to activate the behavior.
	 * If the cool down timer has expired, the behavior is activated.
	 * This method do not execute the behavior, thus, there will be no direct tangible
	 * effect of the activation besides the fact that, if activated, it is possible to execute it.
	 * It is strongly discouraged to override this method.
	 * @return the behavior {@link State} after the call
	 */
	public final State tryToActivate() {
		if (state == State.ACTIVE) {
			// Cannot activate an active behavior
			return state;
		}

		if (msecBeforeNextActivation == 0 && activate()) {
			state = State.ACTIVE;
			activationTS = World.getWorld().getCurrentTS();
			msecBeforeNextDeactivation = getClass().getAnnotation(BehaviorInfo.class).deactivationCoolDownTime();
		} else {
			msecBeforeNextActivation--;
		}

		return state;
	}

	/**
	 * Tries to deactivate the behavior.
	 * If the deactivation cool down timer has expired, the behavior is deactivated
	 * Thus, the behavior might be prevented from deactivating.
	 * It is strongly discouraged to override this method.
	 * @return the behavior {@link State} after the call
	 */
	public final State tryToDeactivate() {
		return tryToDeactivate(false);
	}

	/**
	 * Tries to deactivate the behavior.
	 * If the deactivation cool down timer has expired, the behavior is deactivated
	 * Thus, the behavior might be prevented from deactivating.
	 * It is strongly discouraged to override this method.
	 * If the deactivation has been forced, the result of the {@link Behavior#deactivate()} method
	 * will be disregarded and deactivation will be completed whatever its value.
	 * If the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * it cannot be deactivated. 
	 * @param forced true to force deactivation regardless of cool down.
	 * @return the behavior {@link State} after the call
	 */
	public final State tryToDeactivate(boolean forced) {
		// if the behavior is always active, it cannot be deactivated
		if (state == State.INACTIVE || getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			// Cannot deactivate an inactive behavior
			return state;
		}

		if (forced) {
			deactivate(forced);
			state = State.INACTIVE;
			msecBeforeNextActivation = getClass().getAnnotation(BehaviorInfo.class).activationCoolDownTime();
		} else if (msecBeforeNextDeactivation == 0 && deactivate(forced)) {
			state = State.INACTIVE;
			msecBeforeNextActivation = getClass().getAnnotation(BehaviorInfo.class).activationCoolDownTime();
		} else {
			msecBeforeNextDeactivation--;
		}

		// reset last execution TS if deactivated
		if (state == State.INACTIVE) {
			lastExecutionTS = 0;
		}

		return state;
	}

	/**
	 * Execute the behavior.
	 * Activating a behavior just enables us to execute it, but it does nothing per se.
	 * Executing, on the contrary, really cause the behavior to do something.
	 * It is strongly discouraged to override this method.
	 * If the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * there is nothing to stop it from executing.
	 * @param msec the number of milliseconds from game start.
	 * @return true if the behavior was successfully executed.
	 */
	public final boolean tryToExecute() {
		// FIXME Should be done elsewhere. A behavior should not be responsible for deactivated its effects when its owner is disabled
		if (getOwner().isDisabled() && !getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			return false;
		}

		if (state == State.ACTIVE) {
			execute();
			lastExecutionTS = World.getWorld().getCurrentTS();
			return true;
		}

		return false;
	}
}
