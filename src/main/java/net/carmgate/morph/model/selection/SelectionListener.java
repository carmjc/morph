package net.carmgate.morph.model.selection;

public interface SelectionListener {

	void morphDeselected(SelectionEvent selectionEvent);

	void morphSelected(SelectionEvent selectionEvent);

	void shipSelected(SelectionEvent selectionEvent);
}
