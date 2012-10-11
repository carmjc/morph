package net.carmgate.morph.model.solid.morph.old;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.old.Emitting;
import net.carmgate.morph.model.solid.morph.BasicMorph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;

@Deprecated
@MorphInfo(type = MorphType.GUN)
public class EmitterMorph extends BasicMorph {

	public EmitterMorph() {
		getActivableBehaviorList().add(new Emitting(this, 1, 0, null));
	}

}
