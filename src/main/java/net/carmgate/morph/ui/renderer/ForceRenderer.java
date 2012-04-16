package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.physics.Force;

import org.lwjgl.opengl.GL11;

public class ForceRenderer implements Renderer<Force> {

	public void render(int glMode, RenderStyle drawType, Force force) {
		Vect3D forceVector = new Vect3D(force.vector);
		forceVector.rotate(force.target.getRotInShip() + force.target.getShip().rot);
		Vect3D targetPos = new Vect3D(force.target.getPosInShip());

		renderVector(glMode, drawType, targetPos, forceVector);
	}

	/**
	 * Renders an arrow given an origin and a direction vector.
	 * @param glMode
	 * @param drawType
	 * @param origin
	 * @param forceVector
	 */
	public void renderVector(int glMode, RenderStyle drawType, Vect3D orig, Vect3D forceVector) {
		Vect3D origin = new Vect3D(orig);
		Vect3D vector = new Vect3D(forceVector);
//		vector.normalize(vector.modulus() * VISIBILITY_FACTOR);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINES);

		// main line
		GL11.glVertex3f(origin.x, origin.y, origin.z);
		GL11.glVertex3f(origin.x + vector.x,
				origin.y + vector.y,
				origin.z + vector.z);

		// arrow detail
		origin.add(vector);
		vector.normalize(vector.modulus() / 3);
		vector.rotate(20);
		GL11.glVertex3f(origin.x, origin.y, origin.z);
		GL11.glVertex3f(origin.x - vector.x,
				origin.y - vector.y,
				origin.z - vector.z);
		vector.rotate(-40);
		GL11.glVertex3f(origin.x, origin.y, origin.z);
		GL11.glVertex3f(origin.x - vector.x, origin.y - vector.y, origin.z - vector.z);


		GL11.glEnd();
		GL11.glColor3f(0.7f, 0.7f, 0.7f);
	}

	/**
	 * Renders an arrow given an origine and a normalized vector
	 * @param glMode
	 * @param drawType
	 * @param origin
	 * @param forceVector
	 * @param modulus
	 */
	public void renderVector(int glMode, RenderStyle drawType, Vect3D origin, Vect3D forceVector, float modulus) {
		Vect3D normFoVect = new Vect3D(forceVector);
		normFoVect.normalize(modulus);
		renderVector(glMode, drawType, origin, normFoVect);
	}

}
