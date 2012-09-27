package net.carmgate.morph.ui.selection;

public interface SelectionListener {

	void inWorldMenuItemSelected(SelectionEvent selectionEvent);

	void morphDeselected(SelectionEvent selectionEvent);

	void morphSelected(SelectionEvent selectionEvent);

	void shipSelected(SelectionEvent selectionEvent);
}
