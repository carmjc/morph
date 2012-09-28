package net.carmgate.morph.ui.model.iwmenu;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class IWMenu {
	private final Map<Integer, IWMenuItem> menuItems = new HashMap<Integer, IWMenuItem>();

	public Map<Integer, IWMenuItem> getMenuItems() {
		return menuItems;
	}

}
