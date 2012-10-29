package net.carmgate.morph.ui.model.iwmenu;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class IWMenu {
	private final Map<Long, IWMenuItem> menuItems = new HashMap<Long, IWMenuItem>();

	public Map<Long, IWMenuItem> getMenuItems() {
		return menuItems;
	}

}
