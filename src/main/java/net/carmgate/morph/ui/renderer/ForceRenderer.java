package net.carmgate.morph.ui.renderer;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.physics.Force;

import org.lwjgl.opengl.GL11;

public class ForceRenderer implements Renderer<Force> {

	private static final float VISIBILITY_FACTOR = 5000;

	public void render(int glMode, RenderStyle drawType, Force force) {
		Vect3D forceVector = new Vect3D(force.vector);
		forceVector.rotateZ(force.target.rot.z + force.target.ship.rot.z);
		Vect3D targetPos = new Vect3D(force.target.pos);
		forceVector.normalize(forceVector.modulus() * VISIBILITY_FACTOR);

		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINES);

		// main line
		GL11.glVertex3f(targetPos.x, targetPos.y, targetPos.z);
		GL11.glVertex3f(targetPos.x + forceVector.x,
				targetPos.y + forceVector.y,
				targetPos.z + forceVector.z);

		// arrow detail
		targetPos.add(forceVector);
		forceVector.normalize(forceVector.modulus() / 3);
		forceVector.rotateZ(20);
		GL11.glVertex3f(targetPos.x, targetPos.y, targetPos.z);
		GL11.glVertex3f(targetPos.x - forceVector.x,
				targetPos.y - forceVector.y,
				targetPos.z - forceVector.z);
		forceVector.rotateZ(-40);
		GL11.glVertex3f(targetPos.x, targetPos.y, targetPos.z);
		GL11.glVertex3f(targetPos.x - forceVector.x, targetPos.y - forceVector.y, targetPos.z - forceVector.z);


		GL11.glEnd();
		GL11.glColor3f(0.7f, 0.7f, 0.7f);
	}

}
