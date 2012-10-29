package net.carmgate.morph.model.behavior;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Activable;
import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.behavior.listener.BehaviorListener;
import net.carmgate.morph.model.requirements.Requirement;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

/**
 * Allows to define any behavior on any element of the model.
 * Keep in mind, even if a behavior is strongly linked to a morph for instance,
 * We should always try to make it compatible with the larget set of elements.
 * For instance, &lt;T> should not be a particular type of Morph but simply a Morph
 * unless there are very good reasons to bare some Morph to get the behavior.
 * @param <T> the type of owner of the behavior.
 */
@BehaviorInfo
public abstract class Behavior<T> implements Activable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Behavior.class);

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
	/** behavior listeners. */
	private final List<BehaviorListener> behaviorListeners = new ArrayList<BehaviorListener>();

	/** activation requirements. This property is lazily initialized. */
	private List<Requirement> activationRequirements;

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

		// Override the state value if the behavior is of the always active type
		if (initialState == State.ACTIVE || getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			state = tryToActivate();
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
	 * <p>Add a behavior listener.</p>
	 * <p>See {@link BehaviorListener} for details about events fired.
	 * @param behaviorListener
	 */
	public final void addBehaviorListener(BehaviorListener behaviorListener) {
		behaviorListeners.add(behaviorListener);
	}

	@Override
	public boolean canBeActivated() {
		if (msecBeforeNextActivation > 0) {
			return false;
		}

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

	@Override
	public final List<Requirement> getActivationRequirements() {
		if (activationRequirements == null) {
			activationRequirements = new ArrayList<Requirement>();
		}
		return activationRequirements;
	}

	/**
	 * @return the current TS at the time of last execution. 0 if the behavior has never been executed.
	 */
	public final long getActivationTS() {
		return activationTS;
	}

	/**
	 * @return the current TS at the time of last execution of the behavior.
	 * 0 if the behavior is inactive.
	 * <p>Caution is advised however: 0 does not mean the behavior is inactive.</p>  
	 */
	public final long getLastExecutionTS() {
		return lastExecutionTS;
	}

	/**
	 * @return the owner of the behavior.
	 */
	public final T getOwner() {
		return owner;
	}

	/**
	 * Returns the state.
	 * If the behavior has annotation parameter {@link BehaviorInfo#alwaysActive()} set to true, 
	 * this will always return {@link State#ACTIVE}.
	 * @return
	 */
	@Override
	public final State getState() {
		if (getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
			return State.ACTIVE;
		}
		return state;
	}

	/**
	 * Removes a behavior listener from the list of behavior listeners of this behavior.
	 * @param behaviorListener
	 */
	public final void removeBehaviorListener(BehaviorListener behaviorListener) {
		behaviorListeners.remove(behaviorListener);
	}

	/**
	 * <p><b>Caution</b>: Never call this method on a behavior that is in the activable behavior list of a morph.</p>
	 * <p>If the cool down timer has expired, the behavior is activated.</p>
	 * @return the behavior {@link State} after the call
	 */
	@Override
	public final State tryToActivate() {
		if (state == State.ACTIVE) {
			// Cannot activate an active behavior
			return state;
		}

		if (canBeActivated()) {
			if (activate()) {
				state = State.ACTIVE;
				activationTS = World.getWorld().getCurrentTS();
				msecBeforeNextDeactivation = getClass().getAnnotation(BehaviorInfo.class).deactivationCoolDownTime();
			}
		} else if (msecBeforeNextActivation > 0) {
			msecBeforeNextActivation--;
		}

		return state;
	}

	/**
	 * <p>If the deactivation cool down timer has expired, the behavior is deactivated
	 * Thus, the behavior might be prevented from deactivating.</p>
	 * <p>Never call this method on a behavior that is in the activable behavior list of a morph.</p>
	 * @return the behavior {@link State} after the call
	 */
	@Override
	public final State tryToDeactivate() {
		return tryToDeactivate(false);
	}

	/**
	 * <p>If the deactivation cool down timer has expired, the behavior is deactivated
	 * Thus, the behavior might be prevented from deactivating.</p>
	 * <p>If the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * it cannot be deactivated.</p> 
	 * <p>Never call this method on a behavior that is in the activable behavior list of a morph.</p>
	 * @param forced true to force deactivation regardless of cool down.
	 * @return the behavior {@link State} after the call
	 */
	@Override
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
	 * <p>Execute the behavior.</p>
	 * <p>Activating a behavior just enables us to execute it, but it does nothing per se.
	 * Executing, on the contrary, really cause the behavior to do something.</p>
	 * <p>If the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * there is nothing to stop it from executing.</p>
	 * <p>Never call this method on a behavior that is in the activable behavior list of a morph.</p>
	 * @return true if the behavior was successfully executed.
	 */
	public final boolean tryToExecute() {
		return tryToExecute(false);
	}

	/**
	 * <p>Execute the behavior.</p>
	 * <p>Activating a behavior just enables us to execute it, but it does nothing per se.
	 * Executing, on the contrary, really cause the behavior to do something.</p>
	 * <p>If the behavior has the annotation parameter {@link BehaviorInfo#alwaysActive()} set to true,
	 * there is nothing to stop it from executing.</p>
	 * <p>Never call this method on a behavior that is in the activable behavior list of a morph.</p>
	 * @param forced true if the {@link Behavior} should be activated regardless of the activation state
	 * of the owning morph.
	 * @return true if the behavior was successfully executed.
	 */
	public final boolean tryToExecute(boolean forced) {
		// FIXME Should be done elsewhere. A behavior should not be responsible for deactivated its effects when its owner is disabled

		// If the execution is not forced
		// if the behavior is not meant to be an always active behavior
		// and if it's owner is activable but inactive
		// then don't execute the behavior
		Activable activableOwner = null;
		if (owner instanceof Activable) {
			activableOwner = (Activable) getOwner();
		}
		if (!forced && activableOwner != null && activableOwner.getState() == State.INACTIVE && !getClass().getAnnotation(BehaviorInfo.class).alwaysActive()) {
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
