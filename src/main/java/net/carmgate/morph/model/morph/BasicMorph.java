package net.carmgate.morph.model.morph;

import net.carmgate.morph.model.behavior.SpreadingEnergy;


public class BasicMorph extends Morph {

	public BasicMorph(float x, float y, float z) {
		super(x, y, z);
		alwaysActiveSpecificBehaviorList.add(new SpreadingEnergy(this));
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
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public MorphType getType() {
		return MorphType.BASIC;
	}

}
