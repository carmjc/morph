package net.carmgate.morph.ui.interaction.action.zoom;

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
