package net.carmgate.morph.model.solid;

public abstract class WorldSolid {

	/** the reference sequence for ids.*/
	private static long lastId = 0;
	/** the id of the current element. */
	private final long id = ++lastId;

	/**
	 * @return the unique id of solid model element. This is a unique Id for any element of the model.
	 */
	public final long getId() {
		return id;
	}
}
