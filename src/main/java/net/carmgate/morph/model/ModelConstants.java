package net.carmgate.morph.model;

public interface ModelConstants {

	// ENERGY AND MASS MANAGEMENT IN MORPH
	/** the ratio between the amount of energy in excess and the mass loss it generates ({@value}). */
	public static final float MASS_LOSS_TO_EXCESS_ENERGY_RATIO = 1f;
	/** the ratio between the amount of energy in excess supported by the morph without structural damage
	 * and the max energy that the morph can store ({@value}). */
	public static final float MAX_EXCEED_ENERGY_AS_RATIO_OF_MAX_MORPH_ENERGY = .1f;
	/** The amount a new mass that a morph receives per second ({@value}). */
	public static final float NEW_MASS_PER_SECOND = 10f;

	// SPREADING ENERGY
	/** The rate of the energy transfer between to adjacent morphs ({@value}). */
	public static final float ENERGY_TRANSFER_PER_SEC = 1f;
	/** The amount of energy transfered from one morph to a neighbor during one second ({@value}). */
	public static final float MAX_DIFFUSED_EXCESS_ENERGY_PER_SECOND = 4f;

	// PROPULSORS
	/** Energy Consumption per millis at full thrust ({@value}). */
	public final static float ENERGY_CONSUMPTION_AT_FULL_THRUST = 0.1f;
	/** Intensity of the propulsor force at full thrust ({@value}). */
	public final static float PROPULSING_FORCE_MODULUS_AT_FULL_THRUST = 40000f;
	/** The slow down factor for the PropulsorLost behavior ({@value}). */
	public static final float SLOW_DOWN_FACTOR = 0.1f;

	/** The drag factor initial value ({@value}). */
	public static final float INITIAL_DRAG_FACTOR = 0.990f;
	/** The ship max speed ({@value}). */
	public static final float MAX_SPEED_PER_PROP_MORPH = 1000;
	/** Under that speed, the ship stops completely ({@value}). */
	public static final float MIN_SPEED = 0.00001f;
	/** The rotation drag factor. The lower, the more it's dragged ({@value}). */
	public static final float ROT_DRAG_FACTOR = 0.5f;

	/** The acceptable max ship speed when trying to determine if we reached the destination.
	 * This is used for the GoToPositionAI. */
	public static final int ACCEPTABLE_SHIP_SPEED = 30;
	/** The acceptable distance to target when trying to determine if we reached the destination.
	 * This is used for the GoToPositionAI. */
	public static final int ACCEPTABLE_DISTANCE_TO_TARGET = 2;

	// STEMMING
	/** The time it takes to stem for a stem morph ({@value}). */
	public static final int CREATION_TIME = 0;

	// MINING
	/** The amount of matter mined per second. */
	public static final int MINING_SPEED = 2;

}
