package net.carmgate.morph.ui.interaction.actions;

import net.carmgate.morph.model.annotation.MorphInfo;
import net.carmgate.morph.model.behavior.Transforming;
import net.carmgate.morph.model.solid.morph.Morph;
import net.carmgate.morph.model.solid.morph.Morph.EvolutionType;
import net.carmgate.morph.ui.model.UIModel;
import net.carmgate.morph.ui.model.UIModel.UIState;
import net.carmgate.morph.ui.model.iwmenu.EvolutionTypeIWMenuItem;
import net.carmgate.morph.ui.model.iwmenu.IWMenu;
import net.carmgate.morph.ui.selection.SelectionAdapter;
import net.carmgate.morph.ui.selection.SelectionEvent;

import org.apache.log4j.Logger;

// TODO add comments
public class ShowEvolveMenuAction implements Runnable {

	/**
	 * Listens to menu item clicks and triggers evolutions.
	 */
	private final class MenuListener extends SelectionAdapter {
		@Override
		public void iwMenuItemSelected(SelectionEvent selectionEvent) {
			hideEvolvingMenu();
			EvolutionTypeIWMenuItem menuItem = (EvolutionTypeIWMenuItem) selectionEvent.getSource();
			Morph selectedMorph = UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().iterator().next();
			Transforming transformBehavior = new Transforming(selectedMorph, menuItem.getEvolutionType());
			selectedMorph.getAlternateBehaviorList().add(transformBehavior);
		}
	}

	private static final Logger LOGGER = Logger.getLogger(ShowEvolveMenuAction.class);

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

	@Override
	public void run() {
		if (UIModel.getUiModel().getSelectionModel().getSelectedMorphs().size() == 1) {
			if (UIModel.getUiModel().getUiState() != UIState.EVOLVING) {
				showEvolvingMenu();
			} else {
				hideEvolvingMenu();
			}
		}
	}

	/**
	 * Shows the menu items for evolution mecanism. 
	 */
	private void showEvolvingMenu() {
		IWMenu inWorldMenu = new IWMenu();
		Morph m = UIModel.getUiModel().getSelectionModel().getSelectedMorphs().values().iterator().next();
		for (EvolutionType type : m.getClass().getAnnotation(MorphInfo.class).possibleEvolutions()) {
			EvolutionTypeIWMenuItem menuItem = new EvolutionTypeIWMenuItem(type);
			inWorldMenu.getMenuItems().put(menuItem.getId(), menuItem);
		}
		UIModel.getUiModel().setCurrentInWorldMenu(inWorldMenu);
		UIModel.getUiModel().setUiState(UIState.EVOLVING);

		inWorldMenuListener = new MenuListener();
		UIModel.getUiModel().getSelectionModel().addSelectionListener(inWorldMenuListener);
	}
}
