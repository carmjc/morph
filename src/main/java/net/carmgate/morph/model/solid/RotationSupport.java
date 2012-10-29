package net.carmgate.morph.model.solid;

/**
 * Allows a world element to rotate.
 */
public interface RotationSupport {
	/** 
	 * @return the element rotation in degrees (0-360)
	 */
	float getRot();

	/** 
	 * @return the element rotation acceleration in degrees^2/s
	 */
	float getRotAccel();

	/** 
	 * @return the element rotation speed in degrees/s
	 */
	float getRotSpeed();

}
