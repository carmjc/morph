package net.carmgate.morph.ui.renderer.behavior;

import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.behavior.old.Emitting;

import org.lwjgl.opengl.GL11;

public class EmittingRenderer extends BehaviorRenderer<Emitting> {

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, Emitting emitting) {
		if (emitting.getState() == State.INACTIVE) {
			return;
		}

		System.out.println("Shooting from " + emitting.getOwner().getPosInShip() + " at " + emitting.getTarget());
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glColor3f(0.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINES);

		GL11.glVertex3f(emitting.getOwner().getPosInShip().x, emitting.getOwner().getPosInShip().y, emitting.getOwner().getPosInShip().z);
		GL11.glVertex3f(emitting.getTarget().x, emitting.getTarget().y, emitting.getTarget().z);

		GL11.glEnd();
		GL11.glColor3f(0.7f, 0.7f, 0.7f);
	}

}
