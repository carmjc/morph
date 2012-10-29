package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.State;
import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.solid.morph.Morph;

/**
 * Behaviors not linked to a morph activation should inherit this abstract behavior.
 * @param <T>
 */
@BehaviorInfo(activationCoolDownTime = 0, deactivationCoolDownTime = 0, reactivationCoolDownTime = 0)
public abstract class NoActivationBehavior<T extends Morph> extends Behavior<T> {

	public NoActivationBehavior(T owner) {
		super(owner);
	}

	public NoActivationBehavior(T owner, State state) {
		super(owner, state);
	}

	@Override
	protected abstract void execute();

}
