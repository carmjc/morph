package net.carmgate.morph.model.behavior;


/**
 * Allows to define any behavior on any element of the model.
 * @param <T> the type of owner of the behavior.
 */
public abstract class Behavior<T> {
	public static enum State {
		ACTIVE,
		INACTIVE
	}

	private final T owner;
	private int timeBeforeNextActivation = 0;

	private int timeBeforeNextDeactivation = 0;
	private State state;
	public Behavior(T owner) {
		this(owner, State.INACTIVE);
	}

	public Behavior(T owner, State initialState) {
		this.owner = owner;
		state = initialState;
	}

	protected abstract boolean activate();
	protected abstract boolean deactivate();
	protected abstract void execute();

	/**
	 * @return the time to wait before activation is available after deactivation
	 */
	protected abstract int getActivationCoolDownTime();

	/**
	 * @return the time to wait before deactivation is available after activation
	 */
	protected abstract int getDeactivationCoolDownTime();

	public T getOwner() {
		return owner;
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

		if (timeBeforeNextActivation == 0 && activate()) {
			state = State.ACTIVE;
			timeBeforeNextDeactivation = getDeactivationCoolDownTime();
		} else {
			timeBeforeNextActivation--;
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

		if (timeBeforeNextDeactivation == 0 && deactivate()) {
			state = State.INACTIVE;
			timeBeforeNextActivation = getActivationCoolDownTime();
		} else {
			timeBeforeNextDeactivation--;
		}
	}

	public void tryToExecute() {
		if (state == State.ACTIVE) {
			execute();
		}
	}
}
