package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.behavior.Emitting;

public class EmitterMorph extends BasicMorph {

	public EmitterMorph(float x, float y, float z) {
		super(x, y, z);
		activableSpecificBehaviorList.add(new Emitting(this, 1, 0, null));
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public float getMaxEnergy() {
		return 100;
	}

	@Override
	public MorphType getType() {
		return MorphType.EMITTER;
	}
}
