package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.carmgate.morph.Main;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.world.World;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.iwmenu.EvolutionTypeIWMenuItem;
import net.carmgate.morph.ui.model.iwmenu.IWMenu;
import net.carmgate.morph.ui.model.iwmenu.IWMenuItem;
import net.carmgate.morph.ui.renderer.morph.MorphRenderer;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

public class IWUIRenderer implements Renderer<World> {
	private static final Logger LOGGER = Logger.getLogger(UIRenderer.class);
	private static Texture baseTexture;
	static {
		try {
			baseTexture = TextureLoader.getTexture("PNG",
					new FileInputStream(ClassLoader.getSystemResource("ui/circular-menu2-128.png").getPath()));
		} catch (FileNotFoundException e) {
			LOGGER.error("Problem loading textures.", e);
		} catch (IOException e) {
			LOGGER.error("Problem loading textures.", e);
		}

	}

	public void init() {
	}

	@Override
	public void render(int glMode, RenderStyle drawType, World world) {
		// TODO This should maybe evolve towards a factory system
		switch (UIModel.getUiModel().getUiState()) {
		case EVOLVING:
			renderEvolvingContextualMenu(glMode, drawType);
			break;
		case NOOP:
			break;
		}
	}

	/**
	 * Renders the contextual menu associated with a morph.
	 * @param glMode
	 * @param drawType
	 */
	private void renderEvolvingContextualMenu(int glMode, RenderStyle drawType) {
		GL11.glPushName(Main.PickingContext.IW_MENU.ordinal());

		Map<Integer, Morph> selectedMorphs = UIModel.getUiModel().getSelectionModel().getSelectedMorphs();
		if (selectedMorphs.size() > 0) {
			Morph m = selectedMorphs.values().iterator().next();
			// TODO investigate why it is necessary to add 5 to the morph coordinates
			// to get the proper position of the contextual menu.
			GL11.glTranslatef(m.getPosInWorld().x, m.getPosInWorld().y, m.getPosInWorld().z);

			// Iterator<Class<? extends Morph>> iterator = UIModel.getUiModel().getPossibleMorphEvols(m).iterator();
			IWMenu currentIWMenu = UIModel.getUiModel().getCurrentIWMenu();
			if (currentIWMenu != null) {
				Iterator<IWMenuItem> iterator = currentIWMenu.getMenuItems().values().iterator();
				for (int i = 0; i < 6; i++) {
					IWMenuItem menuItem = null;

					// Get next possible evol if there is one
					if (iterator.hasNext()) {
						menuItem = iterator.next();
					}

					// Change the color of the menu item if there is no other choice
					if (menuItem != null) {
						GL11.glColor4f(1f, 1f, 1f, 0.8f);
					} else {
						GL11.glColor4f(1f, 1f, 1f, 0.1f);
					}

					// Draw the menu item
					if (glMode == GL11.GL_SELECT) {
						if (menuItem != null) {
							GL11.glPushName(menuItem.getId());
						}
					}

					// Draw the morph matching the possible evolution
					if (menuItem instanceof EvolutionTypeIWMenuItem) {
						if (glMode != GL11.GL_SELECT) {
							baseTexture.bind();
						} else {
							TextureImpl.bindNone();
						}

						GL11.glColor4f(1, 1, 1, 0.5f);
						GL11.glBegin(GL11.GL_QUADS);
						// We need to draw trapezoids instead of squares
						// If we don't, we get 6 overlapping squares for the menu items and there is no way to know
						// which one is picked when in select mode.
						// (0, 0)
						GL11.glTexCoord2f(243f / 512, 225f / 512);
						GL11.glVertex2f(baseTexture.getTextureWidth() * (243 - 256) / 256 / 2, baseTexture.getTextureHeight() * (225 - 256) / 256 / 2);
						// (0, 1)
						GL11.glTexCoord2f(268f / 512, 225f / 512);
						GL11.glVertex2f(baseTexture.getTextureWidth() * (268 - 256) / 256 / 2, baseTexture.getTextureHeight() * (225 - 256) / 256 / 2);
						// (1, 1)
						GL11.glTexCoord2f(374f / 512, 44f / 512);
						GL11.glVertex2f(baseTexture.getTextureWidth() * (374 - 256) / 256 / 2, baseTexture.getTextureHeight() * (44 - 256) / 256 / 2);
						// (1, 0)
						GL11.glTexCoord2f(138f / 512, 44f / 512);
						GL11.glVertex2f(baseTexture.getTextureWidth() * (138 - 256) / 256 / 2, baseTexture.getTextureHeight() * (44 - 256) / 256 / 2);
						GL11.glEnd();
						GL11.glColor4f(1f, 1f, 1f, 1f);
						GL11.glColor4f(1, 1, 1, 1f);

						if (glMode != GL11.GL_SELECT) {
							renderMorphTypeMenuItemContent(glMode, i, menuItem);
						}
					}

					if (glMode == GL11.GL_SELECT) {
						if (menuItem != null) {
							GL11.glPopName();
						}
					}

					GL11.glRotatef(60, 0, 0, 1);
				}
			}

			GL11.glTranslatef(-m.getPosInWorld().x, -m.getPosInWorld().y, -m.getPosInWorld().z);
		}

		GL11.glPopName();
	}

