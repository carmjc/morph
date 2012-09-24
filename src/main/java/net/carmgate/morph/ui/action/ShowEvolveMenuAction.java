package net.carmgate.morph.ui.action;

import net.carmgate.morph.ui.UIModel;
import net.carmgate.morph.ui.UIModel.UIState;

import org.lwjgl.input.Keyboard;

public class ShowEvolveMenuAction implements Runnable {

	public void run() {
		if (Keyboard.getEventKey() == Keyboard.KEY_E && !Keyboard.getEventKeyState()) {
			if (UIModel.getUiModel().getUiState() != UIState.EVOLVING) {
				UIModel.getUiModel().setUiState(UIState.EVOLVING);
			} else {
				UIModel.getUiModel().setUiState(UIState.NOOP);
			}
		}
	}
}
