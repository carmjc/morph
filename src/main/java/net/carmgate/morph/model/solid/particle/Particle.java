package net.carmgate.morph.model.solid.particle;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.WorldPositionSupport;

public class Particle implements WorldPositionSupport {

	/** true if the particle should be renderer in the background. */
	protected boolean background;

	/** Life expectancy, in millis. */
	protected long life;

	/** the particle position. */
	protected Vect3D pos;

	/** the particle speed. */
	protected Vect3D direction;

	/** vector normal to the direction. */
	protected Vect3D normal = new Vect3D();

	public int oscillationPeriod;

	/**
	 * @param life life expectancy (in millis)
	 */
	public Particle(long life, Vect3D pos, Vect3D direction, int oscillationPeriod, boolean background) {
		this.life = life;
		this.pos = pos;
		this.direction = direction;
		this.background = background;
		if (oscillationPeriod > 0) {
			if (Math.random() > 0.5) {
				normal.x = direction.y;
				normal.y = -direction.x;
			} else {
				normal.x = -direction.y;
				normal.y = direction.x;
			}
		}
	}

	public final long getLife() {
		return life;
	}

	@Override
	public final Vect3D getPos() {
		return pos;
	}

	public final boolean isBackground() {
		return background;
	}

	public float getRotSpeed() {
		return 0;
	}

	public float getRotAccel() {
		return 0;
	}

	public float getRot() {
		return 0;
	}

}
