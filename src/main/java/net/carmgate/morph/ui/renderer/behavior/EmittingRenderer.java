package net.carmgate.morph.ui.renderer.behavior;

import net.carmgate.morph.model.behavior.Emitting;

import org.lwjgl.opengl.GL11;

public class EmittingRenderer extends BehaviorRenderer<Emitting> {

	@Override
	public void render(int glMode, RenderStyle drawType, Emitting emitting) {
		System.out.println("Shooting");
		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glColor3f(0.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINES);

		GL11.glVertex3f(emitting.getOwner().pos.x, emitting.getOwner().pos.y, emitting.getOwner().pos.z);
		GL11.glVertex3f(emitting.getTarget().x, emitting.getTarget().x, emitting.getTarget().x);

		GL11.glEnd();
		GL11.glColor3f(0.7f, 0.7f, 0.7f);
	}

}
