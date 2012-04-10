package net.carmgate.morph.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Vect3DTest {

	@Test
	public void testAngleWith() {
		Vect3D vect1 = new Vect3D(0, 1, 0);
		Vect3D vect2 = new Vect3D(vect1);
		vect2.rotate(-60);
		assertTrue("" + vect1.angleWith(vect2), (vect1.angleWith(vect2) + 60) % 360 < 0.00001);

		vect2.rotate(-60);
		assertTrue("" + vect1.angleWith(vect2), (vect1.angleWith(vect2) + 120) % 360 < 0.00001);

		vect2.rotate(-60);
		assertTrue("" + vect1.angleWith(vect2), (vect1.angleWith(vect2) + 180) % 360 < 0.00001);

		vect2.rotate(-60);
		assertTrue("" + vect1.angleWith(vect2), (vect1.angleWith(vect2) + 240) % 360 < 0.00001);
		assertTrue("Angle should be positive (" + vect1.angleWith(vect2) + ")", vect1.angleWith(vect2) > 0);

		vect2.rotate(120);
		assertTrue("" + vect1.angleWith(vect2), (vect1.angleWith(vect2) + 120) % 360 < 0.00001);
		assertTrue("Angle should be ngative (" + vect1.angleWith(vect2) + ")", vect1.angleWith(vect2) < 0);
	}

}
