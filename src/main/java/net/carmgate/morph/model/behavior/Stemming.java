package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.morph.StemMorph;

public class Stemming extends Behavior<StemMorph> {

	public Stemming(StemMorph owner) {
		super(owner);
	}

	@Override
	@Deprecated
	protected boolean activate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Deprecated
	protected boolean deactivate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	protected int getActivationCoolDownTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@Deprecated
	protected int getDeactivationCoolDownTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
