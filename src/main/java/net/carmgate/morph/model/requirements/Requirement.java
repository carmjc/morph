package net.carmgate.morph.model.requirements;

public interface Requirement {
	/**
	 * Returns true if the requirement is satisfied.
	 * @return
	 */
	boolean check();
}
