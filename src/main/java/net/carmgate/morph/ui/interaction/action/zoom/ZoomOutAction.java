package net.carmgate.morph.ui.interaction.action.zoom;

public class ZoomOutAction implements Runnable {

	private ZoomAction zoomAction;

	public ZoomOutAction(ZoomAction zoomAction) {
		this.zoomAction = zoomAction;
	}

	@Override
	public void run() {
		zoomAction.zoomOut();
	}

}
