package net.carmgate.morph.ui.renderer.behavior.morph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.carmgate.morph.model.Vect3D;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.Progress;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.ui.renderer.RendererUtil;
import net.carmgate.morph.ui.renderer.behavior.BehaviorRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class ProgressRenderer extends BehaviorRenderer<Behavior<? extends Morph>> {

	private static final Logger LOGGER = Logger.getLogger(ProgressRenderer.class);

	private static final int nbSegments = 40;
	private static final double deltaAngle = 2 * Math.PI / nbSegments;
	private static final double cos = Math.cos(deltaAngle);
	private static final double sin = Math.sin(deltaAngle);

	private static Texture texture;

	static {
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream(ClassLoader.getSystemResource("ui/circular-progress-64.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Failed loading texture.", e);
		} catch (IOException e) {
			LOGGER.error("Failed loading texture.", e);
		}
	}

	@Override
	protected void renderBehavior(int glMode, RenderStyle drawType, Behavior<? extends Morph> behavior) {
		// if this behavior does not implement ProgressBehavior,
		// we have no way to render the progress
		if (!(behavior instanceof Progress)) {
			return;
		}

		Progress progressBehavior = (Progress) behavior;

		Vect3D posInWorld = behavior.getOwner().getPosInWorld();
		GL11.glTranslatef(posInWorld.x, posInWorld.y, posInWorld.z);

		float alphaLevel = 1f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);
		Texture tmpTexture = texture;
		int tmpNbSegments = nbSegments;
		double tmpCos = cos;
		double tmpSin = sin;
		float initialRadiusX = 0.5f;
		float initialRadiusY = 0;
		float radialPercentageOfDisc = progressBehavior.getProgress();
		RendererUtil.drawPartialCircle(tmpTexture, tmpNbSegments, tmpCos, tmpSin,
				initialRadiusX, initialRadiusY, radialPercentageOfDisc, true, glMode);
		alphaLevel /= 0.3f;
		GL11.glColor4f(1f, 1f, 1f, alphaLevel);

		GL11.glTranslatef(-posInWorld.x, -posInWorld.y, -posInWorld.z);
	}
}
