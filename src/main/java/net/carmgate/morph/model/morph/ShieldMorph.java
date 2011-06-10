package net.carmgate.morph.model.morph;

public class ShieldMorph extends BasicMorph {

	public ShieldMorph(float x, float y, float z) {
		super(x, y, z);
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
		return 1000;
	}

	@Override
	public MorphType getType() {
		return MorphType.SHIELD;
	}
}
