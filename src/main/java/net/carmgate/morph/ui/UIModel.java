package net.carmgate.morph.ui;

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
	private UIState uiState;

	private UIModel() {
		// Do nothing. This is just meant to prevent instanciation
		// from outside of the singleton.
	}

	/**
	 * @return the current UI state (See {@link UIState})
	 */
	public UIState getUiState() {
		return uiState;
	}

	/**
	 * @param uiState the current UI state (See {@link UIState})
	 */
	public void setUiState(UIState uiState) {
		this.uiState = uiState;
	}
}
