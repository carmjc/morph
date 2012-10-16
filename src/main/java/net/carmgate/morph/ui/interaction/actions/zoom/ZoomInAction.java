package net.carmgate.morph.ui.interaction.actions.zoom;

public class ZoomInAction implements Runnable {

	private ZoomAction zoomAction;

	public ZoomInAction(ZoomAction zoomAction) {
		this.zoomAction = zoomAction;
	}

	@Override
	public void run() {
		zoomAction.zoomIn();
	}

}
