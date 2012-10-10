package net.carmgate.morph.ui.renderer.ship;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.ship.Ship;
import net.carmgate.morph.ui.renderer.Renderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class SelectedShipRenderer implements Renderer<Ship> {

	private static final Logger LOGGER = Logger.getLogger(SelectedShipRenderer.class);

	private static final int nbSegments = 60;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private static Texture texture;

	static {
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("ui/ship-selection-16.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading texture.", e);
		} catch (IOException e) {
			LOGGER.error("Failed loading texture.", e);
		}
	}

	public SelectedShipRenderer() {
	}

	@Override
	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, Ship ship) {
		Vect3D comPosInWorld = new Vect3D(ship.getPos());
		comPosInWorld.add(ship.getCenterOfMassInShip());
		GL11.glTranslatef(comPosInWorld.x, comPosInWorld.y, comPosInWorld.z);

		float maxDistance = 0;
		for (Morph m : ship.getMorphsByIds().values()) {
			if (!m.getClass().getAnnotation(MorphInfo.class).virtual()) {
				maxDistance = Math.max(maxDistance, comPosInWorld.distance(m.getPosInWorld()));
			}
		}
		maxDistance += 32;

		float alphaLevel = 0.2f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);
		texture.bind();
		double y1 = -maxDistance + 8; // radius = 1
		double x1 = 0;
		double y2 = -maxDistance; // radius = 1
		double x2 = 0;
		double x3 = cos * x1 - sin * y1;
		double y3 = sin * x1 + cos * y1;
		double x4 = cos * x2 - sin * y2;
		double y4 = sin * x2 + cos * y2;
		for (int i = 0; i < nbSegments; i++) { // nbSegments
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2d(x2, y2);
			GL11.glTexCoord2d(0, 1);
			GL11.glVertex2d(x1, y1);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2d(x3, y3);
			GL11.glTexCoord2d(1, 0);
			GL11.glVertex2d(x4, y4);
			GL11.glEnd();
			GL11.glRotatef(6, 0, 0, 1);
		}
		alphaLevel /= 0.3f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);

		GL11.glTranslatef(-comPosInWorld.x, -comPosInWorld.y, -comPosInWorld.z);
	}

}
