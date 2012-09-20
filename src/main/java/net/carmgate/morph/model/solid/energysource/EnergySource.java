package net.carmgate.morph.model.solid.energysource;

/**
 * An energy source outputs energy.
 * That energy can be used by morph to replenish their energy.
 */
public abstract class EnergySource {

	private static int lastId = 0;
	private int id = ++lastId;

	/**
	 * @return the id of the source. This is a unique Id for all sources.
	 */
	public int getId() {
		return id;
	}
}
