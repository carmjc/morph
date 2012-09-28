package net.carmgate.morph.ui.model.iwmenu;

public class IWMenuItem {

	private static int lastAssignedId = 0;

	/** The menu item id. For picking purposes. */
	private final int id;

	public IWMenuItem() {
		id = ++lastAssignedId;
	}

	public int getId() {
		return id;
	}
}
