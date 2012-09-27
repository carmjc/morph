package net.carmgate.morph.ui.model.menu;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class InWorldMenu {
	private final Map<Integer, IWMenuItem> menuItems = new HashMap<Integer, IWMenuItem>();

	public Map<Integer, IWMenuItem> getMenuItems() {
		return menuItems;
	}

}
