package net.carmgate.morph.model.behavior;


/**
 * Allows to define any behavior on any element of the model.
 * Keep in mind, even if a behavior is strongly linked to a morph for instance,
 * We should always try to make it compatible with the larget set of elements.
 * For instance, &lt;T> should not be a particular type of Morph but simply a Morph
 * unless there are very good reasons to bare some Morph to get the behavior.
 * @param <T> the type of owner of the behavior.
 */
public abstract class Behavior<T> {
	public static enum State {
		ACTIVE,
		INACTIVE
	}

	private final T owner;
	/** Number of millis before next activation, counting from last update. */
	private long msecBeforeNextActivation = 0;
	/** Number of millis before next deactivation, counting from last update. */
	private long msecBeforeNextDeactivation = 0;
	private State state;
	/** number of milliseconds from game start of the last behavior update. */
	private long lastUpdateMsec;

	public Behavior(T owner) {
		this(owner, State.INACTIVE);
	}

	public Behavior(T owner, State initialState) {
		this.owner = owner;
		state = initialState;
	}

	@Deprecated
	protected abstract boolean activate();

	@Deprecated
	protected abstract boolean deactivate();

	protected abstract void execute();

	/**
	 * @return the time to wait before activation is available after deactivation (in millis)
	 */
	@Deprecated
	protected abstract int getActivationCoolDownTime();

	/**
	 * @return the time to wait before deactivation is available after activation (in millis)
	 */
	@Deprecated
	protected abstract int getDeactivationCoolDownTime();

	public long getLastUpdateMsec() {
		return lastUpdateMsec;
	}

	public T getOwner() {
		return owner;
	}

	public State getState() {
		return state;
	}

	/**
	 * Tries to activate the behavior.
	 * If the cool down timer has expired, the behavior is activated
	 */
	public void tryToActivate() {
		if (state == State.ACTIVE) {
			// Cannot activate an active behavior
			return;
		}

		if (msecBeforeNextActivation == 0 && activate()) {
			state = State.ACTIVE;
			msecBeforeNextDeactivation = getDeactivationCoolDownTime();
		} else {
			msecBeforeNextActivation--;
		}
	}

	/**
	 * Tries to deactivate the behavior.
	 * If the cool down timer has expired, the behavior is deactivated
	 */
	public void tryToDeactivate() {
		if (state == State.INACTIVE) {
			// Cannot deactivate an inactive behavior
			return;
		}

		if (msecBeforeNextDeactivation == 0 && deactivate()) {
			state = State.INACTIVE;
			msecBeforeNextActivation = getActivationCoolDownTime();
		} else {
			msecBeforeNextDeactivation--;
		}
	}

	/**
	 * @param msec the number of milliseconds from game start
	 */
	public void tryToExecute() {
		if (state == State.ACTIVE) {
			execute();
		}
	}
}
