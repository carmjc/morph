package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.annotation.BehaviorInfo;
import net.carmgate.morph.model.morph.Morph;

@BehaviorInfo(activationCoolDownTime = 0, deactivationCoolDownTime = 0, reactivationCoolDownTime = 0)
public abstract class NoActivationBehavior<T extends Morph> extends Behavior<T> {

	public NoActivationBehavior(T owner) {
		super(owner);
	}

	@Override
	protected abstract void execute();

}
