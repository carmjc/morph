package net.carmgate.morph.model.behavior.listener;

import net.carmgate.morph.model.behavior.Behavior;

public class BehaviorEvent {
	private Behavior<?> source;

	public BehaviorEvent(Behavior<?> source) {
		this.source = source;
	}

	public Behavior<?> getSource() {
		return source;
	}
}
