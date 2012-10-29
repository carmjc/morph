package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;

import net.carmgate.morph.model.solid.particle.Particle;
import net.carmgate.morph.model.solid.particle.ParticleEngine;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ParticleRenderer implements Renderer<ParticleEngine> {

	private static final Logger LOGGER = Logger.getLogger(ParticleRenderer.class);

	private static Texture baseTexture;
	static {
		try {
			baseTexture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("particle-8.png").getPath()));
		} catch (Exception e) {
			LOGGER.error("Error while loading morph textures.", e);
		}
	}

	public void render(int glMode, net.carmgate.morph.ui.renderer.Renderer.RenderStyle drawType, ParticleEngine particleEngine, boolean background) {
		for (Particle p : particleEngine.getParticles()) {
			if (p.isBackground() == background) {
				GL11.glTranslatef(p.getPos().x, p.getPos().y, p.getPos().z);
				GL11.glColor4f(1, 1, 1, p.getLife() > 5000 ? 1 : (float) p.getLife() / 5000);
				RendererUtil.drawTexturedRectangle(baseTexture, glMode);
				GL11.glTranslatef(-p.getPos().x, -p.getPos().y, -p.getPos().z);
			}
		}
	}

	@Override
	public void render(int glMode, RenderStyle drawType, ParticleEngine particleEngine) {
		render(glMode, drawType, particleEngine, false);
	}

}
