package net.carmgate.morph.ui.renderer.morph;

import java.text.DecimalFormat;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Behavior;
import net.carmgate.morph.model.behavior.State;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.ui.renderer.UIRenderer;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

public class SelectedMorphRenderer extends MorphRenderer {

	private static final int LINE_FEED_HEIGHT = 3;
	private static final int X_PADDING = 10;
	private static final int Y_PADDING = 5;
	private static final int Y_START = -40;
	private static final int X_START = 100;

	public SelectedMorphRenderer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Renders a morph.
	 * The referential is center on the morph and rotated as the morph is rotated in the ship's referential or the world's referential
	 * if the morph is not attached to a ship.
	 */
	@Override
	public void render(int glMode, RenderStyle drawType, Morph morph) {

		// unbind texture
		TextureImpl.bindNone();

		String activeBehaviorList = "";
		for (Behavior<?> behavior : morph.getActivableBehaviorList()) {
			if (behavior.getState() == State.ACTIVE) {
				if (activeBehaviorList.length() > 0) {
					activeBehaviorList += ", ";
				}
				activeBehaviorList += behavior.getClass().getSimpleName();
			}
		}
		for (Behavior<?> behavior : morph.getAlwaysActiveBehaviorList()) {
			if (behavior.getState() == State.ACTIVE) {
				if (activeBehaviorList.length() > 0) {
					activeBehaviorList += ", ";
				}
				activeBehaviorList += behavior.getClass().getSimpleName();
			}
		}

		String[] selectedMorphInfos = new String[] {
				"Energy: " + Math.round(morph.getEnergy()) + "/"
						+ morph.getClass().getAnnotation(MorphInfo.class).maxEnergy(),
				"Energy flow: in=" + new DecimalFormat("0.0####").format(morph.getEnergyFlow()),
				"Energy in excess=" + new DecimalFormat("0.0####").format(morph.getExcessEnergy()),
				"Mass: " + Math.round(morph.getMass()) + "/"
						+ morph.getClass().getAnnotation(MorphInfo.class).maxMass(),
				"Currently active behaviors: " + activeBehaviorList,
		};

		int maxWidth = 0;
		int totalHeight = 0;
		for (String selectedMorphInfo : selectedMorphInfos) {
			// Get width and height of the text to draw
			totalHeight += UIRenderer.font.getHeight(selectedMorphInfo) + LINE_FEED_HEIGHT;
			maxWidth = Math.max(maxWidth, UIRenderer.font.getWidth(selectedMorphInfo));
		}
		totalHeight -= LINE_FEED_HEIGHT - Y_PADDING;

		// draw the label border
		GL11.glColor4f(0.9f, 0.9f, 0.9f, 0.9f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(X_START - X_PADDING, Y_START - Y_PADDING);
		GL11.glVertex2f(X_START + maxWidth + X_PADDING, Y_START - Y_PADDING);
		GL11.glVertex2f(X_START + maxWidth + X_PADDING, Y_START + totalHeight + Y_PADDING);
		GL11.glVertex2f(X_START - X_PADDING, Y_START + totalHeight + Y_PADDING);
		GL11.glEnd();
		GL11.glColor4f(0.9f, 0.9f, 0.9f, 0.9f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(X_START - X_PADDING, Y_START - Y_PADDING);
		GL11.glVertex2f(X_START + maxWidth + X_PADDING, Y_START - Y_PADDING);
		GL11.glVertex2f(X_START + maxWidth + X_PADDING, Y_START + totalHeight + Y_PADDING);
		GL11.glVertex2f(X_START - X_PADDING, Y_START + totalHeight + Y_PADDING);
		GL11.glEnd();

		int newTotalHeight = 0;
		for (String selectedMorphInfo : selectedMorphInfos) {
			UIRenderer.font.drawString(X_START, Y_START + newTotalHeight, selectedMorphInfo, Color.black);
			// UIRenderer.font.drawString(0, 0, selectedMorphInfo, Color.black);
			newTotalHeight += UIRenderer.font.getHeight(selectedMorphInfo) + LINE_FEED_HEIGHT;
		}
	}
}
