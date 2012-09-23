package net.carmgate.morph.model.solid.world;

import net.carmgate.morph.model.Vect3D;

/**
 * A world area is a portion of space holding a varying amount of free morph mass available to gather.
 * @author Carm
 */
public class WorldArea {
	/** coordinates are factored by WORLDAREA_SIZE. */
	public Vect3D pos;

	public float mass;

	/** World area size. */
	public static float WORLDAREA_SIZE = 100;

	/**
	 * Converts world coordinates to world areas coordinates.
	 * @param worldPos
	 * @return
	 */
	public static Vect3D toWorldAreaPos(Vect3D worldPos) {
		Vect3D worldAreaPos = new Vect3D(worldPos);
		worldAreaPos.x = (float) Math.floor(worldAreaPos.x / WORLDAREA_SIZE);
		worldAreaPos.y = (float) Math.floor(worldAreaPos.y / WORLDAREA_SIZE);
		worldAreaPos.z = (float) Math.floor(worldAreaPos.z / WORLDAREA_SIZE);
		return worldAreaPos;
	}

	/** x, y, z in world coordinates. */
	public WorldArea(float x, float y, float z) {
		pos = new Vect3D((float) Math.floor(x / WORLDAREA_SIZE), (float) Math.floor(y / WORLDAREA_SIZE), (float) Math.floor(z / WORLDAREA_SIZE));
	}

	/** x, y, z in world coordinates. */
	public WorldArea(Vect3D worldPos) {
		this(worldPos.x, worldPos.y, worldPos.z);
	}
}