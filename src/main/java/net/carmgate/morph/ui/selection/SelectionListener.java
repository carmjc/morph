package net.carmgate.morph.ui.selection;

public interface SelectionListener {

	void iwMenuItemDeselected(SelectionEvent selectionEvent);

	void iwMenuItemSelected(SelectionEvent selectionEvent);

	void morphDeselected(SelectionEvent selectionEvent);

	void morphSelected(SelectionEvent selectionEvent);

	void shipDeselected(SelectionEvent selectionEvent);

	void shipSelected(SelectionEvent selectionEvent);
}
