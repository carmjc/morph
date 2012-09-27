package net.carmgate.morph.ui.model.menu;

import net.carmgate.morph.model.solid.morph.Morph.MorphType;

public class MorphTypeIWMenuItem extends IWMenuItem {

	private final MorphType morphType;

	/**
	 * Creates a new menu item mapping a given morph type.
	 * @param morphType a {@link MorphType}
	 */
	public MorphTypeIWMenuItem(MorphType morphType) {
		this.morphType = morphType;
	}

	/**
	 * @return the {@link MorphType}
	 */
	public MorphType getMorphType() {
		return morphType;
	}
}
