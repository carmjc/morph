package net.carmgate.morph.ui.action;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.TransformingBehavior;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.MorphType;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.UIModel.UIState;
import net.carmgate.morph.ui.model.menu.InWorldMenu;
import net.carmgate.morph.ui.model.menu.MorphTypeIWMenuItem;
import net.carmgate.morph.ui.selection.SelectionAdapter;
import net.carmgate.morph.ui.selection.SelectionEvent;

import org.lwjgl.input.Keyboard;

// TODO add comments
public class ShowEvolveMenuAction implements Runnable {

	private final class MenuListener extends SelectionAdapter {
		@Override
		public void inWorldMenuItemSelected(SelectionEvent selectionEvent) {
			hideEvolvingMenu();
			MorphTypeIWMenuItem menuItem = (MorphTypeIWMenuItem) selectionEvent.getSource();
			Morph selectedMorph = UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().iterator().next();
			TransformingBehavior transformBehavior = new TransformingBehavior(selectedMorph, menuItem.getMorphType());
			selectedMorph.getAlternateBehaviorList().add(transformBehavior);
		}
	}

	private MenuListener inWorldMenuListener;

	public MenuListener getInWorldMenuListener() {
		return inWorldMenuListener;
	}

	private void hideEvolvingMenu() {
		UIModel.getUiModel().setCurrentInWorldMenu(null);
		UIModel.getUiModel().setUiState(UIState.NOOP);

		UIModel.getUiModel().getSelectionModel().removeSelectionListener(inWorldMenuListener);
		inWorldMenuListener = null;
	}

	public void run() {
		if (Keyboard.getEventKey() == Keyboard.KEY_E && !Keyboard.getEventKeyState()) {
			if (UIModel.getUiModel().getSelectionModel().getSelectedMorphs().size() == 1) {
				if (UIModel.getUiModel().getUiState() != UIState.EVOLVING) {
					showEvolvingMenu();
				} else {
					hideEvolvingMenu();
				}
			}
		}
	}

	/**
	 * 
	 */
	private void showEvolvingMenu() {
		InWorldMenu inWorldMenu = new InWorldMenu();
		Morph m = UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().iterator().next();
		for (MorphType type : m.getClass().getAnnotation(MorphInfo.class).possibleEvolutions()) {
			MorphTypeIWMenuItem menuItem = new MorphTypeIWMenuItem(type);
			inWorldMenu.getMenuItems().put(menuItem.getId(), menuItem);
		}
		UIModel.getUiModel().setCurrentInWorldMenu(inWorldMenu);
		UIModel.getUiModel().setUiState(UIState.EVOLVING);

		inWorldMenuListener = new MenuListener();
		UIModel.getUiModel().getSelectionModel().addSelectionListener(inWorldMenuListener);
	}
}
