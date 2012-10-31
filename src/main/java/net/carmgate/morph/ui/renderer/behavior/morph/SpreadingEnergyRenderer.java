package net.carmgate.morph.ui.renderer.behavior.morph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.impl.morph.EnergyDiffusing;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class SpreadingEnergyRenderer extends BehaviorRenderer<EnergyDiffusing> {

	private final static int TRANSFER_WIDTH = 8;

	private static final Logger LOGGER = Logger.getLogger(SpreadingEnergyRenderer.class);
	private static Texture transfertTexture;

	static {
		try {
			transfertTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("spreading-8.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		} catch (IOException e) {
			LOGGER.error("Failed loading gunfire texture: " + e);
		}
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, EnergyDiffusing behavior) {
		for (Morph m : behavior.getOwner().getNeighbors()) {
			if (m != null
					&& m.getEnergy() / m.getMaxEnergy() < behavior.getOwner().getEnergy() * 0.9
							/ behavior.getOwner().getMaxEnergy()
					&& m.getEnergy() < m.getMaxEnergy() * 0.9) {
				Vect3D direction = new Vect3D(m.getPosInWorld());
				direction.substract(behavior.getOwner().getPosInWorld());
				Vect3D middle = new Vect3D(m.getPosInWorld().x - direction.x / 2, m.getPosInWorld().y - direction.y / 2, 0);
				Vect3D normalizedDirection = new Vect3D(direction);
				normalizedDirection.normalize(TRANSFER_WIDTH);

				transfertTexture.bind();
				GL11.glColor4f(1, 1, 1, 0.6f);
				GL11.glBegin(GL11.GL_QUADS);
				float animationProgress = (float) World.getWorld().getCurrentTS() % 1000 / 1000;
				GL11.glTexCoord2f(0 - animationProgress, 0);
				GL11.glVertex2f(middle.x - normalizedDirection.x - normalizedDirection.y,
						middle.y - normalizedDirection.y + normalizedDirection.x);
				GL11.glTexCoord2f(0 - animationProgress, 1);
				GL11.glVertex2f(middle.x - normalizedDirection.x + normalizedDirection.y,
						middle.y - normalizedDirection.y - normalizedDirection.x);
				GL11.glTexCoord2f(1 - animationProgress, 1);
				GL11.glVertex2f(middle.x + normalizedDirection.x + normalizedDirection.y,
						middle.y + normalizedDirection.y - normalizedDirection.x);
				GL11.glTexCoord2f(1 - animationProgress, 0);
				GL11.glVertex2f(middle.x + normalizedDirection.x - normalizedDirection.y,
						middle.y + normalizedDirection.y + normalizedDirection.x);
				GL11.glEnd();
			}
		}
	}
}
