package net.carmgate.morph.model.solid.particle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.solid.world.World;

import org.apache.log4j.Logger;

public class ParticleEngine {

	private static final Logger LOGGER = Logger.getLogger(ParticleEngine.class);

	private List<Particle> particles = new LinkedList<Particle>();

	private int oscillationPeriod;

	/**
	 * Add a particle.
	 * @param x initial pos x coordinate.
	 * @param y initial pos y coordinate.
	 * @param z initial pos z coordinate.
	 * @param life the life expectancy in millis.
	 */
	public void addParticle(long life, Vect3D pos, Vect3D direction, int oscillationPeriod, boolean background) {
		particles.add(new Particle(life, pos, direction, oscillationPeriod, background));
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public void update() {
		long sinceLastUpdate = World.getWorld().getSinceLastUpdateTS();

		Iterator<Particle> i = particles.iterator();
		for (; i.hasNext();) {
			Particle p = i.next();
			p.life -= sinceLastUpdate;
			if (p.life <= 0) {
				i.remove();
				continue;
			}

			p.pos.x += p.direction.x * sinceLastUpdate / 1000;
			p.pos.y += p.direction.y * sinceLastUpdate / 1000;
			p.pos.z += p.direction.z * sinceLastUpdate / 1000;

			// apply normal perturbency if required
			// if (p.oscillationPeriod > 0) {
			// float f = (float) ((0.5f - Math.abs(1f - (float) p.life % p.oscillationPeriod / (p.oscillationPeriod / 2))) / (Math.random() * 100 + 100));
			// p.pos.x += p.normal.x * f;
			// p.pos.y += p.normal.y * f;
			// }
		}
	}
}
