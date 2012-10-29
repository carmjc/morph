package net.carmgate.morph.ui.model.iwmenu;

import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;

public class EvolutionTypeIWMenuItem extends IWMenuItem {

	private final EvolutionType evolutionType;

	/**
	 * Creates a new menu item mapping a given morph type.
	 * @param type a {@link MorphType}
	 */
	public EvolutionTypeIWMenuItem(EvolutionType type) {
		evolutionType = type;
	}

	/**
	 * @return the {@link MorphType}
	 */
	public EvolutionType getEvolutionType() {
		return evolutionType;
	}
}
