package net.carmgate.morph.model.solid.particle;

import net.carmgate.morph.model.Vect3D;

public class Particle {

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

	public long getLife() {
		return life;
	}

	public Vect3D getPos() {
		return pos;
	}

	public boolean isBackground() {
		return background;
	}

}
