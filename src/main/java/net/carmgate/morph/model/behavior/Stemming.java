package net.carmgate.morph.model.behavior;

import net.carmgate.morph.model.morph.StemMorph;

public class Stemming extends Behavior<StemMorph> {

	public Stemming(StemMorph owner) {
		super(owner);
	}

	@Override
	protected boolean activate() {
		return true;
	}

	@Override
	protected boolean deactivate(boolean forced) {
		return true;
	}

	@Override
	protected void execute() {
	}

}
