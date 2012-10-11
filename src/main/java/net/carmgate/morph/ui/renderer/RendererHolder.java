package net.carmgate.morph.ui.renderer;

public class RendererHolder {

	// Renderers
	public static final IWUIRenderer iwuiRenderer = new IWUIRenderer();
	public static final UIRenderer uiRenderer = new UIRenderer();
	public static final WorldRenderer worldRenderer = new WorldRenderer();

	private RendererHolder() {
		// Singleton private constructor
	}

}
