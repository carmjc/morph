package net.carmgate.morph.ui.model;

import net.carmgate.morph.ui.model.iwmenu.IWMenu;
import net.carmgate.morph.ui.selection.SelectionModel;

/**
 * This class stores any information specific to the UI.
 */
public class UIModel {

	/**
	 * Enum describing the different states of the UI.
	 * Currently, it's not possible for the UI to be in several different states
	 * at the same time.
	 */
	public enum UIState {
		EVOLVING,
		NOOP
	}

	/** This singleton instance. */
	private static UIModel uiModel = new UIModel();

	/**
	 * @return this singleton instance.
	 */
	public static UIModel getUiModel() {
		return uiModel;
	}

	/**
	 * The current UI state.
	 * Currently, it's not possible for the UI to be in several different states
	 * at the same time.
	 */
	private UIState uiState = UIState.NOOP;

	/** Current menu. Null if there is no current active menu.*/
	private IWMenu currentInWorldMenu;

	/** The selection model. */
	private final SelectionModel selectionModel = new SelectionModel();

	/**
	 * [SINGLETON] Privatized constructor.
	 */
	private UIModel() {
	}

	/**
	 * @return the current in-world menu.
	 */
	public IWMenu getCurrentIWMenu() {
		return currentInWorldMenu;
	}

	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * @return the current UI state (See {@link UIState})
	 */
	public UIState getUiState() {
		return uiState;
	}

	/**
	 * @param inWorldMenu the current in-world menu.
	 */
	public void setCurrentInWorldMenu(IWMenu inWorldMenu) {
		currentInWorldMenu = inWorldMenu;
	}

	/**
	 * @param uiState the current UI state (See {@link UIState})
	 */
	public void setUiState(UIState uiState) {
		this.uiState = uiState;
	}
}
