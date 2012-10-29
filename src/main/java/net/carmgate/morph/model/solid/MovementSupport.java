package net.carmgate.morph.model.solid;

import net.carmgate.morph.model.Vect3D;

/**
 * Allow a world element to move in the world.
 * <p>This interface needs {@link WorldPositionSupport}.</p>
 */
public interface MovementSupport {

	/**
	 * @return the element acceleration vector in world coordinates.
	 */
	Vect3D getPosAccel();

	/**
	 * @return the element speed vector in world coordinates.
	 */
	Vect3D getPosSpeed();

}