	/**
	 * @param glMode
	 * @param menuItemIndex
	 * @param menuItem
	 */
	private void renderMorphTypeMenuItemContent(int glMode, int menuItemIndex, IWMenuItem menuItem) {
		if (menuItem != null && glMode != GL11.GL_SELECT) {
			Texture baseTexture = MorphRenderer.getBaseTexture();
			if (baseTexture != null) {
				if ((float) World.getWorld().getCurrentTS() / 1000 % 1 < 0.5f) {
					GL11.glColor4f(1, 1, 1, 0.75f + (float) World.getWorld().getCurrentTS() / 1000 % 1 / 2);
				} else {
					GL11.glColor4f(1, 1, 1, 1.25f - (float) World.getWorld().getCurrentTS() / 1000 % 1 / 2);
				}

				GL11.glTranslatef(0, -40, 0);
				GL11.glRotatef(-60 * menuItemIndex, 0, 0, 1);

				baseTexture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
				GL11.glEnd();

				GL11.glRotatef(60 * menuItemIndex, 0, 0, 1);
				GL11.glTranslatef(0, 40, 0);
			}
			Texture evolTexture = MorphRenderer.getTextures().get(((EvolutionTypeIWMenuItem) menuItem).getEvolutionType().getMorphType());
			if (evolTexture != null) {
				if ((float) World.getWorld().getCurrentTS() / 1000 % 1 < 0.5f) {
					GL11.glColor4f(1, 1, 1, 0.75f + (float) World.getWorld().getCurrentTS() / 1000 % 1 / 2);
				} else {
					GL11.glColor4f(1, 1, 1, 1.25f - (float) World.getWorld().getCurrentTS() / 1000 % 1 / 2);
				}

				GL11.glTranslatef(0, -40, 0);
				GL11.glRotatef(-60 * menuItemIndex, 0, 0, 1);

				evolTexture.bind();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(-evolTexture.getTextureWidth() / 2, -evolTexture.getTextureWidth() / 2);
				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(evolTexture.getTextureWidth() / 2, -evolTexture.getTextureWidth() / 2);
				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(evolTexture.getTextureWidth() / 2, evolTexture.getTextureHeight() / 2);
				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(-evolTexture.getTextureWidth() / 2, evolTexture.getTextureHeight() / 2);
				GL11.glEnd();

				GL11.glRotatef(60 * menuItemIndex, 0, 0, 1);
				GL11.glTranslatef(0, 40, 0);
			}
		}
	}
}
