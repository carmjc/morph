package net.carmgate.morph.model.behavior;


public abstract class NoActivationBehavior<T> extends Behavior<T> {

	public NoActivationBehavior(T owner) {
		super(owner);
	}

	@Override
	protected final boolean activate() {
		return true;
	}

	@Override
	protected final boolean deactivate() {
		return true;
	}

	@Override
	protected abstract void execute();

	@Override
	protected final int getActivationCoolDownTime() {
		return 0;
	}

	@Override
	protected final int getDeactivationCoolDownTime() {
		return 0;
	}

	@Override
	public void tryToActivate() {
		activate();
	}

	@Override
	public void tryToDeactivate() {
		deactivate();
	}

	@Override
	public void tryToExecute() {
		execute();
	}

}
